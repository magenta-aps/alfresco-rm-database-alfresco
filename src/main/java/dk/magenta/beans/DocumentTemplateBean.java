package dk.magenta.beans;

import dk.magenta.model.DatabaseModel;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;
import org.json.JSONException;
import org.odftoolkit.simple.TextDocument;
import org.odftoolkit.simple.common.field.VariableField;
import java.io.*;
import java.util.*;

import static dk.magenta.model.DatabaseModel.*;

public class DocumentTemplateBean {

    public static final int DEFAULT_BUFFER_SIZE = 8192;

    private NodeService nodeService;

    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    private PermissionService permissionService;

    public void setPropertyValuesBean(PropertyValuesBean propertyValuesBean) {
        this.propertyValuesBean = propertyValuesBean;
    }

    private PropertyValuesBean propertyValuesBean;

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

            List<String> list = Arrays.asList(((nodeService.hasAspect(declaration, DatabaseModel.ASPECT_BUA)) ? DatabaseModel.PROP_TEMPLATE_DOC_KENDELSE_FILENAME_BUA : DatabaseModel.PROP_TEMPLATE_DOC_KENDELSE_FILENAME));
            List<ChildAssociationRef> children = nodeService.getChildrenByName(nodeRef_templateFolder, ContentModel.ASSOC_CONTAINS, list);

            NodeRef template_doc = children.get(0).getChildRef();

            documentNodeRef = this.generateOfferLetterDocumentKendelse(template_doc, declaration, retten, dato);

            // add signiture aspect
            NodeRef newDoc = new NodeRef("workspace://SpacesStore/" + documentNodeRef);
            Map<QName, Serializable> aspectProps = new HashMap<>();
            nodeService.addAspect(newDoc, ASPECT_ADDSIGNATURE, aspectProps);
        }

        else if (type.equals(DatabaseModel.PROP_TEMPLATE_DOC_SAMTYKKE)) {

            NodeRef nodeRef_templateFolder = siteService.getContainer(DatabaseModel.TYPE_PSYC_SITENAME, DatabaseModel.PROP_TEMPLATE_LIBRARY);

            List<String> list = Arrays.asList(((nodeService.hasAspect(declaration, DatabaseModel.ASPECT_BUA)) ? DatabaseModel.PROP_TEMPLATE_DOC_SAMTYKKE_FILENAME_BUA : DatabaseModel.PROP_TEMPLATE_DOC_SAMTYKKE_FILENAME));
            List<ChildAssociationRef> children = nodeService.getChildrenByName(nodeRef_templateFolder, ContentModel.ASSOC_CONTAINS, list);

            NodeRef template_doc = children.get(0).getChildRef();

            documentNodeRef = this.generateOfferLetterDocumentSamtykke(template_doc, declaration);

            // add signiture aspect
            NodeRef newDoc = new NodeRef("workspace://SpacesStore/" + documentNodeRef);
            Map<QName, Serializable> aspectProps = new HashMap<>();
            nodeService.addAspect(newDoc, ASPECT_ADDSIGNATURE, aspectProps);
        }



        if (nodeService.hasAspect(declaration, DatabaseModel.ASPECT_BUA)) {
            NodeRef nodeRef_templateFolder = siteService.getContainer(DatabaseModel.TYPE_PSYC_SITENAME, DatabaseModel.PROP_TEMPLATE_LIBRARY);
            List<String> list = Arrays.asList((nodeService.hasAspect(declaration, DatabaseModel.ASPECT_BUA)) ? DatabaseModel.PROP_PSYCOLOGICALDOCUMENT_BUA : DatabaseModel.PROP_PSYCOLOGICALDOCUMENT);
            List<ChildAssociationRef> children = nodeService.getChildrenByName(nodeRef_templateFolder, ContentModel.ASSOC_CONTAINS, list);

            NodeRef template_doc = children.get(0).getChildRef();

            NodeRef psycologicalDocument = this.generatePsycologicalDocumen(template_doc, declaration);

            permissionService.setPermission(psycologicalDocument, DatabaseModel.GROUP_ALLOWEDTODELETE, PermissionService.DELETE_NODE, true);
        }
        else {
            NodeRef nodeRef_templateFolder = siteService.getContainer(DatabaseModel.TYPE_PSYC_SITENAME, DatabaseModel.PROP_TEMPLATE_LIBRARY);
            List<String> list = Arrays.asList(DatabaseModel.PROP_SAMTYKKE_TDL_KONTAKT);
            List<ChildAssociationRef> children = nodeService.getChildrenByName(nodeRef_templateFolder, ContentModel.ASSOC_CONTAINS, list);

            NodeRef template_doc = children.get(0).getChildRef();

            NodeRef samtykkeTidlKontaktDocument = this.generateSamtykkeDocument(template_doc, declaration);
            permissionService.setPermission(samtykkeTidlKontaktDocument, DatabaseModel.GROUP_ALLOWEDTODELETE, PermissionService.DELETE_NODE, true);
        }

        permissionService.setPermission(new NodeRef("workspace://SpacesStore/" + documentNodeRef), DatabaseModel.GROUP_ALLOWEDTODELETE, PermissionService.DELETE_NODE, true);

