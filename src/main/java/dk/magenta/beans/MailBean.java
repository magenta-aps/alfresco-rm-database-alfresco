package dk.magenta.beans;


import dk.magenta.model.DatabaseModel;
import net.coobird.thumbnailator.Thumbnails;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.transform.ContentTransformer;
import org.alfresco.repo.jscript.ScriptLogger;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;

import org.json.JSONException;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.odftoolkit.odfdom.incubator.doc.style.OdfStyle;
import org.odftoolkit.odfdom.type.Color;
import org.odftoolkit.simple.TextDocument;
import org.odftoolkit.simple.style.Border;
import org.odftoolkit.simple.style.StyleTypeDefinitions;
import org.odftoolkit.simple.table.Cell;
import org.odftoolkit.simple.table.Row;
import org.odftoolkit.simple.table.Table;
import org.odftoolkit.simple.text.Paragraph;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.imageio.ImageIO;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

import static dk.magenta.model.DatabaseModel.ASPECT_ADDSIGNATURE;
import static dk.magenta.model.DatabaseModel.PROP_SUPERVISINGDOCTOR;
import static org.odftoolkit.simple.style.StyleTypeDefinitions.HorizontalAlignmentType.LEFT;

//import org.apache.poi.hwpf.HWPFDocument;

public class MailBean {

    public static final int DEFAULT_BUFFER_SIZE = 8192;

    private NodeService nodeService;

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    private SiteService siteService;

    public void setPropertyValuesBean(PropertyValuesBean propertyValuesBean) {
        this.propertyValuesBean = propertyValuesBean;
    }

    private PropertyValuesBean propertyValuesBean;

    private String siteShortName = "Retspsyk";

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    private PersonService personService;

    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    private AuthenticationService authenticationService;

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    private ContentService contentService;

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public NodeRef[] getNodeRefsToMail(NodeRef[] noderefs) {

        NodeRef[] result = new NodeRef[noderefs.length];

        for (int i =0; i<= noderefs.length-1; i++) {

            NodeRef doc = noderefs[i];
            result[i] = transform(doc);

        }
        return result;
    }


