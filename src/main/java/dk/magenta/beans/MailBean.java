package dk.magenta.beans;

import dk.magenta.model.DatabaseModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.transform.ContentTransformer;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.download.DownloadService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;
import org.apache.solr.common.util.Hash;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.activation.DataSource;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.*;


import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Date;
import java.util.Properties;

public class MailBean {

    private NodeService nodeService;

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    private SiteService siteService;

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


    public void sendEmail(NodeRef[] attachmentNodeRefs, String authority, String body, String subject) {


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

                 NodeRef transformed = this.transform(attachmentNodeRef);

                pds_to_be_deleted.add(transformed);

                final MimeBodyPart attachment = new MimeBodyPart();
                attachment.setDataHandler(new DataHandler(new DataSource() {

                    public InputStream getInputStream() throws IOException {
                        ContentReader reader = contentService.getReader(transformed, ContentModel.PROP_CONTENT);
                        return reader.getContentInputStream();
                    }

                    public OutputStream getOutputStream() throws IOException {
                        throw new IOException("Read-only data");
                    }

                    public String getContentType() {
                        return contentService.getReader(transformed, ContentModel.PROP_CONTENT).getMimetype();
                    }

                    public String getName() {
                        return nodeService.getProperty(transformed, ContentModel.PROP_NAME).toString();
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
}
