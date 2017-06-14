package dk.magenta.beans;

import dk.magenta.model.DatabaseModel;
import dk.magenta.utils.JSONUtils;
import dk.magenta.utils.QueryUtils;
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
import org.apache.xmlbeans.impl.xb.xmlconfig.Qnameconfig;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
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

        StoreRef storeRef = StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;
        ResultSet resultSet = searchService.query(storeRef, "lucene", query);
        Iterator<ResultSetRow> iterator = resultSet.iterator();

        Set<NodeRef> nodeRefs = new HashSet<>();
        while (iterator.hasNext()) {
            ResultSetRow result = iterator.next();
            nodeRefs.add(result.getNodeRef());
        }
        return nodeRefs;
    }

    public NodeRef getEntry (String type, String entryKey, String entryValue) throws JSONException {

        String query = QueryUtils.getEntryQuery(type, entryKey, entryValue);
        Set<NodeRef> nodeRefs = getEntries(query);
        if (nodeRefs.iterator().hasNext())
            return nodeRefs.iterator().next();
        else
            return null;
    }

    public JSONObject addEntry (String siteShortName, QName type, Map<QName, Serializable> properties) throws JSONException {

        //Get counter for this site document library
        NodeRef docLibRef = siteService.getContainer(siteShortName, SiteService.DOCUMENT_LIBRARY);
        Integer counter = (Integer) nodeService.getProperty(docLibRef, ContentModel.PROP_COUNTER);
        if(counter == null)
            counter = 0;
        counter++;

        //Remove value if set
        if (properties.containsKey(DatabaseModel.PROP_CASE_NUMBER))
            properties.remove(DatabaseModel.PROP_CASE_NUMBER);

        //Set unique value
        properties.put(DatabaseModel.PROP_CASE_NUMBER, counter);

        //Create name for node (This is not displayed anywhere)
        QName qName = QName.createQName(DatabaseModel.CONTENT_MODEL_URI, counter.toString());

        //Get current date folder to place the entry in
        LocalDate date = LocalDate.now();
        String year = ((Integer)date.getYear()).toString();
        String month = ((Integer)date.getMonthValue()).toString();
        String day = ((Integer)date.getDayOfMonth()).toString();

        NodeRef yearRef = getOrCreateChildByName(docLibRef, year);
        NodeRef monthRef = getOrCreateChildByName(yearRef, month);
        NodeRef dayRef = getOrCreateChildByName(monthRef, day);

        //Create entry
        nodeService.createNode(dayRef, ContentModel.ASSOC_CONTAINS, qName, type, properties);

        //Increment the site document library counter when the entry has been created successfully
        nodeService.setProperty(docLibRef, ContentModel.PROP_COUNTER, counter);

        return JSONUtils.getSuccess();
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

    public JSONObject updateEntry (NodeRef entryRef, Map<QName, Serializable> properties) throws JSONException {
        if(entryRef != null) {
            for(Map.Entry<QName, Serializable> property : properties.entrySet()) {
                nodeService.setProperty(entryRef, property.getKey(), property.getValue());
            }
        }
        return JSONUtils.getObject("error", "Entry with nodeRef (" + entryRef.getId() + ") does not exist.");
    }

    public JSONObject deleteEntry (NodeRef entryRef) throws JSONException {
        if(entryRef != null) {
            nodeService.deleteNode(entryRef);
        }
        return JSONUtils.getObject("error", "Entry with nodeRef (" + entryRef.getId() + ") does not exist.");
    }

    public JSONObject toJSON (NodeRef entryRef) throws JSONException {
        if(entryRef != null) {
            Map<QName, Serializable> properties = nodeService.getProperties(entryRef);
            return JSONUtils.getObject(properties);
        }
        return JSONUtils.getObject("error", "Entry with nodeRef (" + entryRef.getId() + ") does not exist.");
    }
}
