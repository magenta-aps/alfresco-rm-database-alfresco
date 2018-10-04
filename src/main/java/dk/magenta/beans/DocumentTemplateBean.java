package dk.magenta.beans;

import dk.magenta.model.DatabaseModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.transform.ContentTransformer;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.odftoolkit.simple.TextDocument;
import org.odftoolkit.simple.common.field.VariableField;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.*;
import java.util.*;

public class DocumentTemplateBean {

    private NodeService nodeService;

    public void setFileFolderService(FileFolderService fileFolderService) {
        this.fileFolderService = fileFolderService;
    }

    private FileFolderService fileFolderService;

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    private SiteService siteService;

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




    public void populateDocument(NodeRef declaration, String type, String retten, String dato) {
        try {


        if (type.equals(DatabaseModel.PROP_TEMPLATE_DOC_KENDELSE)) {

            NodeRef nodeRef_templateFolder = siteService.getContainer(DatabaseModel.TYPE_PSYC_SITENAME, DatabaseModel.PROP_TEMPLATE_LIBRARY);

            List<String> list = Arrays.asList(DatabaseModel.PROP_TEMPLATE_DOC_KENDELSE);
            List<ChildAssociationRef> children = nodeService.getChildrenByName(nodeRef_templateFolder, ContentModel.ASSOC_CONTAINS, list);

            NodeRef template_doc = children.get(0).getChildRef();

            this.generateOfferLetterDocumentKendelse(template_doc, declaration);
        }

        else if (type.equals(DatabaseModel.PROP_TEMPLATE_DOC_SAMTYKKE)) {

            NodeRef nodeRef_templateFolder = siteService.getContainer(DatabaseModel.TYPE_PSYC_SITENAME, DatabaseModel.PROP_TEMPLATE_LIBRARY);

            List<String> list = Arrays.asList(DatabaseModel.PROP_TEMPLATE_DOC_SAMTYKKE);
            List<ChildAssociationRef> children = nodeService.getChildrenByName(nodeRef_templateFolder, ContentModel.ASSOC_CONTAINS, list);

            NodeRef template_doc = children.get(0).getChildRef();

            this.generateOfferLetterDocumentSamtykke(template_doc, declaration);
        }




        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private DeclarationInfo getProperties(NodeRef declaration) {

        DeclarationInfo info = new DeclarationInfo();
        info.cpr = (String)nodeService.getProperty(declaration, DatabaseModel.PROP_CPR);
        return info;
    }

    public void generateOfferLetterDocumentKendelse(NodeRef templateDoc, NodeRef declaration) throws Exception {

        DeclarationInfo info = this.getProperties(declaration);
        System.out.println("hvad er cpr:  " + info.cpr);
        System.out.println("hvad er cpr:  " + info.cpr);
        System.out.println("hvad er cpr:  " + info.cpr);
        System.out.println("hvad er cpr:  " + info.cpr);
        System.out.println("hvad er cpr:  " + info.cpr);
        System.out.println("hvad er cpr:  " + info.cpr);

        NodeRef nodeRef_templateFolder = siteService.getContainer(DatabaseModel.TYPE_PSYC_SITENAME, DatabaseModel.PROP_TEMPLATE_LIBRARY);


        ContentReader contentReader = contentService.getReader(templateDoc, ContentModel.PROP_CONTENT);
        TextDocument templateDocument = TextDocument.loadDocument(contentReader.getContentInputStream());

        VariableField candidateVar = templateDocument.getVariableFieldByName("cpr");
        candidateVar.updateField(info.cpr, null);

//        VariableField ptr = templateDocument.getVariableFieldByName("ptnr");
//
//        System.out.println("hvad er template" + ptr);
//
//        ptr.updateField("237498478239", null);


        // make the new document below the case

        FileInfo newFile = fileFolderService.create(declaration, "erklæring.odt", ContentModel.TYPE_CONTENT);


        ContentWriter writer = contentService.getWriter(newFile.getNodeRef(), ContentModel.PROP_CONTENT, true);
        writer.setMimetype(" application/vnd.oasis.opendocument.text");

        File f = new File("tmp");

        templateDocument.save(f);
        writer.putContent(f);
    }

    public void generateOfferLetterDocumentSamtykke(NodeRef templateDoc, NodeRef declaration) throws Exception {

        NodeRef nodeRef_templateFolder = siteService.getContainer(DatabaseModel.TYPE_PSYC_SITENAME, DatabaseModel.PROP_TEMPLATE_LIBRARY);


        ContentReader contentReader = contentService.getReader(templateDoc, ContentModel.PROP_CONTENT);
        TextDocument templateDocument = TextDocument.loadDocument(contentReader.getContentInputStream());

        VariableField candidateVar = templateDocument.getVariableFieldByName("cpr");
        candidateVar.updateField("231287-3871", null);

//        VariableField ptr = templateDocument.getVariableFieldByName("ptnr");
//
//        System.out.println("hvad er template" + ptr);
//
//        ptr.updateField("237498478239", null);


        // make the new document below the case

        FileInfo newFile = fileFolderService.create(declaration, "erklæring.odt", ContentModel.TYPE_CONTENT);


        ContentWriter writer = contentService.getWriter(newFile.getNodeRef(), ContentModel.PROP_CONTENT, true);
        writer.setMimetype(" application/vnd.oasis.opendocument.text");

        File f = new File("tmp");

        templateDocument.save(f);
        writer.putContent(f);




    }

    private class DeclarationInfo {
        public String cpr;
    }
}