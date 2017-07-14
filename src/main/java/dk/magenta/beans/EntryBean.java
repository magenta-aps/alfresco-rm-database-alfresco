package dk.magenta.beans;

import dk.magenta.model.DatabaseModel;
import dk.magenta.utils.JSONUtils;
import dk.magenta.utils.TypeUtils;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

public class EntryBean {

    private NodeService nodeService;
    private SearchService searchService;
    private SiteService siteService;

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }
    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }
    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public Set<NodeRef> getEntries (String query) throws JSONException {

        ResultSet resultSet = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "lucene", query);
        Iterator<ResultSetRow> iterator = resultSet.iterator();

        Set<NodeRef> nodeRefs = new HashSet<>();
        while (iterator.hasNext()) {
            ResultSetRow result = iterator.next();
            nodeRefs.add(result.getNodeRef());
        }
        return nodeRefs;
    }

    public NodeRef addEntry (String siteShortName, String type, Map<QName, Serializable> properties) throws JSONException {

        //Get counter for this site document library
        NodeRef docLibRef = siteService.getContainer(siteShortName, SiteService.DOCUMENT_LIBRARY);
        Integer counter = (Integer) nodeService.getProperty(docLibRef, ContentModel.PROP_COUNTER);
        if(counter == null)
            counter = 0;
        counter++;

        //Get entry key for this type
        String entryKey = TypeUtils.getEntryKey(type);
        QName entryKeyQName = QName.createQName(DatabaseModel.RM_MODEL_URI, entryKey);

        //Remove value if set
        if (properties.containsKey(entryKeyQName))
            properties.remove(entryKeyQName);

        //Set unique value
        properties.put(entryKeyQName, counter);

        //Create name for node (This is not displayed anywhere)
        QName nameQName = QName.createQName(DatabaseModel.CONTENT_MODEL_URI, counter.toString());

        //Get current date folder to place the entry in
        LocalDate date = LocalDate.now();
        String year = ((Integer)date.getYear()).toString();
        String month = ((Integer)date.getMonthValue()).toString();
        String day = ((Integer)date.getDayOfMonth()).toString();

        NodeRef yearRef = getOrCreateChildByName(docLibRef, year);
        NodeRef monthRef = getOrCreateChildByName(yearRef, month);
        NodeRef dayRef = getOrCreateChildByName(monthRef, day);

        //Create entry
        QName typeQName = QName.createQName(DatabaseModel.RM_MODEL_URI, type);
        ChildAssociationRef childAssociationRef =
                nodeService.createNode(dayRef, ContentModel.ASSOC_CONTAINS, nameQName, typeQName, properties);
        NodeRef nodeRef = childAssociationRef.getChildRef();

        //Increment the site document library counter when the entry has been created successfully
        nodeService.setProperty(docLibRef, ContentModel.PROP_COUNTER, counter);

        return nodeRef;
    }

    private NodeRef getOrCreateChildByName(NodeRef parentRef, String name) {

        NodeRef childRef = nodeService.getChildByName(parentRef, ContentModel.ASSOC_CONTAINS, name);

        if(childRef == null) {
            Map<QName, Serializable> properties = new HashMap<>();
            properties.put(ContentModel.PROP_NAME, name);
            QName qName = QName.createQName(DatabaseModel.CONTENT_MODEL_URI, name);
            ChildAssociationRef childAssociationRef = nodeService.createNode(parentRef, ContentModel.ASSOC_CONTAINS, qName, ContentModel.TYPE_FOLDER, properties);
            childRef = childAssociationRef.getChildRef();
        }

        return childRef;
    }

    public void updateEntry (NodeRef entryRef, Map<QName, Serializable> properties) throws JSONException {
        for (Map.Entry<QName, Serializable> property : properties.entrySet())
            nodeService.setProperty(entryRef, property.getKey(), property.getValue());
    }

    public void deleteEntry (NodeRef entryRef) throws JSONException {
        nodeService.deleteNode(entryRef);
    }

    public JSONObject toJSON (NodeRef entryRef) throws JSONException {
        Map<QName, Serializable> properties = nodeService.getProperties(entryRef);
        for (Map.Entry<QName, Serializable> property : properties.entrySet())
        {
            if(property.getValue().getClass() == Date.class) {
                SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                TimeZone timeZone = TimeZone.getTimeZone("UTC");
                date.setTimeZone(timeZone);
                String dateString = date.format(property.getValue()) + "Z";
                property.setValue(dateString);
            }
        }

        return JSONUtils.getObject(properties);
    }

    public NodeRef getNodeRef(String uuid)
    {
        String nodeRefStr = StoreRef.STORE_REF_WORKSPACE_SPACESSTORE + "/" + uuid;
        return new NodeRef(nodeRefStr);
    }
}

