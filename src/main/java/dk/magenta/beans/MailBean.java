package dk.magenta.beans;


import dk.magenta.model.DatabaseModel;
import net.coobird.thumbnailator.Thumbnails;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.transform.ContentTransformer;
import org.alfresco.repo.jscript.ScriptLogger;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.io.IOUtils;
import org.jfree.chart.ChartUtilities;
import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeUtils;
import org.jodconverter.local.JodConverter;
import org.jodconverter.local.office.LocalOfficeManager;
import org.json.JSONException;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.odftoolkit.odfdom.type.Color;
import org.odftoolkit.simple.TextDocument;
import org.odftoolkit.simple.style.Border;
import org.odftoolkit.simple.style.StyleTypeDefinitions;
import org.odftoolkit.simple.table.Cell;
import org.odftoolkit.simple.table.Row;
import org.odftoolkit.simple.table.Table;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.imageio.ImageIO;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import java.awt.image.BufferedImage;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

import static dk.magenta.model.DatabaseModel.*;
import static org.odftoolkit.simple.style.StyleTypeDefinitions.HorizontalAlignmentType.LEFT;


import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;


//import org.apache.poi.hwpf.HWPFDocument;

public class MailBean {

    public static final int DEFAULT_BUFFER_SIZE = 8192;

    private NodeService nodeService;

    public void setFileFolderService(FileFolderService fileFolderService) {
        this.fileFolderService = fileFolderService;
    }

    private FileFolderService fileFolderService;

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    private SiteService siteService;

    public void setPropertyValuesBean(PropertyValuesBean propertyValuesBean) {
        this.propertyValuesBean = propertyValuesBean;
    }

    public void setTransformBean(TransformBean transformBean) {
        this.transformBean = transformBean;
    }

    private TransformBean transformBean;

    private PropertyValuesBean propertyValuesBean;

    private String siteShortName = "Retspsyk";

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    private PersonService personService;

    public void setWeeklyStatBean(WeeklyStatBean weeklyStatBean) {
        this.weeklyStatBean = weeklyStatBean;
    }

    private WeeklyStatBean weeklyStatBean;

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


