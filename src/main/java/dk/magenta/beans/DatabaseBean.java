package dk.magenta.beans;

import dk.magenta.model.DatabaseModel;
import dk.magenta.utils.JSONUtils;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.site.SiteServiceException;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
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

import static org.alfresco.model.ContentModel.ASSOC_CONTAINS;
import static org.alfresco.model.ContentModel.PROP_NAME;
import static org.alfresco.model.ContentModel.TYPE_CONTENT;
import static org.alfresco.service.namespace.NamespaceService.CONTENT_MODEL_1_0_URI;

public class DatabaseBean {

    private PermissionService permissionService;
    private SiteService siteService;
    private NodeService nodeService;
    private ContentService contentService;
    private AuthorityService authorityService;

    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    public NodeRef createDatabase (String displayName, String description) {

        String shortName = displayName.replaceAll(" ", "-");
        NodeRef nodeRef = createSite(shortName, displayName, description);

        // Set rm:database as type for site
        QName typeQName = QName.createQName(DatabaseModel.RM_MODEL_URI, DatabaseModel.DATABASE);
        nodeService.setType(nodeRef, typeQName);

        //Setup Property Values
        NodeRef propertyValues = siteService.getContainer(shortName, DatabaseModel.PROP_VALUES);
        String propertyValueManager = "GROUP_site_" + shortName + "_SitePropertyValueManager";
        String siteManager = "GROUP_site_" + shortName + "_SiteManager";
        permissionService.setInheritParentPermissions(propertyValues, false);
        permissionService.setPermission(propertyValues, propertyValueManager, PermissionService.EDITOR, true);
        permissionService.setPermission(propertyValues, siteManager, PermissionService.EDITOR, true);

        //Setup templateFolder
        NodeRef templateLibrary = siteService.getContainer(shortName, DatabaseModel.PROP_TEMPLATE_LIBRARY);
        String templateFolderManager = "GROUP_site_" + shortName + "_TemplateFolderValueManager";
        permissionService.setInheritParentPermissions(templateLibrary, false);
        permissionService.setPermission(templateLibrary, templateFolderManager, DatabaseModel.Permission_SiteTemplateManager, true);

        //Setup Document Library
        NodeRef documentLibrary = siteService.getContainer(shortName, SiteService.DOCUMENT_LIBRARY);
        String entryLockManager = "GROUP_site_" + shortName + "_SiteEntryLockManager";
        permissionService.setPermission(documentLibrary, entryLockManager, PermissionService.UNLOCK, true);
        Map<QName,Serializable> props = new HashMap<>();
        nodeService.addAspect(documentLibrary, ContentModel.ASPECT_COUNTABLE, props);

        return nodeRef;
    }

    private NodeRef createSite(String shortName, String displayName, String description) {

        SiteInfo site = null;

        // Iterate through possible short names for the new site until a vacant is found
        int i = 1;
        do {
            try {
                // Create site
                site = siteService.createSite("site-dashboard", shortName, displayName, description,
                        SiteVisibility.PRIVATE);
                NodeRef n = site.getNodeRef();

                // Create containers for document library, template library and property values
                createContainer(shortName, SiteService.DOCUMENT_LIBRARY);
                createContainer(shortName, DatabaseModel.PROP_TEMPLATE_LIBRARY);
                createContainer(shortName, DatabaseModel.PROP_VALUES);

                // Create site dashboard
                createSiteDashboard(n, shortName);
            }
            catch(SiteServiceException e) {
                if(e.getMsgId().equals("site_service.unable_to_create"))
                    shortName = shortName + "-" + ++i;
            }
        }
        while(site == null);

        return site.getNodeRef();
    }

    private NodeRef createContainer(String shortName, String componentId) {
        return siteService.createContainer(shortName, componentId, ContentModel.TYPE_FOLDER, null);
    }

