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



    public String populateDocument(NodeRef declaration, String type, String retten, String dato) throws Exception{


        String documentNodeRef = null;


        if (type.equals(DatabaseModel.PROP_TEMPLATE_DOC_KENDELSE)) {

            NodeRef nodeRef_templateFolder = siteService.getContainer(DatabaseModel.TYPE_PSYC_SITENAME, DatabaseModel.PROP_TEMPLATE_LIBRARY);

            List<String> list = Arrays.asList(DatabaseModel.PROP_TEMPLATE_DOC_KENDELSE);
            List<ChildAssociationRef> children = nodeService.getChildrenByName(nodeRef_templateFolder, ContentModel.ASSOC_CONTAINS, list);

            NodeRef template_doc = children.get(0).getChildRef();

            documentNodeRef = this.generateOfferLetterDocumentKendelse(template_doc, declaration, retten, dato);
        }

        else if (type.equals(DatabaseModel.PROP_TEMPLATE_DOC_SAMTYKKE)) {

            NodeRef nodeRef_templateFolder = siteService.getContainer(DatabaseModel.TYPE_PSYC_SITENAME, DatabaseModel.PROP_TEMPLATE_LIBRARY);

            List<String> list = Arrays.asList(DatabaseModel.PROP_TEMPLATE_DOC_SAMTYKKE);
            List<ChildAssociationRef> children = nodeService.getChildrenByName(nodeRef_templateFolder, ContentModel.ASSOC_CONTAINS, list);

            NodeRef template_doc = children.get(0).getChildRef();

            documentNodeRef = this.generateOfferLetterDocumentSamtykke(template_doc, declaration);
        }

        return documentNodeRef;
    }

    private DeclarationInfo getProperties(NodeRef declaration) {

        DeclarationInfo info = new DeclarationInfo();
        info.cpr = (String)nodeService.getProperty(declaration, DatabaseModel.PROP_CPR);
        info.cpr = info.cpr.substring(0,6) + "-" + info.cpr.substring(6,10);

        info.fornavn = (String)nodeService.getProperty(declaration, DatabaseModel.PROP_FIRST_NAME);
        info.efternavn = (String)nodeService.getProperty(declaration, DatabaseModel.PROP_LAST_NAME);

        info.adresse = (String)nodeService.getProperty(declaration, DatabaseModel.PROP_ADDRESS);
        info.postnr = String.valueOf(((Integer)nodeService.getProperty(declaration, DatabaseModel.PROP_POSTCODE)));
        info.by = (String)nodeService.getProperty(declaration, DatabaseModel.PROP_CITY);






        info.laege = (String)nodeService.getProperty(declaration, DatabaseModel.PROP_DOCTOR);

        int sagsnummer = (int)nodeService.getProperty(declaration, DatabaseModel.PROP_CASE_NUMBER);
        info.sagsnr = String.valueOf(sagsnummer);

        String journalnummer = (String)nodeService.getProperty(declaration, DatabaseModel.PROP_JOURNALNUMMER);
        info.journalnummer = journalnummer;



        info.politikreds = (String)nodeService.getProperty(declaration, DatabaseModel.PROP_REFERING_AGENCY);

        Date receivedDate = (Date)nodeService.getProperty(declaration, DatabaseModel.PROP_CREATION_DATE);
        Calendar cal = Calendar.getInstance();
        int year;
        int day;
        int month;
        cal.setTime(receivedDate);
        year = cal.get(Calendar.YEAR);
        day = cal.get(Calendar.DATE);
        month = (cal.get(Calendar.MONTH)+1);
        info.oprettetdato  = day + "." + month + "." + year;



        return info;
    }

    public String generateOfferLetterDocumentKendelse(NodeRef templateDoc, NodeRef declaration, String retten, String dato) throws Exception {

        DeclarationInfo info = this.getProperties(declaration);
        System.out.println("hvad er cpr:  " + info.cpr);
        System.out.println("hvad er fornavn:  " + info.fornavn);
        System.out.println("hvad er efternavn:  " + info.efternavn);
        System.out.println("hvad er post:  " + info.postnr);
        System.out.println("hvad er by:  " + info.by);
        System.out.println("politikreds:  " + retten);
        System.out.println("kendelsesdato:  " + dato);
        System.out.println("amlb:  " + info.ambldato);
        System.out.println("laege:  " + info.laege);
        System.out.println("journalnummer:  " + info.journalnummer);

        NodeRef nodeRef_templateFolder = siteService.getContainer(DatabaseModel.TYPE_PSYC_SITENAME, DatabaseModel.PROP_TEMPLATE_LIBRARY);


        ContentReader contentReader = contentService.getReader(templateDoc, ContentModel.PROP_CONTENT);
        TextDocument templateDocument = TextDocument.loadDocument(contentReader.getContentInputStream());

        VariableField candidateVar = templateDocument.getVariableFieldByName("cpr");
        candidateVar.updateField(info.cpr, null);

        VariableField navn = templateDocument.getVariableFieldByName("Navn");
        navn.updateField(info.fornavn + " " + info.efternavn, null);

        VariableField retteni = templateDocument.getVariableFieldByName("retteni");
        retteni.updateField(retten, null);

        VariableField kendelsesdato = templateDocument.getVariableFieldByName("kendelsesdato");
        kendelsesdato.updateField(dato, null);


        VariableField politikreds = templateDocument.getVariableFieldByName("politikreds");
        politikreds.updateField(info.politikreds, null);

        VariableField kunnavn = templateDocument.getVariableFieldByName("kunnavn");
        kunnavn.updateField(info.fornavn + " " + info.efternavn, null);

        VariableField patientnr = templateDocument.getVariableFieldByName("patientnr");
        patientnr.updateField(info.sagsnr, null);

        VariableField journalnr = templateDocument.getVariableFieldByName("journalnr");
        journalnr.updateField(info.journalnummer, null);

        VariableField modtagetdato = templateDocument.getVariableFieldByName("modtagetdato");
        modtagetdato.updateField(info.oprettetdato, null);


        // make the new document below the case

        FileInfo newFile = fileFolderService.create(declaration, info.cpr.substring(0,7) + "erklæring.odt", ContentModel.TYPE_CONTENT);


        ContentWriter writer = contentService.getWriter(newFile.getNodeRef(), ContentModel.PROP_CONTENT, true);
        writer.setMimetype("application/vnd.oasis.opendocument.text");

        File f = new File("tmp");

        templateDocument.save(f);
        writer.putContent(f);


        return newFile.getNodeRef().getId();
    }

    public String generateOfferLetterDocumentSamtykke(NodeRef templateDoc, NodeRef declaration) throws Exception {

        DeclarationInfo info = this.getProperties(declaration);
        System.out.println("hvad er cpr:  " + info.cpr);
        System.out.println("hvad er fornavn:  " + info.fornavn);
        System.out.println("hvad er efternavn:  " + info.efternavn);
        System.out.println("hvad er post:  " + info.postnr);
        System.out.println("hvad er by:  " + info.by);
        System.out.println("amlb:  " + info.ambldato);
        System.out.println("laege:  " + info.laege);

        NodeRef nodeRef_templateFolder = siteService.getContainer(DatabaseModel.TYPE_PSYC_SITENAME, DatabaseModel.PROP_TEMPLATE_LIBRARY);


        ContentReader contentReader = contentService.getReader(templateDoc, ContentModel.PROP_CONTENT);
        TextDocument templateDocument = TextDocument.loadDocument(contentReader.getContentInputStream());

        VariableField candidateVar = templateDocument.getVariableFieldByName("cpr");
        candidateVar.updateField(info.cpr, null);

        VariableField navn = templateDocument.getVariableFieldByName("Navn");
        navn.updateField(info.fornavn + " " + info.efternavn, null);


        VariableField politikreds = templateDocument.getVariableFieldByName("politikreds");
        politikreds.updateField(info.politikreds, null);

        VariableField kunnavn = templateDocument.getVariableFieldByName("kunnavn");
        kunnavn.updateField(info.fornavn + " " + info.efternavn, null);

        VariableField patientnr = templateDocument.getVariableFieldByName("patientnr");
        patientnr.updateField(info.sagsnr, null);

        VariableField journalnr = templateDocument.getVariableFieldByName("journalnr");
        journalnr.updateField(info.journalnummer, null);

        VariableField modtagetdato = templateDocument.getVariableFieldByName("modtagetdato");
        modtagetdato.updateField(info.oprettetdato, null);


        // make the new document below the case

        FileInfo newFile = fileFolderService.create(declaration, info.cpr.substring(0,7) + "erklæring.odt", ContentModel.TYPE_CONTENT);


        ContentWriter writer = contentService.getWriter(newFile.getNodeRef(), ContentModel.PROP_CONTENT, true);
        writer.setMimetype("application/vnd.oasis.opendocument.text");

        File f = new File("tmp");

        templateDocument.save(f);
        writer.putContent(f);

        return newFile.getNodeRef().getId();
    }

    private class DeclarationInfo {
        public String cpr;
        public String fornavn;
        public String efternavn;
        public String adresse;
        public String postnr;
        public String by;
        public String ambldato;
        public String laege;
        public String sagsnr;
        public String politikreds;
        public String oprettetdato;
        public String journalnummer;
    }
}