//        ContentReader contentReaderODT = contentService.getReader(newDoc, ContentModel.PROP_CONTENT);
//        OdfDocument odt = OdfDocument.loadDocument(contentReaderODT.getContentInputStream());
//
//        File file = new File("tmp.jpg");
//        File backFile = new File("back");
//        ContentReader imageReader = contentService.getReader(new NodeRef("workspace://SpacesStore/1d3eda73-5075-4c65-9e46-30c30b881d5e"), ContentModel.PROP_CONTENT);
//        copyInputStreamToFile(imageReader.getContentInputStream(), file);
//        odt.newImage(file.toURI());
//        odt.save(backFile);
//
//        ContentWriter contentWriter = contentService.getWriter(newDoc, ContentModel.PROP_CONTENT, true);
//        contentWriter.putContent(backFile);


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

        String strindDay = (day <= 9) ? "0" + day : String.valueOf(day);
        String strindMonth = (month <= 9) ? "0" + month : String.valueOf(month);

        info.oprettetdato  = strindDay + "." + strindMonth + "." + year;
//        info.oprettetdato  = (day <= 9) ? "0" + day : String.valueOf(day) + "." + (month <= 9) ? ("0" + String.valueOf(month) ? month + "." + year;

        if (nodeService.getProperty(declaration, DatabaseModel.PROP_DECLARATION_DATE) != null) {
            Date declarationDate = (Date)nodeService.getProperty(declaration, DatabaseModel.PROP_DECLARATION_DATE);

            cal = Calendar.getInstance();

            cal.setTime(declarationDate);
            year = cal.get(Calendar.YEAR);
            day = cal.get(Calendar.DATE);
            month = (cal.get(Calendar.MONTH)+1);

            String declarationDay = (day <= 9) ? "0" + day : String.valueOf(day);
            String declarationMonthMonth = (month <= 9) ? "0" + month : String.valueOf(month);

            info.erklaeringAfgivet = declarationDay + "." + month + "." + year;

        }

        if (nodeService.getProperty(declaration, PROP_REFERING_AGENCY) != null) {

            // todo her skal være titel, adresse, post, by som ligger som et objekt i nedenstående property

            // map key med value i property



            String key = (String)nodeService.getProperty(declaration, PROP_REFERING_AGENCY);

            try {
                info.henvisendeInstans = propertyValuesBean.getReferingAgentByKey(key);

                System.out.println("hvad er info");
                System.out.println(info.henvisendeInstans);

            } catch (JSONException e) {
                e.printStackTrace();
            }

//
        }

        info.doctor = (String)nodeService.getProperty(declaration, PROP_DOCTOR);
        return info;
    }

    public String generateOfferLetterDocumentKendelse(NodeRef templateDoc, NodeRef declaration, String retten, String dato) throws Exception {

        DeclarationInfo info = this.getProperties(declaration);
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

        // #43832 - declarations should be saved in the new folder, Erklaering og Psykologisk rapport
        NodeRef folder = fileFolderService.searchSimple(declaration, DatabaseModel.ATTR_DEFAULT_DECLARATION_FOLDER);
        FileInfo newFile = fileFolderService.create(folder, info.cpr.substring(0,7) + "_erklaering.odt", ContentModel.TYPE_CONTENT);


        ContentWriter writer = contentService.getWriter(newFile.getNodeRef(), ContentModel.PROP_CONTENT, true);
        writer.setMimetype("application/vnd.oasis.opendocument.text");

        File f = new File("tmp");

        templateDocument.save(f);
        writer.putContent(f);

        return newFile.getNodeRef().getId();
    }

    public String generateOfferLetterDocumentSamtykke(NodeRef templateDoc, NodeRef declaration) throws Exception {

        DeclarationInfo info = this.getProperties(declaration);


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

        // #43832 - declarations should be saved in the new folder, Erklaering og Psykologisk rapport
        NodeRef folder = fileFolderService.searchSimple(declaration, DatabaseModel.ATTR_DEFAULT_DECLARATION_FOLDER);

        FileInfo newFile = fileFolderService.create(folder, info.cpr.substring(0,6) + "_erklaering.odt", ContentModel.TYPE_CONTENT);


        ContentWriter writer = contentService.getWriter(newFile.getNodeRef(), ContentModel.PROP_CONTENT, true);
        writer.setMimetype("application/vnd.oasis.opendocument.text");

        File f = new File("tmp");

        templateDocument.save(f);
        writer.putContent(f);

        return newFile.getNodeRef().getId();
    }

    public NodeRef generatePsycologicalDocumen(NodeRef templateDoc, NodeRef declaration) throws Exception {

        DeclarationInfo info = this.getProperties(declaration);

        NodeRef nodeRef_templateFolder = siteService.getContainer(DatabaseModel.TYPE_PSYC_SITENAME, DatabaseModel.PROP_TEMPLATE_LIBRARY);


        ContentReader contentReader = contentService.getReader(templateDoc, ContentModel.PROP_CONTENT);
        TextDocument templateDocument = TextDocument.loadDocument(contentReader.getContentInputStream());


        if (nodeService.hasAspect(declaration, DatabaseModel.ASPECT_BUA)) {
            VariableField candidateVar = templateDocument.getVariableFieldByName("cpr");
            candidateVar.updateField(info.cpr, null);

            VariableField navn = templateDocument.getVariableFieldByName("Navn");
            navn.updateField(info.fornavn + " " + info.efternavn, null);
        }
        else {

            VariableField candidateVar = templateDocument.getVariableFieldByName("CPR");
            candidateVar.updateField(info.cpr, null);

            VariableField navn = templateDocument.getVariableFieldByName("NAVN");
            navn.updateField(info.fornavn + " " + info.efternavn, null);

        }

        // make the new document below the case

        FileInfo newFile = fileFolderService.create(declaration, info.cpr.substring(0,6) + "_psykundersøgelse.odt", ContentModel.TYPE_CONTENT);

        ContentWriter writer = contentService.getWriter(newFile.getNodeRef(), ContentModel.PROP_CONTENT, true);
        writer.setMimetype("application/vnd.oasis.opendocument.text");

        File f = new File("tmp");

        templateDocument.save(f);
        writer.putContent(f);

        return newFile.getNodeRef();
    }

    public NodeRef generateSamtykkeDocument(NodeRef templateDoc, NodeRef declaration) throws Exception {

        DeclarationInfo info = this.getProperties(declaration);

        NodeRef nodeRef_templateFolder = siteService.getContainer(DatabaseModel.TYPE_PSYC_SITENAME, DatabaseModel.PROP_TEMPLATE_LIBRARY);

        ContentReader contentReader = contentService.getReader(templateDoc, ContentModel.PROP_CONTENT);
        TextDocument templateDocument = TextDocument.loadDocument(contentReader.getContentInputStream());

        VariableField candidateVar = templateDocument.getVariableFieldByName("cprxxx");
        candidateVar.updateField(info.cpr, null);

        VariableField navn = templateDocument.getVariableFieldByName("navnxxx");
        navn.updateField(info.fornavn + " " + info.efternavn, null);

        FileInfo newFile = fileFolderService.create(declaration, info.cpr.substring(0,6) + "_samtykke.odt", ContentModel.TYPE_CONTENT);

        ContentWriter writer = contentService.getWriter(newFile.getNodeRef(), ContentModel.PROP_CONTENT, true);
        writer.setMimetype("application/vnd.oasis.opendocument.text");

        File f = new File("tmp");

        templateDocument.save(f);
        writer.putContent(f);

        return newFile.getNodeRef();
    }

    public NodeRef generateBerigtigelseAfKonklusionDocument(NodeRef declaration) throws Exception {
        NodeRef nodeRef_templateFolder = siteService.getContainer(DatabaseModel.TYPE_PSYC_SITENAME, DatabaseModel.PROP_TEMPLATE_LIBRARY);
        List<String> list = Arrays.asList(DatabaseModel.PROP_BERIGTIGELSE );
        List<ChildAssociationRef> children = nodeService.getChildrenByName(nodeRef_templateFolder, ContentModel.ASSOC_CONTAINS, list);

        NodeRef templateDoc = children.get(0).getChildRef();
        DeclarationInfo info = this.getProperties(declaration);

        ContentReader contentReader = contentService.getReader(templateDoc, ContentModel.PROP_CONTENT);
        TextDocument templateDocument = TextDocument.loadDocument(contentReader.getContentInputStream());

        VariableField navnXXX = templateDocument.getVariableFieldByName("navnxxx");
        navnXXX.updateField(info.fornavn + " " + info.efternavn, null);

        VariableField cprXXX = templateDocument.getVariableFieldByName("cprxxx");
        cprXXX.updateField(info.cpr, null);

        VariableField doctorXXX = templateDocument.getVariableFieldByName("doctorxxx");
        doctorXXX.updateField(info.doctor, null);

        VariableField afgivetdenXXX = templateDocument.getVariableFieldByName("datexxx");
        afgivetdenXXX.updateField(info.erklaeringAfgivet, null);

        VariableField afgivetdenXXX2 = templateDocument.getVariableFieldByName("afgivetxxx2");
        afgivetdenXXX2.updateField(info.erklaeringAfgivet, null);

        VariableField xxxHenviser = templateDocument.getVariableFieldByName("xxxhenviser");
        xxxHenviser.updateField(info.henvisendeInstans, null);


        VariableField sagsnrXXX = templateDocument.getVariableFieldByName("sagsnrxxx");
        sagsnrXXX.updateField(info.sagsnr, null);

        VariableField journalXXX = templateDocument.getVariableFieldByName("journalnrxxx");
        journalXXX.updateField(info.journalnummer, null);

        VariableField journal2XXX = templateDocument.getVariableFieldByName("journalnr2xxx");
        journal2XXX.updateField(info.journalnummer, null);

        VariableField voressagsnrXXX = templateDocument.getVariableFieldByName("voressagsnrxxx");
        voressagsnrXXX.updateField(info.sagsnr, null);

        VariableField cpr2xxx = templateDocument.getVariableFieldByName("cpr2xxx");
        cpr2xxx.updateField(info.cpr, null);

        Calendar cal = Calendar.getInstance();

        cal.setTime(new Date());
        int year = cal.get(Calendar.YEAR);
        int day = cal.get(Calendar.DATE);
        int month = (cal.get(Calendar.MONTH)+1);

        String declarationDay = (day <= 9) ? "0" + day : String.valueOf(day);
        String declarationMonthMonth = (month <= 9) ? "0" + month : String.valueOf(month);

        VariableField dagsdatoxxx = templateDocument.getVariableFieldByName("dagsdataxxx");
        dagsdatoxxx.updateField(declarationDay + "." + declarationMonthMonth + "." + year, null);

        FileInfo newFile = fileFolderService.create(declaration, info.cpr.substring(0,6) + "_berigtigelse.odt", ContentModel.TYPE_CONTENT);

        ContentWriter writer = contentService.getWriter(newFile.getNodeRef(), ContentModel.PROP_CONTENT, true);
        writer.setMimetype("application/vnd.oasis.opendocument.text");

        File f = new File("tmp");

        templateDocument.save(f);
        writer.putContent(f);


        // # https://redmine.magenta-aps.dk/issues/46005#note-11
        Map<QName, Serializable> aspectProps = new HashMap<>();
        nodeService.addAspect(newFile.getNodeRef(), ASPECT_ADDSIGNATURE, aspectProps);

        return newFile.getNodeRef();
    }

    public NodeRef generateSuppleredeUdtalelseDocument(NodeRef declaration) throws Exception {
        NodeRef nodeRef_templateFolder = siteService.getContainer(DatabaseModel.TYPE_PSYC_SITENAME, DatabaseModel.PROP_TEMPLATE_LIBRARY);
        List<String> list = Arrays.asList(DatabaseModel.PROP_SUPPLERENDEUDTALELSE );
        List<ChildAssociationRef> children = nodeService.getChildrenByName(nodeRef_templateFolder, ContentModel.ASSOC_CONTAINS, list);

        NodeRef templateDoc = children.get(0).getChildRef();
        DeclarationInfo info = this.getProperties(declaration);

        ContentReader contentReader = contentService.getReader(templateDoc, ContentModel.PROP_CONTENT);
        TextDocument templateDocument = TextDocument.loadDocument(contentReader.getContentInputStream());

        VariableField navnXXX = templateDocument.getVariableFieldByName("xxxfuldenavn");
        navnXXX.updateField(info.fornavn + " " + info.efternavn, null);

        VariableField cprXXX = templateDocument.getVariableFieldByName("xxxcpr");
        cprXXX.updateField(info.cpr, null);

        VariableField sagsnrXXX = templateDocument.getVariableFieldByName("xxxsagsnr");
        sagsnrXXX.updateField(info.sagsnr, null);

        VariableField doctorXXX = templateDocument.getVariableFieldByName("xxxafgivetaf");
        doctorXXX.updateField(info.doctor, null);

        VariableField journalXXX = templateDocument.getVariableFieldByName("xxxjournalnr");
        journalXXX.updateField(info.journalnummer, null);

        VariableField journal2XXX = templateDocument.getVariableFieldByName("xxxjournalnr2");
        journal2XXX.updateField(info.journalnummer, null);

        VariableField xxxHenviser = templateDocument.getVariableFieldByName("xxxhenviser");
        xxxHenviser.updateField(info.henvisendeInstans, null);

        VariableField afgivetdenXXX = templateDocument.getVariableFieldByName("xxxafgivelsesdato");
        afgivetdenXXX.updateField(info.erklaeringAfgivet, null);

        VariableField afgivet2denXXX = templateDocument.getVariableFieldByName("xxxafgivelsesdato2");
        afgivet2denXXX.updateField(info.erklaeringAfgivet, null);

        VariableField voressagsnrXXX = templateDocument.getVariableFieldByName("xxxvoressagsnr");
        voressagsnrXXX.updateField(info.sagsnr, null);

        VariableField cpr2xxx = templateDocument.getVariableFieldByName("xxxcpr2");
        cpr2xxx.updateField(info.cpr, null);

        Calendar cal = Calendar.getInstance();

        cal.setTime(new Date());
        int year = cal.get(Calendar.YEAR);
        int day = cal.get(Calendar.DATE);
        int month = (cal.get(Calendar.MONTH))+1;

        String declarationDay = (day <= 9) ? "0" + day : String.valueOf(day);
        String declarationMonthMonth = (month <= 9) ? "0" + month : String.valueOf(month);

        VariableField dagsdatoxxx = templateDocument.getVariableFieldByName("dagsdatoxxx");
        dagsdatoxxx.updateField(declarationDay + "." + declarationMonthMonth + "." + year, null);

        // #47220 make a folder for the document

        FileInfo folder = fileFolderService.create(declaration, PROP_SUPPLERENDEUDTALELSE_FOLDER, ContentModel.TYPE_FOLDER);
        FileInfo newFile = fileFolderService.create(declaration, info.cpr.substring(0,6) + "_" + PROP_SUPPLERENDEUDTALELSE, ContentModel.TYPE_CONTENT);

        ContentWriter writer = contentService.getWriter(newFile.getNodeRef(), ContentModel.PROP_CONTENT, true);
        writer.setMimetype("application/vnd.oasis.opendocument.text");

        File f = new File("tmp");

        templateDocument.save(f);
        writer.putContent(f);

        // # https://redmine.magenta-aps.dk/issues/46005#note-11
        Map<QName, Serializable> aspectProps = new HashMap<>();
        nodeService.addAspect(newFile.getNodeRef(), ASPECT_ADDSIGNATURE, aspectProps);

        return newFile.getNodeRef();
    }

    public NodeRef generateBrevDocument(NodeRef declaration) throws Exception {
        NodeRef nodeRef_templateFolder = siteService.getContainer(DatabaseModel.TYPE_PSYC_SITENAME, DatabaseModel.PROP_TEMPLATE_LIBRARY);
        List<String> list = Arrays.asList(DatabaseModel.PROP_FLETTEBREV );
        List<ChildAssociationRef> children = nodeService.getChildrenByName(nodeRef_templateFolder, ContentModel.ASSOC_CONTAINS, list);

        NodeRef templateDoc = children.get(0).getChildRef();
        DeclarationInfo info = this.getProperties(declaration);

        ContentReader contentReader = contentService.getReader(templateDoc, ContentModel.PROP_CONTENT);
        TextDocument templateDocument = TextDocument.loadDocument(contentReader.getContentInputStream());

        VariableField navnXXX = templateDocument.getVariableFieldByName("fuldnavnxxx");
        navnXXX.updateField(info.fornavn + " " + info.efternavn, null);

        VariableField cprXXX = templateDocument.getVariableFieldByName("cprxxx");
        cprXXX.updateField(info.cpr, null);

        VariableField journalXXX = templateDocument.getVariableFieldByName("journalnrxxx");
        journalXXX.updateField(info.journalnummer, null);

        VariableField journal2XXX = templateDocument.getVariableFieldByName("journalnr2xxx");
        journal2XXX.updateField(info.journalnummer, null);

        VariableField xxxHenviser = templateDocument.getVariableFieldByName("henviserxxx");
        xxxHenviser.updateField(info.henvisendeInstans, null);

        VariableField cpr2xxx = templateDocument.getVariableFieldByName("cpr2xxx");
        cpr2xxx.updateField(info.cpr, null);

        Calendar cal = Calendar.getInstance();

        cal.setTime(new Date());
        int year = cal.get(Calendar.YEAR);
        int day = cal.get(Calendar.DATE);
        int month = (cal.get(Calendar.MONTH))+1;

        String declarationDay = (day <= 9) ? "0" + day : String.valueOf(day);
        String declarationMonthMonth = (month <= 9) ? "0" + month : String.valueOf(month);

        VariableField dagsdatoxxx = templateDocument.getVariableFieldByName("flettedatoxxx");
        dagsdatoxxx.updateField(declarationDay + "." + declarationMonthMonth + "." + year, null);


        List<String> criteria = Arrays.asList(DatabaseModel.PROP_DEFAULTFOLDER_MAILRECEIPTS);
        List<ChildAssociationRef> mailFolder = nodeService.getChildrenByName(declaration, org.alfresco.model.ContentModel.ASSOC_CONTAINS, criteria);

        NodeRef korrespondance_folder = mailFolder.get(0).getChildRef();

        FileInfo newFile = fileFolderService.create(korrespondance_folder, info.cpr.substring(0,6) + "_brev.odt", ContentModel.TYPE_CONTENT);

        ContentWriter writer = contentService.getWriter(newFile.getNodeRef(), ContentModel.PROP_CONTENT, true);
        writer.setMimetype("application/vnd.oasis.opendocument.text");

        File f = new File("tmp");

        templateDocument.save(f);
        writer.putContent(f);

        // # https://redmine.magenta-aps.dk/issues/46005#note-11
        Map<QName, Serializable> aspectProps = new HashMap<>();
        nodeService.addAspect(newFile.getNodeRef(), ASPECT_ADDSIGNATURE, aspectProps);

        return newFile.getNodeRef();
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
        public String doctor;
        public String henvisendeInstans;
        public String erklaeringAfgivet;
    }

//    private static void copyInputStreamToFile(InputStream inputStream, File file)
//            throws IOException {
//
//        // append = false
//        try (FileOutputStream outputStream = new FileOutputStream(file, false)) {
//            int read;
//            byte[] bytes = new byte[DEFAULT_BUFFER_SIZE];
//            while ((read = inputStream.read(bytes)) != -1) {
//                outputStream.write(bytes, 0, read);
//            }
//        }
//
//    }
}