    private NodeRef createChildNode(NodeRef n, String name, QName type) {
        Map<QName, Serializable> props = new HashMap<>();
        props.put(ContentModel.PROP_NAME, name);

        return nodeService.createNode(n, ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name),
                type, props).getChildRef();
    }

    private void createSiteDashboard(NodeRef siteNodeRef, String siteShortName) {

        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder;
        try {
            docBuilder = docBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            return;
        }

        // Create surf-config folder
        NodeRef surfConfigRef = createChildNode(siteNodeRef, "surf-config", ContentModel.TYPE_FOLDER);
        Map<QName, Serializable> aspectProps = new HashMap<>();
        nodeService.addAspect(surfConfigRef, ContentModel.ASPECT_HIDDEN, aspectProps);

        // Create components folder
        NodeRef componentsRef = createChildNode(surfConfigRef, "components", ContentModel.TYPE_FOLDER);

        Map<String, String> components = new HashMap<>();
        components.put("title", "title/collaboration-title");
        components.put("navigation", "navigation/collaboration-navigation");
        components.put("component-1-1", "dashlets/docsummary");
        components.put("component-1-2", "dashlets/forum-summary");
        components.put("component-2-1", "dashlets/colleagues");
        components.put("component-2-2", "dashlets/activityfeed");

        for (Map.Entry<String, String> component : components.entrySet()) {
            Document doc = createComponentXML(docBuilder, siteShortName, component.getKey(), component.getValue(), "");
            String name = "page." + component.getKey() + ".site~" + siteShortName + "~dashboard";
            createXMLFile(componentsRef, name, doc);
        }

        // Create pages folder
        NodeRef pagesRef = createChildNode(surfConfigRef, "pages", ContentModel.TYPE_FOLDER);
        NodeRef siteRef = createChildNode(pagesRef, "site", ContentModel.TYPE_FOLDER);
        NodeRef siteShortNameRef = createChildNode(siteRef, siteShortName, ContentModel.TYPE_FOLDER);

        Document dashboardDoc = createPageXML(docBuilder);
        createXMLFile(siteShortNameRef, "dashboard", dashboardDoc);
    }

    private Document createComponentXML(DocumentBuilder docBuilder, String siteShortName, String region_id,
                                               String url, String height) {
        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement("component");
        doc.appendChild(rootElement);

        addXMLChild(doc, rootElement, "guid", "page." + region_id + ".site~" + siteShortName + "~dashboard");
        addXMLChild(doc, rootElement, "scope", "page");
        addXMLChild(doc, rootElement, "region-id", region_id);
        addXMLChild(doc, rootElement, "source-id", "site/" + siteShortName + "/dashboard");
        addXMLChild(doc, rootElement, "url", "/components/" + url);

        if(!"".equals(height)) {
            Element propertiesElement = doc.createElement("properties");
            doc.appendChild(propertiesElement);

            addXMLChild(doc, propertiesElement, "height", height);
        }

        return doc;
    }

    private Document createPageXML(DocumentBuilder docBuilder) {
        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement("page");
        doc.appendChild(rootElement);

        addXMLChild(doc, rootElement, "title", "Collaboration Site Dashboard");
        addXMLChild(doc, rootElement, "title-id", "page.siteDashboard.title");
        addXMLChild(doc, rootElement, "description", "Collaboration site's dashboard page");
        addXMLChild(doc, rootElement, "description-id", "page.siteDashboard.description");
        addXMLChild(doc, rootElement, "authentication", "user");
        addXMLChild(doc, rootElement, "template-instance", "dashboard-2-columns-wide-right");

        Element propertiesElement = doc.createElement("properties");
        rootElement.appendChild(propertiesElement);

        String pageDocLib = "{\"pageId\":\"documentlibrary\"}";
        addXMLChild(doc, propertiesElement, "sitePages", "[" + pageDocLib + "]");

        return doc;
    }

    private void addXMLChild(Document doc, Element parent, String name, String textContent) {
        Element e = doc.createElement(name);
        e.setTextContent(textContent);
        parent.appendChild(e);
    }

    private void createXMLFile(NodeRef parent,
                                      String fileName, Document xmlDoc) {
        fileName += ".xml";

        Map<QName, Serializable> properties = new HashMap<>();
        properties.put(PROP_NAME, fileName);
        NodeRef n = nodeService.createNode(parent, ASSOC_CONTAINS,
                QName.createQName(CONTENT_MODEL_1_0_URI, fileName), TYPE_CONTENT, properties).getChildRef();

        ContentWriter contentWriter = contentService.getWriter(n, ContentModel.PROP_CONTENT, true);
        contentWriter.setMimetype(MimetypeMap.MIMETYPE_XML);
        OutputStream s = contentWriter.getContentOutputStream();

        // Write to new xml file
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            StreamResult result = new StreamResult(s);
            DOMSource source = new DOMSource(xmlDoc);
            transformer.transform(source, result);
            s.close();
        } catch (IOException | TransformerException e) {
            e.printStackTrace();
        }
    }


    public List<String> getRole(String siteShortName) {
        Set<String> userAuthorities = authorityService.getAuthorities();
        List<String> roles = new ArrayList<>();
        String authPrefix = "GROUP_site_" + siteShortName + "_";

        for(String authority : userAuthorities) {
            if(authority.startsWith(authPrefix)) {
                String siteRole = authority.replace(authPrefix, "");
                roles.add(siteRole);
            }
        }
        return roles;
    }

    public JSONObject updateUserRoles(String siteShortName, String username, JSONArray addGroups, JSONArray removeGroups)
            throws JSONException {

        Set<String> authorities = authorityService.getAuthorities();

        if(authorities.contains("GROUP_site_" + siteShortName + "_SiteRoleManager")) {
            try {
                AuthenticationUtil.setRunAsUserSystem();
                if(addGroups != null)
                    updateSiteRole(siteShortName, username, addGroups, true);
                if(removeGroups != null)
                    updateSiteRole(siteShortName, username, removeGroups, false);
            }
            finally {
                AuthenticationUtil.clearCurrentSecurityContext();
            }
            return JSONUtils.getSuccess();
        }
        return JSONUtils.getError(new AccessDeniedException("You are not Role Manager for the site: " + siteShortName));
    }

    private void updateSiteRole(String siteShortName, String username, JSONArray groups,
                                boolean add) throws JSONException {
        Set<String> authorities = authorityService.getAuthoritiesForUser(username);

        for (int i = 0; i < groups.length(); i++) {
            String group = groups.getString(i);
            String groupSite = group.split("_")[2];
            if (siteShortName.equals(groupSite))
                if(authorities.contains(group)) {
                    if (!add)
                        authorityService.removeAuthority(group, username);
                }
                else {
                    if (add)
                        authorityService.addAuthority(group, username);
                }
        }
    }

    public String getType(String siteShortName) {
        SiteInfo site = siteService.getSite(siteShortName);
        NodeRef nodeRef = site.getNodeRef();
        Serializable databaseType = nodeService.getProperty(nodeRef, DatabaseModel.PROP_DATABASE_TYPE);

        if(databaseType == null)
            throw new IllegalArgumentException("The site '" + siteShortName + "' is not a database.");

        return databaseType.toString();
    }
}