    public void sendEmail(NodeRef[] attachmentNodeRefs, String authority, String body, String subject, boolean addSignature, NodeRef declaration, String bcc) throws Exception {


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

            MimeBodyPart bodyText = new MimeBodyPart();
            msg.addRecipients(Message.RecipientType.TO, bcc);

            MimeBodyPart messagePart = new MimeBodyPart();

            bodyText.setText(body);

            String htmlBody = "<p align=left><img src=\"cid:senny\"> </p>";

            messagePart.setContent(htmlBody, "text/html");



            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(bodyText);
            multipart.addBodyPart(messagePart);


            // second part (the image)



            NodeRef nodeRef_templateDocFolder = siteService.getContainer(DatabaseModel.TYPE_PSYC_SITENAME, DatabaseModel.PROP_TEMPLATE_LIBRARY);

            List<String> list = Arrays.asList(PROP_SIGNATUREIMAGE_FILENAME);
            List<ChildAssociationRef> children = nodeService.getChildrenByName(nodeRef_templateDocFolder, ContentModel.ASSOC_CONTAINS, list);

            NodeRef img = children.get(0).getChildRef();

            ContentReader reader = contentService.getReader(img, ContentModel.PROP_CONTENT);

            File tmp = new File("img.png");
            FileOutputStream out = new FileOutputStream(tmp);
            IOUtils.copy(reader.getContentInputStream(), out);

            MimeBodyPart signature = new MimeBodyPart();


            DataSource fds1 = new FileDataSource(tmp);

            signature.setDataHandler(new DataHandler(fds1));
            signature.addHeader("Content-ID","<senny>");

            multipart.addBodyPart(signature);

            for (int i=0; i <= attachmentNodeRefs.length-1;i++) {

                NodeRef attachmentNodeRef = attachmentNodeRefs[i];
                NodeRef transformed = null;
                String fileName = "";

                if (addSignature) {
                    if (nodeService.hasAspect(attachmentNodeRef, ASPECT_ADDSIGNATURE)) {
                        // return the nodeRef and  replace this with the one without the signature in the list of attachments
                        NodeRef documentWithSignature = this.addSignature(attachmentNodeRef, declaration);
                        pds_to_be_deleted.add(documentWithSignature);
                        transformed = this.transform(documentWithSignature);

                        // copy the pdf to the source of the attachmentNodeRef
                        NodeRef folder = nodeService.getPrimaryParent(attachmentNodeRef).getParentRef();
//                        NodeRef folder = fileFolderService.searchSimple(declaration, DatabaseModel.ATTR_DEFAULT_DECLARATION_FOLDER);

                        fileName = nodeService.getProperty(attachmentNodeRef, ContentModel.PROP_NAME).toString() + ".pdf";

                        // fixes name problem that causes errors in sending nodeRefs with signatures #48567
                        FileInfo newFile = fileFolderService.copy(transformed, folder, "underskrevet_" + fileName);
                    }
                    else {
                        // only transform odf, #47443
                        if (contentService.getReader(attachmentNodeRef, ContentModel.PROP_CONTENT).getMimetype().equals(MimetypeMap.MIMETYPE_OPENDOCUMENT_TEXT)) {
                            transformed = this.transform(attachmentNodeRef);
                            fileName = nodeService.getProperty(attachmentNodeRef, ContentModel.PROP_NAME).toString() + ".pdf";

                            // todo make a copy of the transformed odf - keep it in the same folder as the original ( attachmentNodeRef )
                            // copy the pdf to the source of the attachmentNodeRef
                            NodeRef folder = nodeService.getPrimaryParent(attachmentNodeRef).getParentRef();
                            FileInfo newFile = fileFolderService.copy(transformed, folder, fileName);
                        }
                        else {
                            transformed = attachmentNodeRef;
                            fileName = nodeService.getProperty(attachmentNodeRef, ContentModel.PROP_NAME).toString();
                        }



                    }
                }
                else {
                     // only transform odf, #47443
                    if (contentService.getReader(attachmentNodeRef, ContentModel.PROP_CONTENT).getMimetype().equals(MimetypeMap.MIMETYPE_OPENDOCUMENT_TEXT)) {
                        transformed = this.transform(attachmentNodeRef);
                        fileName = nodeService.getProperty(attachmentNodeRef, ContentModel.PROP_NAME).toString() + ".pdf";

                        // todo make a copy of the transformed odf - keep it in the same folder as the original ( attachmentNodeRef )
                        NodeRef folder = nodeService.getPrimaryParent(attachmentNodeRef).getParentRef();
                        FileInfo newFile = fileFolderService.copy(transformed, folder, fileName);

                    }
                    else {
                        transformed = attachmentNodeRef;
                        fileName = nodeService.getProperty(attachmentNodeRef, ContentModel.PROP_NAME).toString();
                    }



                }

                // only delete if a transformation took place, #47443
                if (contentService.getReader(attachmentNodeRef, ContentModel.PROP_CONTENT).getMimetype().equals(MimetypeMap.MIMETYPE_OPENDOCUMENT_TEXT)) {
                    pds_to_be_deleted.add(transformed);
                }

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

//                attachment.setFileName(nodeService.getProperty(transformed, ContentModel.PROP_NAME).toString());
                attachment.setFileName(fileName);
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

//    private NodeRef transform(NodeRef source) {
//
//        String source_name = (String)nodeService.getProperty(source, ContentModel.PROP_NAME);
//
//        NodeRef tmpFolder = siteService.getContainer(siteShortName, DatabaseModel.PROP_TMP);
//
//        // Create new PDF
//        Map<QName, Serializable> documentLibaryProps = new HashMap<>();
//        documentLibaryProps.put(ContentModel.PROP_NAME, source_name + ".pdf");
//
//
//        ChildAssociationRef pdf = nodeService.createNode(tmpFolder, ContentModel.ASSOC_CONTAINS,
//                QName.createQName(ContentModel.USER_MODEL_URI, "thePDF"),
//                ContentModel.TYPE_CONTENT, documentLibaryProps);
//
//
//        ContentData contentData = (ContentData) nodeService.getProperty(source, ContentModel.PROP_CONTENT);
//        String originalMimeType = contentData.getMimetype();
//
//        ContentReader pptReader = contentService.getReader(source, ContentModel.PROP_CONTENT);
//        ContentWriter pdfWriter = contentService.getWriter(pdf.getChildRef(), ContentModel.PROP_CONTENT, true);
//
//        pdfWriter.setMimetype(MimetypeMap.MIMETYPE_PDF);
//
//        ContentTransformer pptToPdfTransformer = contentService.getTransformer(originalMimeType, MimetypeMap.MIMETYPE_PDF);
//
//        pptToPdfTransformer.transform(pptReader, pdfWriter);
//        return pdf.getChildRef();
//    }

    private NodeRef transform(NodeRef source) {
        NodeRef tmpFolder = siteService.getContainer(siteShortName, DatabaseModel.PROP_TMP);
        try {
            return transformBean.transformODTtoPDF(source, tmpFolder);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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
        TextDocument log_entires = TextDocument.loadDocument(contentReader.getContentInputStream());


        File filePrimary = new File("primary.jpg");
        File fileSecondary = new File("secondary.jpg");

        File backFile = new File("back");
        copyInputStreamToFile(primarySignature.image, filePrimary);

//        File newTestFile = new File("test.jpg");
        try {
            BufferedImage test = ImageIO.read(filePrimary);

            Thumbnails.of(filePrimary).scale(0.25).toFile(filePrimary);

//            ImageIO.write(test, "jpg", newTestFile);
        }
        catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }







        Table table;

        if (secondarySignature != null) {
            table = log_entires.addTable(6,1);
        }
        else {
            table = log_entires.addTable(2,1);
        }

        Row row1 = table.getRowByIndex(0);
        Row row2 = table.getRowByIndex(1);
        Row row3 = table.getRowByIndex(2);
        Row row4 = table.getRowByIndex(3);
        Row row5 = table.getRowByIndex(4);
        Row row6 = table.getRowByIndex(5);
        Row row7 = table.getRowByIndex(6);

        Cell cRow1A = row1.getCellByIndex(0);

        Border border = new Border(Color.WHITE, 1.0, StyleTypeDefinitions.SupportedLinearMeasure.PT);
        cRow1A.setBorders(StyleTypeDefinitions.CellBordersType.NONE, border);

        cRow1A.setVerticalAlignment(StyleTypeDefinitions.VerticalAlignmentType.MIDDLE);

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

            // add text, "Tiltrædes af"

            Cell cRow3B = row3.getCellByIndex(0);
            cRow3B.setBorders(StyleTypeDefinitions.CellBordersType.NONE, border);

            Cell cRow4B = row4.getCellByIndex(0);
            cRow4B.setBorders(StyleTypeDefinitions.CellBordersType.NONE, border);


            Cell cRow5B = row5.getCellByIndex(0);
            cRow5B.setBorders(StyleTypeDefinitions.CellBordersType.NONE, border);
            cRow5B.addParagraph("Tiltrædes af: ");

            Cell cRow6B = row6.getCellByIndex(0);
            cRow6B.setBorders(StyleTypeDefinitions.CellBordersType.NONE, border);
            cRow6B.setImage(fileSecondary.toURI()).setHorizontalPosition(StyleTypeDefinitions.FrameHorizontalPosition.LEFT);

            Cell cRow7B = row7.getCellByIndex(0);
            cRow7B.setBorders(StyleTypeDefinitions.CellBordersType.NONE, border);
            cRow7B.addParagraph(secondarySignature.text);
        }
        else {
            Cell cRow1B = row1.getCellByIndex(0);
            cRow1B.setBorders(StyleTypeDefinitions.CellBordersType.NONE, border);
            Cell cRow2B = row2.getCellByIndex(0);
            cRow2B.setBorders(StyleTypeDefinitions.CellBordersType.NONE, border);
            Cell cRow3B = row3.getCellByIndex(0);
            cRow3B.setBorders(StyleTypeDefinitions.CellBordersType.NONE, border);
            Cell cRow4B = row4.getCellByIndex(0);
            cRow4B.setBorders(StyleTypeDefinitions.CellBordersType.NONE, border);
            Cell cRow5B = row5.getCellByIndex(0);
            cRow5B.setBorders(StyleTypeDefinitions.CellBordersType.NONE, border);
            Cell cRow6B = row6.getCellByIndex(0);
            cRow6B.setBorders(StyleTypeDefinitions.CellBordersType.NONE, border);
            Cell cRow7B = row7.getCellByIndex(0);
            cRow7B.setBorders(StyleTypeDefinitions.CellBordersType.NONE, border);
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

            NodeRef tmp =  this.transform(documentWithSignature);

            // make sure to delete the node again
            nodeService.addAspect(tmp,ASPECT_TMP,null);
            return tmp;


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

    public NodeRef doChart(String requestedYear)  {



        XYSeries sent = weeklyStatBean.getWeekNodesForYearChartSent(requestedYear);
        XYSeries sentAkk = weeklyStatBean.getWeekNodesForYearChartSentAkk(requestedYear);


        XYSeries received = weeklyStatBean.getWeekNodesForYearChartReceived(requestedYear);
        XYSeries receivedAkk = weeklyStatBean.getWeekNodesForYearChartReceivedAkk(requestedYear);

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(sent);
        dataset.addSeries(sentAkk);

        dataset.addSeries(received);
        dataset.addSeries(receivedAkk);


        JFreeChart chart = ChartFactory.createXYLineChart(
                "----",
                "Uge",
                "Antal",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        XYPlot plot = chart.getXYPlot();

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, java.awt.Color.RED);
        renderer.setSeriesStroke(0, new BasicStroke(2.0f));
        renderer.setSeriesPaint(1, java.awt.Color.BLUE);
        renderer.setSeriesStroke(1, new BasicStroke(2.0f));

        renderer.setSeriesPaint(2, java.awt.Color.GREEN);
        renderer.setSeriesStroke(2, new BasicStroke(2.0f));
        renderer.setSeriesPaint(3, java.awt.Color.BLACK);
        renderer.setSeriesStroke(3, new BasicStroke(2.0f));

        plot.setRenderer(renderer);
        plot.setBackgroundPaint((Paint) java.awt.Color.WHITE);

        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint((Paint) java.awt.Color.BLACK);

        plot.setDomainGridlinesVisible(true);
        plot.setDomainGridlinePaint((Paint) java.awt.Color.BLACK);

        chart.getLegend().setFrame(BlockBorder.NONE);

        chart.setTitle(new TextTitle("Rapport pr uge for " + requestedYear,
                        new Font("Serif", java.awt.Font.BOLD, 18)
                )
        );

        OutputStream out = null;
        File f = new File("uge.png");
        try {
            out = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            ChartUtilities.writeChartAsPNG(out, chart,800,1000);

            NodeRef tmpFolder = siteService.getContainer(siteShortName, DatabaseModel.PROP_TMP);
            QName qName = QName.createQName(DatabaseModel.CONTENT_MODEL_URI, "test");
            Map<QName, Serializable> properties = new HashMap<>();
            properties.put(ContentModel.PROP_NAME, "graf.png");
            ChildAssociationRef childAssociationRef = nodeService.createNode(tmpFolder, ContentModel.ASSOC_CONTAINS, qName, ContentModel.TYPE_CONTENT, properties);
            nodeService.setProperty(childAssociationRef.getChildRef(), ContentModel.PROP_NAME, childAssociationRef.getChildRef().getId());

            ContentWriter writer = contentService.getWriter(childAssociationRef.getChildRef(), ContentModel.PROP_CONTENT, true);

            writer.setMimetype("image/png");
            writer.putContent(f);

            return childAssociationRef.getChildRef();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }




}
