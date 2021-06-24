package dk.magenta.beans;

import dk.magenta.model.DatabaseModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.download.DownloadService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.pdfbox.pdmodel.PDDocument;

import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

public class ContentsBean {

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

    public void setVersionService(VersionService versionService) {
        this.versionService = versionService;
    }

    private VersionService versionService;

    public void setFileFolderService(FileFolderService fileFolderService) {
        this.fileFolderService = fileFolderService;
    }

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

    public JSONArray getChildNodes(NodeRef parentNodeRef) throws JSONException {

        JSONArray result = new JSONArray();

        List<String> contentTypes = new ArrayList<>();
        contentTypes.add("cmis:document");
        contentTypes.add("cmis:folder");
        contentTypes.add("cmis:link");

        Map<String, JSONArray> contentTypeMap = new HashMap<>();
        for (String contentType : contentTypes)
            contentTypeMap.put(contentType, new JSONArray());

        List<ChildAssociationRef> childAssociationRefs = nodeService.getChildAssocs(parentNodeRef);

        for (ChildAssociationRef childAssociationRef : childAssociationRefs) {
            JSONObject json = new JSONObject();

            NodeRef childNodeRef = childAssociationRef.getChildRef();

            if (!nodeService.hasAspect(childNodeRef, ContentModel.ASPECT_HIDDEN)) {

                String name = (String) nodeService.getProperty(childNodeRef, ContentModel.PROP_NAME);
                json.put("name", name);

                QName qname = nodeService.getType(childNodeRef);

                String type;
                if (qname.equals(ContentModel.TYPE_FOLDER)) {
                    type = "cmis:folder";
                } else if (qname.equals(DatabaseModel.PROP_LINK)) {
                    type = "cmis:link";
                } else {
                    type = "cmis:document";
                    ContentData contentData = (ContentData) nodeService.getProperty(childNodeRef, ContentModel.PROP_CONTENT);
                     String mimeType = contentData.getMimetype();
                    json.put("mimeType", mimeType);
                    String nodeRefStr = childNodeRef.toString().replace("://", "/");
                    String thumbnail = "/alfresco/service/api/node/" + nodeRefStr + "/content/thumbnails/doclib?c=force";
                    json.put("thumbnail", thumbnail);
                }

                AccessStatus accessStatus = permissionService.hasPermission(childNodeRef, PermissionService.DELETE);
                json.put("canMoveAndDelete", accessStatus == AccessStatus.ALLOWED);

                json.put("contentType", type);

                if (!"cmis:link".equals(type)) {
                    json.put("nodeRef", childNodeRef);

                    ChildAssociationRef parent = nodeService.getPrimaryParent(childNodeRef);

                    json.put("parentNodeRef", parent.getParentRef());
                    json.put("shortRef", childNodeRef.getId());

                    String modifier = (String) nodeService.getProperty(childNodeRef, ContentModel.PROP_MODIFIER);
                    NodeRef person = personService.getPerson(modifier);

                    json.put("lastChangedBy", nodeService.getProperty(person, ContentModel.PROP_FIRSTNAME) + " " + nodeService.getProperty(person, ContentModel.PROP_LASTNAME));

                    Date d = (Date) nodeService.getProperty(childNodeRef, ContentModel.PROP_MODIFIED);
                    json.put("lastChanged", d.getTime());

                    boolean hasHistory = false;
                    if (nodeService.hasAspect(childNodeRef, ContentModel.ASPECT_VERSIONABLE)) {
                        String versionLabel = (String) nodeService.getProperty(childNodeRef, ContentModel.PROP_VERSION_LABEL);
                        if (versionLabel != null && !versionLabel.equals("1.0"))
                            hasHistory = true;
                    }
                    json.put("hasHistory", hasHistory);
                } else {
                    String linkSiteShortName = (String) nodeService.getProperty(childNodeRef, DatabaseModel.PROP_LINK_TARGET);
                    NodeRef linkNodeRef = (NodeRef) nodeService.getProperty(childNodeRef, DatabaseModel.PROP_LINK_TARGET_NODEREF);

                    json.put("nodeid", childNodeRef.getId());
                    json.put("destination_link", linkSiteShortName);
                    json.put("nodeRef", childNodeRef);

                    if (linkNodeRef != null) {
                        json.put("destination_nodeid", linkNodeRef.getId());

                        SiteInfo linkSiteInfo = siteService.getSite(linkSiteShortName);
                        String linkSiteDisplayName = linkSiteInfo.getTitle();
                        json.put("name", linkSiteDisplayName);
                    }
                }

                contentTypeMap.get(type).put(json);
            }
        }

        for (String contentType : contentTypes)
            result.put(contentTypeMap.get(contentType));

        return result;
    }

