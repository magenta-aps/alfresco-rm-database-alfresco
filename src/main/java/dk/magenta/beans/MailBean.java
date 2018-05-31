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
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    public void sendEmail(NodeRef[] attachmentNodeRefs, String body, String subject, String authority) {
        // Defines the E-Mail information.
        String from = "retspsykiatrisk.ambulatorium@ps.rm.dk ";
        String to = authority;


        // Creates a Session with the following properties.
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.port", "587");


        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication("xxx", "xxx");
                    }
                });


        //Session session = Session.getDefaultInstance(props);

        try {
            InternetAddress fromAddress = new InternetAddress(from);
            InternetAddress toAddress = new InternetAddress(to);

            // Create an Internet mail msg.
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(fromAddress);
            msg.setRecipient(Message.RecipientType.TO, toAddress);
            msg.setSubject(subject);
            msg.setSentDate(new Date());

            // Set the email msg text.
            MimeBodyPart messagePart = new MimeBodyPart();
            messagePart.setText(body);

            // Set the email attachment file

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messagePart);


            // the loop

            for (int i=0; i <= attachmentNodeRefs.length-1;i++) {

                 NodeRef attachmentNodeRef = attachmentNodeRefs[i];

                 NodeRef transformed = this.transform(attachmentNodeRef);


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

                // Create Multipart E-Mail.

                multipart.addBodyPart(attachment);
            }

            msg.setContent(multipart);

            Transport.send(msg, "xxx", "xxx*");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private NodeRef transform(NodeRef source) {

        String source_name = (String)nodeService.getProperty(source, ContentModel.PROP_NAME);

        // Create new PDF
        Map<QName, Serializable> documentLibaryProps = new HashMap<>();
        documentLibaryProps.put(ContentModel.PROP_NAME, source_name + ".pdf");

        NodeRef parent = nodeService.getPrimaryParent(source).getParentRef();

        ChildAssociationRef pdf = nodeService.createNode(parent, ContentModel.ASSOC_CONTAINS,
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








}