    public void sendEmail(NodeRef[] attachmentNodeRefs, String authority, String body, String subject, boolean addSignature, NodeRef declaration) throws Exception {


        logEvent(attachmentNodeRefs, authority);

        ArrayList<NodeRef> pds_to_be_deleted  = new ArrayList<NodeRef>();

        String to = authority;

        Properties props = new Properties();
        props.put("mail.smtp.host", "mail1.rm.dk");
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.port", "25");


        // for use in testing
//        Session session = Session.getInstance(props,
//                new javax.mail.Authenticator() {
//                    protected PasswordAuthentication getPasswordAuthentication() {
//                        return new PasswordAuthentication("magentatestdokument2018@gmail.com", "alexandersnegl");
//                    }
//                });

        Session session = Session.getInstance(props);



        try {
            InternetAddress fromAddress = new InternetAddress("ps.o.faelles.post@rm.dk");
            InternetAddress toAddress = new InternetAddress(to);

            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(fromAddress);
            msg.setRecipient(Message.RecipientType.TO, toAddress);
            msg.setSubject(subject);
            msg.setSentDate(new Date());

            MimeBodyPart messagePart = new MimeBodyPart();
            messagePart.setText(body);


            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messagePart);



            for (int i=0; i <= attachmentNodeRefs.length-1;i++) {

                NodeRef attachmentNodeRef = attachmentNodeRefs[i];
                NodeRef transformed = null;

                if (addSignature) {
                    if (nodeService.hasAspect(attachmentNodeRef, ASPECT_ADDSIGNATURE)) {
                        // return the nodeRef and  replace this with the one without the signature in the list of attachments
                        NodeRef documentWithSignature = this.addSignature(attachmentNodeRef, declaration);
                        pds_to_be_deleted.add(documentWithSignature);
                        transformed = this.transform(documentWithSignature);
                    }
                    else {
                        transformed = this.transform(attachmentNodeRef);
                    }
                }
                else {
                    transformed = this.transform(attachmentNodeRef);

                }
                pds_to_be_deleted.add(transformed);

                final MimeBodyPart attachment = new MimeBodyPart();
                NodeRef finalTransformed = transformed;
                attachment.setDataHandler(new DataHandler(new DataSource() {

                    public InputStream getInputStream() throws IOException {
                        ContentReader reader = contentService.getReader(finalTransformed, ContentModel.PROP_CONTENT);
                        return reader.getContentInputStream();
                    }

                    public OutputStream getOutputStream() throws IOException {
                        throw new IOException("Read-only data");
                    }

                    public String getContentType() {
                        return contentService.getReader(finalTransformed, ContentModel.PROP_CONTENT).getMimetype();
                    }

                    public String getName() {
                        return nodeService.getProperty(finalTransformed, ContentModel.PROP_NAME).toString();
                    }


                }));

                attachment.setFileName(nodeService.getProperty(transformed, ContentModel.PROP_NAME).toString());

                multipart.addBodyPart(attachment);
            }

            msg.setContent(multipart);
            Transport.send(msg);


            // cleanup the genereted pdfs

            for (int i = 0; i <= pds_to_be_deleted.size()-1; i++) {
                NodeRef n = pds_to_be_deleted.get(i);
                nodeService.deleteNode(n);
            }




        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public void sendEmailNoTransform(NodeRef[] attachmentNodeRefs, String authority, String body, String subject) {

        String to = authority;

        Properties props = new Properties();
        props.put("mail.smtp.host", "mail1.rm.dk");
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.port", "25");

        Session session = Session.getInstance(props);



        try {
            InternetAddress fromAddress = new InternetAddress("ps.o.faelles.post@rm.dk");
            InternetAddress toAddress = new InternetAddress(to);

            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(fromAddress);
            msg.setRecipient(Message.RecipientType.TO, toAddress);
            msg.setSubject(subject);
            msg.setSentDate(new Date());

            MimeBodyPart messagePart = new MimeBodyPart();
            messagePart.setText(body);


            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messagePart);



            for (int i=0; i <= attachmentNodeRefs.length-1;i++) {

                NodeRef attachmentNodeRef = attachmentNodeRefs[i];

                final MimeBodyPart attachment = new MimeBodyPart();
                attachment.setDataHandler(new DataHandler(new DataSource() {

                    public InputStream getInputStream() throws IOException {
                        ContentReader reader = contentService.getReader(attachmentNodeRef, ContentModel.PROP_CONTENT);
                        return reader.getContentInputStream();
                    }

                    public OutputStream getOutputStream() throws IOException {
                        throw new IOException("Read-only data");
                    }

                    public String getContentType() {
                        return contentService.getReader(attachmentNodeRef, ContentModel.PROP_CONTENT).getMimetype();
                    }

                    public String getName() {
                        return nodeService.getProperty(attachmentNodeRef, ContentModel.PROP_NAME).toString();
                    }


                }));

                attachment.setFileName(nodeService.getProperty(attachmentNodeRef, ContentModel.PROP_NAME).toString());
                multipart.addBodyPart(attachment);
            }

            msg.setContent(multipart);

            Transport.send(msg);

        } catch (MessagingException e) {
            e.printStackTrace();
        }

    }

    private NodeRef transform(NodeRef source) {

        String source_name = (String)nodeService.getProperty(source, ContentModel.PROP_NAME);

        NodeRef tmpFolder = siteService.getContainer(siteShortName, DatabaseModel.PROP_TMP);

        // Create new PDF
        Map<QName, Serializable> documentLibaryProps = new HashMap<>();
        documentLibaryProps.put(ContentModel.PROP_NAME, source_name + ".pdf");


        ChildAssociationRef pdf = nodeService.createNode(tmpFolder, ContentModel.ASSOC_CONTAINS,
                QName.createQName(ContentModel.USER_MODEL_URI, "thePDF"),
                ContentModel.TYPE_CONTENT, documentLibaryProps);


        ContentData contentData = (ContentData) nodeService.getProperty(source, ContentModel.PROP_CONTENT);
        String originalMimeType = contentData.getMimetype();

        ContentReader pptReader = contentService.getReader(source, ContentModel.PROP_CONTENT);
        ContentWriter pdfWriter = contentService.getWriter(pdf.getChildRef(), ContentModel.PROP_CONTENT, true);

        pdfWriter.setMimetype(MimetypeMap.MIMETYPE_PDF);

        ContentTransformer pptToPdfTransformer = contentService.getTransformer(originalMimeType, MimetypeMap.MIMETYPE_PDF);

        pptToPdfTransformer.transform(pptReader, pdfWriter);




        return pdf.getChildRef();
    }