    public void transformPDFtoJPG(NodeRef nodeRef) throws IOException {

        ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);

        // Create new preview node of earlier version
        Map<QName,  Serializable> properties = new HashMap<>();
        properties.put(ContentModel.PROP_NAME, "duff");

        ContentWriter jpgWriter = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);


        PDDocument document = PDDocument.load (reader.getContentInputStream());

        PDPage firstPage = (PDPage) document.getDocumentCatalog().getAllPages().get(0);

        // tjek indhold.
        Map<String, PDXObjectImage>  images = firstPage.findResources().getImages();
        System.out.println("images");
        System.out.println(images);


        System.out.println(images.get("Im4"));
        System.out.println(images.get(0));
        System.out.println(images.get(1));

        File f = new File("tmp");

        ImageIO.write(images.get("Im0").getRGBImage(), "JPEG", f);

        jpgWriter.setMimetype(MimetypeMap.MIMETYPE_IMAGE_JPEG);
        jpgWriter.putContent(f);

        document.close();
    }


    public NodeRef downloadContent(NodeRef[] requestedNodes) {
        NodeRef downloadNodeRef = downloadService.createDownload(requestedNodes, false);

        //Set mime type to zip. Default is octet-stream
        ContentData cd = (ContentData) nodeService.getProperty(downloadNodeRef, ContentModel.PROP_CONTENT);
        ContentData newCd = ContentData.setMimetype(cd, "application/zip");
        nodeService.setProperty(downloadNodeRef, ContentModel.PROP_CONTENT, newCd);

        return downloadNodeRef;
    }


    public void revert() {



    }

    public void moveContent(NodeRef[] requestedNodes, NodeRef dest) throws FileNotFoundException {

        for (int i = 0; i <= requestedNodes.length - 1; i++) {
            NodeRef n = requestedNodes[i];
            FileInfo f = fileFolderService.move(n, dest, null);
        }
    }

    public void rename(NodeRef nodeRef, String name) {
            String fileExtension = getFileExtension(nodeRef);
            if(fileExtension != null) {
                name += fileExtension;
            }

        nodeService.setProperty(nodeRef, ContentModel.PROP_NAME, name);
    }

    private String[] getNameAndExtension(NodeRef nodeRef) {
        String name = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
        boolean isContent = isContent(nodeRef);
        return getNameAndExtension(name, isContent);
    }

    public boolean isContent(NodeRef nodeRef) {
        QName type = nodeService.getType(nodeRef);
        return type.equals(ContentModel.TYPE_CONTENT);
    }

    public String getFileExtension(NodeRef nodeRef) {
        String[] nameAndExtension = getNameAndExtension(nodeRef);
        return nameAndExtension[1];
    }

    private String[] getNameAndExtension(String name, boolean isContent) {
        String[] split = new String[2];
        if(isContent) {
            int extensionIndex = name.lastIndexOf(".");
            if (extensionIndex > 0) {
                split[0] = name.substring(0, extensionIndex);
                split[1] = name.substring(extensionIndex);
            } else {
                split[0] = name;
            }
        }
        else {
            split[0] = name;
            split[1] = "";
        }
        return split;
    }

    public String getDownloadStatus(NodeRef downloadNodeRef) {
        return downloadService.getDownloadStatus(downloadNodeRef).getStatus().toString();
    }

    /**
     * Gets the Company Home.
     * (method = getCompanyHome)
     * @return the nodeRef for Company Home.
     */
    public NodeRef getCompanyHome() throws JSONException {
        return repository.getCompanyHome();
    }

    public NodeRef getSharedFolderBUA() throws JSONException {

        NodeRef sharedFolderBua = siteService.getContainer("retspsyk", DatabaseModel.PROP_SHAREDFOLDER_BUA);

        if (sharedFolderBua != null) {
            return sharedFolderBua;
        }
        else {
            return siteService.createContainer("retspsyk", DatabaseModel.PROP_SHAREDFOLDER_BUA, ContentModel.TYPE_FOLDER, null);
        }
    }




    public org.json.JSONArray getVersions(NodeRef nodeRef) throws JSONException {
        JSONArray result = new JSONArray();
        VersionHistory h = versionService.getVersionHistory(nodeRef);
        JSONObject json = new JSONObject();


        if (h != null) {
            Collection<Version> versions = h.getAllVersions();

            String latestVersion = versionService.getCurrentVersion(nodeRef).getVersionLabel();

            for (Version v : versions) {

                json = new JSONObject();
                json.put("latest", v.getVersionLabel().equals(latestVersion));

                json.put("parent_nodeRef", nodeRef.getId());
                json.put("nodeRef", v.getFrozenStateNodeRef().getId());

                String modifier = v.getFrozenModifier();

                String metadata_modifier = (String)v.getVersionProperty("{http://www.alfresco.org/model/content/1.0}modifier");


                NodeRef personNoderef;

                if (metadata_modifier != null) {
                    personNoderef = personService.getPerson(metadata_modifier);
                }
                else {
                    personNoderef = personService.getPerson(modifier);
                }



                PersonService.PersonInfo personInfo = personService.getPerson(personNoderef);

                String displayName = personInfo.getFirstName() + " " + personInfo.getLastName();
                if(displayName != null) {
                    json.put("modifier", displayName);
                }

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                json.put("created", sdf.format(v.getFrozenModifiedDate()));

                json.put("version", v.getVersionLabel());

                result.put(json);
            }
        }

        return result;
    }

    /**
     * Creates a thumbnail of a version
     * @param nodeId id of parent node.
     * @param versionId id of version node.
     * @return a JSONArray containing a JSONObject 'nodeRef'.
     */
    public NodeRef getThumbnail(String nodeId, String versionId, boolean forceUpdate) {


        NodeRef nodeRef = new NodeRef("workspace", "SpacesStore", nodeId);
        NodeRef versionRef = new NodeRef("versionStore", "version2Store", versionId);

        Serializable parentName = nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
        Serializable versionLabel = nodeService.getProperty(versionRef, ContentModel.PROP_VERSION_LABEL);

        String name =  "(v. " + versionLabel + ") " + parentName;
        NodeRef versionPreviewRef = nodeService.getChildByName(nodeRef, DatabaseModel.ASSOC_VERSION_PREVIEW, name);


        if(versionPreviewRef != null && !forceUpdate) {

            return versionPreviewRef;
        }
        else if (forceUpdate && versionPreviewRef != null) {

            nodeService.deleteNode(versionPreviewRef);
        }



        AuthenticationUtil.runAs(() -> {
            // Add version previewable aspect if it is not present

            // Create new preview node of earlier version
            Map<QName,  Serializable> properties = new HashMap<>();
            properties.put(ContentModel.PROP_NAME, name);
            Serializable content = nodeService.getProperty(versionRef, ContentModel.PROP_CONTENT);
            properties.put(ContentModel.PROP_CONTENT, content);
            QName cmName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name);
            ChildAssociationRef childAssocRef = nodeService.createNode(
                    nodeRef,
                    DatabaseModel.ASSOC_VERSION_PREVIEW,
                    cmName,
                    ContentModel.TYPE_CONTENT,
                    properties);

            nodeService.addAspect(childAssocRef.getChildRef(), ContentModel.ASPECT_HIDDEN, null);
            return true;
        }, AuthenticationUtil.getSystemUserName());
        versionPreviewRef = nodeService.getChildByName(nodeRef, DatabaseModel.ASSOC_VERSION_PREVIEW, name);
        if(versionPreviewRef != null)
            return versionPreviewRef;
        else
            return null;
    }
}

