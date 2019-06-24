package dk.magenta.beans;

import dk.magenta.model.DatabaseModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.download.DownloadService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public class ContentsBean {

    private NodeService nodeService;
    private PermissionService permissionService;
    private PersonService personService;
    private SiteService siteService;
    private DownloadService downloadService;
    private Repository repository;

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


    public NodeRef downloadContent(NodeRef[] requestedNodes) {
        NodeRef downloadNodeRef = downloadService.createDownload(requestedNodes, false);

        //Set mime type to zip. Default is octet-stream
        ContentData cd = (ContentData) nodeService.getProperty(downloadNodeRef, ContentModel.PROP_CONTENT);
        ContentData newCd = ContentData.setMimetype(cd, "application/zip");
        nodeService.setProperty(downloadNodeRef, ContentModel.PROP_CONTENT, newCd);

        return downloadNodeRef;
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

}