    private void logEvent(NodeRef[] attachmentNodeRefs, String authority) {

        String username = authenticationService.getCurrentUserName();

        NodeRef user = personService.getPerson(username);

        if (!nodeService.hasAspect(user, DatabaseModel.ASPECT_SENDMAILLOGS)) {
            nodeService.addAspect(user, DatabaseModel.ASPECT_SENDMAILLOGS,null);

            org.json.simple.JSONObject tmp = new org.json.simple.JSONObject();
            org.json.simple.JSONArray tmpArray =  new org.json.simple.JSONArray();
            tmp.put("entries", tmpArray);

            nodeService.setProperty(user, DatabaseModel.PROP_ENTRIES, tmp.toString());
        }

        String content = (String) nodeService.getProperty(user, DatabaseModel.PROP_ENTRIES);


        org.json.simple.JSONObject j = null;
        JSONParser parser = new JSONParser();
        try {
            j = (org.json.simple.JSONObject) parser.parse(content);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        org.json.simple.JSONArray jsonArray = (org.json.simple.JSONArray)(j.get("entries"));

        if (jsonArray == null) {
            jsonArray = new org.json.simple.JSONArray();
        }

        org.json.simple.JSONObject entry = new org.json.simple.JSONObject();
        entry.put("time", System.currentTimeMillis());
        entry.put("authority",authority);


        org.json.simple.JSONArray nodeRefs = new org.json.simple.JSONArray();
        for (int i=0; i <= attachmentNodeRefs.length-1;i++) {
            nodeRefs.add("\"" + attachmentNodeRefs[i] + "\"");
        }

        entry.put("nodeRefs",nodeRefs);

        jsonArray.add(entry);
        j.put("entries", jsonArray);

        nodeService.setProperty(user, DatabaseModel.PROP_ENTRIES, j.toString());
    }

    public NodeRef addSignature(NodeRef attachment, NodeRef declaration) throws Exception {


        Signiture primarySignature = this.getPrimarySignature(declaration);
        Signiture secondarySignature = this.getSecondaryignature(declaration);



        ContentReader contentReader = contentService.getReader(attachment, ContentModel.PROP_CONTENT);
//        OdfDocument odt = OdfDocument.loadDocument(contentReader.getContentInputStream());

        TextDocument log_entires = TextDocument.loadDocument(contentReader.getContentInputStream());


        File filePrimary = new File("primary.jpg");
        File fileSecondary = new File("secondary.jpg");

        File backFile = new File("back");
        copyInputStreamToFile(primarySignature.image, filePrimary);

        // reseize image



        File newTestFile = new File("test.jpg");
        try {
            BufferedImage test = ImageIO.read(filePrimary);
            System.out.println("hvad er test");
            System.out.println(test);



//            test = Scalr.resize(test, 250);

            Thumbnails.of(filePrimary).scale(0.25).toFile(filePrimary);

            System.out.println("hvad er test nu efter scale");
            System.out.println(test);


            ImageIO.write(test, "jpg", newTestFile);

//            newTestFile = copyInputStreamToFile(newTestFile, newTestFile);

            System.out.println("hvad er newTestFile");
            System.out.println(newTestFile.toURI());
        }
        catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }







        Table table;

        if (secondarySignature != null) {
            table = log_entires.addTable(2,2);
        }
        else {
            table = log_entires.addTable(2,2);
        }

        Row row1 = table.getRowByIndex(0);
        Row row2 = table.getRowByIndex(1);


        Cell cRow1A = row1.getCellByIndex(0);



        Border border = new Border(Color.WHITE, 1.0, StyleTypeDefinitions.SupportedLinearMeasure.PT);
        cRow1A.setBorders(StyleTypeDefinitions.CellBordersType.NONE, border);

        cRow1A.setVerticalAlignment(StyleTypeDefinitions.VerticalAlignmentType.MIDDLE);


        System.out.println("hvad er cRow1a");
        System.out.println(cRow1A);

        System.out.println("hvad er newTestFile");
        System.out.println(newTestFile);
        System.out.println(newTestFile.toURI());

        try {
            cRow1A.setImage(filePrimary.toURI()).setHorizontalPosition(StyleTypeDefinitions.FrameHorizontalPosition.LEFT);
        }
        catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }



