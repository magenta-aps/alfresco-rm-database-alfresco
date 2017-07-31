package dk.magenta.beans;

import dk.magenta.model.DatabaseModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.site.SiteServiceException;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
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
import java.util.HashMap;
import java.util.Map;

import static org.alfresco.model.ContentModel.ASSOC_CONTAINS;
import static org.alfresco.model.ContentModel.PROP_NAME;
import static org.alfresco.model.ContentModel.TYPE_CONTENT;
import static org.alfresco.service.namespace.NamespaceService.CONTENT_MODEL_1_0_URI;

public class DatabaseBean {

    private PermissionService permissionService;
    private SiteService siteService;
    private NodeService nodeService;
    private ContentService contentService;
    private AuthenticationService authenticationService;

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

    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public NodeRef createDatabase (String displayName, String description) {

        String shortName = displayName.replaceAll(" ", "-");
        NodeRef nodeRef = createSite(shortName, displayName, description);

        NodeRef propertyValues = siteService.getContainer(shortName, DatabaseModel.PROP_VALUES);
        String propertyValueManager = "GROUP_site_" + shortName + "_SitePropertyValueManager";
        String siteManager = "GROUP_site_" + shortName + "_SiteManager";
        permissionService.setInheritParentPermissions(propertyValues, false);
        permissionService.setPermission(propertyValues, propertyValueManager, PermissionService.EDITOR, true);
        permissionService.setPermission(propertyValues, siteManager, PermissionService.EDITOR, true);

        NodeRef documentLibrary = siteService.getContainer(shortName, SiteService.DOCUMENT_LIBRARY);
        String entryLockManager = "GROUP_site_" + shortName + "_SiteEntryLockManager";
        permissionService.setPermission(documentLibrary, entryLockManager, PermissionService.UNLOCK, true);

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

                // Create containers for document library and property values
                createContainer(shortName, SiteService.DOCUMENT_LIBRARY);
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


    public String getRole(String siteShortName) {
        String currentUser = authenticationService.getCurrentUserName();
        return siteService.getMembersRole(siteShortName, currentUser);
    }
}
