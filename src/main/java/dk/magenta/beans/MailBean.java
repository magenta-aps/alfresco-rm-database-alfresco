package dk.magenta.beans;

import dk.magenta.model.DatabaseModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.transform.ContentTransformer;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.download.DownloadService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.*;

public class MailBean {

    private NodeService nodeService;
    private PermissionService permissionService;
    private PersonService personService;
    private SiteService siteService;
    private DownloadService downloadService;
    private Repository repository;

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    private ContentService contentService;
    private FileFolderService fileFolderService;

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }
    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }
    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }
    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void setDownloadService(DownloadService downloadService) {
        this.downloadService = downloadService;
    }
    public void setRepository(Repository repository)
    {
        this.repository = repository;
    }

    public void transform(NodeRef source) {

        System.out.println("hvad er the source" + source);

        // Create new PDF
        Map<QName, Serializable> documentLibaryProps = new HashMap<>();
        documentLibaryProps.put(ContentModel.PROP_NAME, "Medlemsoversigt.pdf");

        NodeRef parent = nodeService.getPrimaryParent(source).getParentRef();

        ChildAssociationRef pdf = nodeService.createNode(parent, ContentModel.ASSOC_CONTAINS,
                QName.createQName(ContentModel.USER_MODEL_URI, "thePDF"),
                ContentModel.TYPE_CONTENT, documentLibaryProps);





//        // set the dest mimetype
//        ContentData contentDataPDF = (ContentData) nodeService.getProperty(pdf.getChildRef(), ContentModel.PROP_CONTENT);
//        contentDataPDF.setMimetype(contentDataPDF, MimetypeMap.MIMETYPE_PDF);
//
//        nodeService.setProperty(pdf.getChildRef(), ContentModel.PROP_CONTENT,contentDataPDF);
//
//        contentDataPDF = (ContentData) nodeService.getProperty(pdf.getChildRef(), ContentModel.PROP_CONTENT);
//
//
//
//
//        String originalMimeTypePDF = contentDataPDF.getMimetype();
//        System.out.println("hvad er pdf the mimetype:" + originalMimeTypePDF);
//
//

        ContentData contentData = (ContentData) nodeService.getProperty(source, ContentModel.PROP_CONTENT);
        String originalMimeType = contentData.getMimetype();



//

        ContentReader pptReader = contentService.getReader(source, ContentModel.PROP_CONTENT);
        ContentWriter pdfWriter = contentService.getWriter(pdf.getChildRef(), ContentModel.PROP_CONTENT, true);
        pdfWriter.setMimetype(MimetypeMap.MIMETYPE_PDF);
        ContentTransformer pptToPdfTransformer = contentService.getTransformer(originalMimeType, MimetypeMap.MIMETYPE_PDF);


        TransformationOptions options = new TransformationOptions();


        System.out.println("originalMimeType" + originalMimeType);




        pptToPdfTransformer.transform(pptReader, pdfWriter);

    }









}