        Cell cRow2A = row2.getCellByIndex(0);
        cRow2A.setBorders(StyleTypeDefinitions.CellBordersType.NONE, border);
        cRow2A.addParagraph(primarySignature.text);

        if (secondarySignature != null) {
            copyInputStreamToFile(secondarySignature.image, fileSecondary);
            Thumbnails.of(fileSecondary).scale(0.25).toFile(fileSecondary);

            Cell cRow1B = row1.getCellByIndex(1);
            cRow1B.setBorders(StyleTypeDefinitions.CellBordersType.NONE, border);
            cRow1B.setImage(fileSecondary.toURI()).setHorizontalPosition(StyleTypeDefinitions.FrameHorizontalPosition.LEFT);

            Cell cRow2B = row2.getCellByIndex(1);
            cRow2B.setBorders(StyleTypeDefinitions.CellBordersType.NONE, border);
            cRow2B.addParagraph(secondarySignature.text);
        }
        else {
            Cell cRow1B = row1.getCellByIndex(1);
            cRow1B.setBorders(StyleTypeDefinitions.CellBordersType.NONE, border);
            Cell cRow2B = row2.getCellByIndex(1);
            cRow2B.setBorders(StyleTypeDefinitions.CellBordersType.NONE, border);
        }

        log_entires.save(backFile);

        NodeRef tmpFolder = siteService.getContainer(siteShortName, DatabaseModel.PROP_TMP);
        QName qName = QName.createQName(DatabaseModel.CONTENT_MODEL_URI, "test");
        Map<QName, Serializable> properties = new HashMap<>();
        ChildAssociationRef childAssociationRef = nodeService.createNode(tmpFolder, ContentModel.ASSOC_CONTAINS, qName, ContentModel.TYPE_CONTENT, properties);
        nodeService.setProperty(childAssociationRef.getChildRef(), ContentModel.PROP_NAME, childAssociationRef.getChildRef().getId());

        ContentWriter writer = contentService.getWriter(childAssociationRef.getChildRef(), ContentModel.PROP_CONTENT, true);
        writer.setMimetype("application/vnd.oasis.opendocument.text");
        writer.putContent(backFile);

        return childAssociationRef.getChildRef();

    }


    public Signiture getPrimarySignature(NodeRef declaration) throws JSONException {


        Signiture signiture = new Signiture();



        String doctor = (String) nodeService.getProperty(declaration, DatabaseModel.PROP_DOCTOR);
        String docUserName = propertyValuesBean.getUserNameByUser(doctor);
        NodeRef templateLibrary = siteService.getContainer("retspsyk", DatabaseModel.PROP_SIGNATURE_LIBRARY);
        NodeRef signatureNodeRef = nodeService.getChildByName(templateLibrary, ContentModel.ASSOC_CONTAINS, docUserName);

        if (signatureNodeRef != null) {
            ContentReader contentReader = contentService.getReader(signatureNodeRef, ContentModel.PROP_CONTENT);
            String text = (String) nodeService.getProperty(signatureNodeRef, DatabaseModel.PROP_SIGNATURE);

            signiture.image = contentReader.getContentInputStream();
            signiture.text = text;

            return signiture;
        }
        else {
            return null;
        }



    }

    public Signiture getSecondaryignature(NodeRef declaration) throws JSONException {


        // hent tiltrædes af læge

        Signiture signiture = new Signiture();

        String doctor = (String) nodeService.getProperty(declaration, PROP_SUPERVISINGDOCTOR);

        if ( (doctor != null) && !doctor.equals("null") )  {
            String superVisorUserName = propertyValuesBean.getUserNameByUser(doctor);

            NodeRef templateLibrary = siteService.getContainer("retspsyk", DatabaseModel.PROP_SIGNATURE_LIBRARY);
            NodeRef signatureNodeRef = nodeService.getChildByName(templateLibrary, ContentModel.ASSOC_CONTAINS, superVisorUserName);

            if (signatureNodeRef != null) {
                String text = (String) nodeService.getProperty(signatureNodeRef, DatabaseModel.PROP_SIGNATURE);

                ContentReader contentReader = contentService.getReader(signatureNodeRef, ContentModel.PROP_CONTENT);

                signiture.image = contentReader.getContentInputStream();
                signiture.text = text;

                return signiture;
            }
        }
        else {
            return null;
        }
        return null;
    }

    private static void copyInputStreamToFile(InputStream inputStream, File file)
            throws IOException {

        // append = false
        try (FileOutputStream outputStream = new FileOutputStream(file, false)) {
            int read;
            byte[] bytes = new byte[DEFAULT_BUFFER_SIZE];
            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
        }
    }

    public NodeRef getPreviewOfPdfWithSignature(NodeRef[] nodeRefs, NodeRef declaration) throws Exception {


        boolean found = false;
        int i = 0;
        NodeRef attachmentNodeRef = null;

        while (!found && i < nodeRefs.length) {
            NodeRef n = nodeRefs[i];
            if (nodeService.hasAspect(n, ASPECT_ADDSIGNATURE)) {
                attachmentNodeRef = n;
                found = true;
            }
            else {
                i = i +1;
            }
        }

        if (attachmentNodeRef != null) {
            NodeRef documentWithSignature = this.addSignature(attachmentNodeRef, declaration);
            return this.transform(documentWithSignature);
        }
        else {
            return null;
        }
    }


    public NodeRef getAttachmentToSign(NodeRef declaration) {
        List<ChildAssociationRef> childAssociationRef = nodeService.getChildAssocs(declaration);


        Iterator iterator = childAssociationRef.iterator();

        while (iterator.hasNext()) {

            NodeRef nodeRef = (NodeRef) iterator.next();

            if (nodeService.hasAspect(nodeRef, ASPECT_ADDSIGNATURE)) {
                return nodeRef;
            }
            else {
                iterator.next();
            }
        }
        return null;
    }

    class Signiture {
        public InputStream image;
        public String text;
    }

    public boolean signituresAvailable(NodeRef nodeRef, NodeRef[] attachedFiles) throws JSONException {

        String docUserName = "";
        String doctor = (String) nodeService.getProperty(nodeRef, DatabaseModel.PROP_DOCTOR);
        docUserName = propertyValuesBean.getUserNameByUser(doctor);

        String secondaryDoctorUserName = "";
        secondaryDoctorUserName = (String) nodeService.getProperty(nodeRef, PROP_SUPERVISINGDOCTOR);

        boolean userCheck = false;

        if (docUserName != null) {
            NodeRef templateLibrary = siteService.getContainer("retspsyk", DatabaseModel.PROP_SIGNATURE_LIBRARY);
            NodeRef signatureNodeRef = nodeService.getChildByName(templateLibrary, ContentModel.ASSOC_CONTAINS, docUserName);

            if ( (secondaryDoctorUserName != null) && (!secondaryDoctorUserName.equals("null")) ) {
                NodeRef secondarySignatureNodeRef = nodeService.getChildByName(templateLibrary, ContentModel.ASSOC_CONTAINS, docUserName);
                userCheck =  ((signatureNodeRef != null) && (secondarySignatureNodeRef != null));
            }
            else {
                userCheck = (signatureNodeRef != null);
            }

            // check if the selected files to send contains a nodeRef with the aspect

            boolean found = false;
            int i = 0;

            while (!found && i <= attachedFiles.length-1) {

                NodeRef attachmentNodeRef = attachedFiles[i];
                if (nodeService.hasAspect(attachmentNodeRef, ASPECT_ADDSIGNATURE)) {
                    found = true;
                }
                else {
                    i = i+1;
                }
            }
            return (userCheck && found);
        }
        else {
            return false;
        }
    }
}
