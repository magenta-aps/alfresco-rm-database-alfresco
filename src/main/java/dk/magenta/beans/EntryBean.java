package dk.magenta.beans;

import com.sun.syndication.feed.rss.Content;
import dk.magenta.model.DatabaseModel;
import dk.magenta.utils.JSONUtils;
import dk.magenta.utils.TypeUtils;
import org.activiti.engine.impl.util.json.JSONArray;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.search.impl.solr.facet.FacetQueryProvider;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class EntryBean {

    private NodeService nodeService;
    private SiteService siteService;
    private SearchService searchService;
    private LockService lockService;

    public FileFolderService getFileFolderService() {
        return fileFolderService;
    }

    public void setFileFolderService(FileFolderService fileFolderService) {
        this.fileFolderService = fileFolderService;
    }

    private FileFolderService fileFolderService;

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }
    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }
    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }
    public void setLockService(LockService lockService) {
        this.lockService = lockService;
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

        //Set unique value and name
        properties.put(entryKeyQName, counter);
        properties.put(ContentModel.PROP_NAME, counter);

        //Create name for node (This is not displayed anywhere)
        QName nameQName = QName.createQName(DatabaseModel.CONTENT_MODEL_URI, counter.toString());

        //Get creation date folder to place the entry in
        String dateTimeStr = (String)properties.get(DatabaseModel.PROP_CREATION_DATE);
        String[] dateTime = dateTimeStr.split("T");
        String[] date = dateTime[0].split("-");
        String year = date[0];
        String month = date[1];
        String day = date[2];

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

        // add the contents of the template library

        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

        NodeRef nodeRef_templateFolder = siteService.getContainer(siteShortName, DatabaseModel.PROP_TEMPLATE_LIBRARY);

        List<ChildAssociationRef> children = nodeService.getChildAssocs(nodeRef_templateFolder);

        Iterator i = children.iterator();



        while (i.hasNext()) {

            ChildAssociationRef child = (ChildAssociationRef)i.next();
            try {
                fileFolderService.copy(child.getChildRef(), nodeRef, (String)nodeService.getProperty(child.getChildRef(), ContentModel.PROP_NAME));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

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

        String uri = DatabaseModel.RM_MODEL_URI;
        QName closed = QName.createQName(uri, "closed");
        Boolean closedProp = (Boolean)nodeService.getProperty(entryRef, closed);
        if(closedProp != null && closedProp) {
            AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
            lockEntry(entryRef);
        }
    }

    public void deleteEntry (NodeRef entryRef) { nodeService.deleteNode(entryRef); }

    private void lockEntry (NodeRef entryRef) {
        if (!nodeService.hasAspect(entryRef, ContentModel.ASPECT_LOCKABLE)) {
            Map<QName, Serializable> prop = new HashMap<>();
            nodeService.addAspect(entryRef, ContentModel.ASPECT_LOCKABLE, prop);
        }
        lockService.lock(entryRef, LockType.READ_ONLY_LOCK);
    }

    //TODO: Make this generic. Atm this only work with forensicDeclarations
    public void unlockEntry (NodeRef entryRef) {
        lockService.unlock(entryRef);
        String uri = DatabaseModel.RM_MODEL_URI;
        QName closed = QName.createQName(uri, "closed");
        QName closedWithoutDeclaration = QName.createQName(uri, "closedWithoutDeclaration");
        QName closedWithoutDeclarationReason = QName.createQName(uri, "closedWithoutDeclarationReason");
        QName closedWithoutDeclarationSentTo = QName.createQName(uri, "closedWithoutDeclarationSentTo");
        nodeService.setProperty(entryRef, closed, false);
        nodeService.setProperty(entryRef, closedWithoutDeclaration, null);
        nodeService.setProperty(entryRef, closedWithoutDeclarationReason, null);
        nodeService.setProperty(entryRef, closedWithoutDeclarationSentTo, null);
    }

    public JSONObject toJSON (NodeRef entryRef) throws JSONException {
        Map<QName, Serializable> properties = nodeService.getProperties(entryRef);
        return JSONUtils.getObject(properties);
    }

    public NodeRef getNodeRef(String uuid)
    {
        String nodeRefStr = StoreRef.STORE_REF_WORKSPACE_SPACESSTORE + "/" + uuid;
        return new NodeRef(nodeRefStr);
    }

    public NodeRef getEntry(String query)
    {
        ResultSet resultSet = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "lucene", query);
        Iterator<ResultSetRow> iterator = resultSet.iterator();

        if(iterator.hasNext()) {
            ResultSetRow result = iterator.next();
            return result.getNodeRef();
        }
        else return null;
    }

    public JSONObject calculatePassive(NodeRef entryKey) {


        Date creation = (Date) nodeService.getProperty(entryKey, DatabaseModel.PROP_CREATION_DATE);
        Date observation = (Date) nodeService.getProperty(entryKey, DatabaseModel.PROP_OBSERVATION_DATE);

        System.out.println(creation);
        System.out.println(observation);


        if (creation == null || observation == null) {
            return JSONUtils.getError(new Exception("missing observation or creation date"));
        }


        boolean locked = lockService.isLockedAndReadOnly(entryKey);

        if (locked) {
            lockService.unlock(entryKey);
        }



        Date timePoint = (Date) nodeService.getProperty(entryKey, DatabaseModel.PROP_CREATION_DATE);
        LocalDate creationDate = timePoint.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        timePoint = (Date)nodeService.getProperty(entryKey, DatabaseModel.PROP_OBSERVATION_DATE);
        LocalDate observationDate = timePoint.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        long waitingTime = creationDate.until(observationDate,ChronoUnit.DAYS );

        nodeService.setProperty(entryKey, DatabaseModel.PROP_WAITING_PASSIVE, waitingTime);

        if (locked) {
            lockService.lock(entryKey, LockType.READ_ONLY_LOCK);
        }

        return JSONUtils.getSuccess();
    }

    public JSONObject calculateActive(NodeRef entryKey) {

        Date declaration = (Date) nodeService.getProperty(entryKey, DatabaseModel.PROP_DECLARATION_DATE);
        Date observation = (Date) nodeService.getProperty(entryKey, DatabaseModel.PROP_OBSERVATION_DATE);

        System.out.println(declaration);
        System.out.println(observation);


        if (declaration == null || observation == null) {
            return JSONUtils.getError(new Exception("missing declaration or observation date"));
        }



        boolean locked = lockService.isLockedAndReadOnly(entryKey);

        if (locked) {
            lockService.unlock(entryKey);
        }



        Date timePoint = (Date) nodeService.getProperty(entryKey, DatabaseModel.PROP_DECLARATION_DATE);
        LocalDate declarationDate = timePoint.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        timePoint = (Date)nodeService.getProperty(entryKey, DatabaseModel.PROP_OBSERVATION_DATE);
        LocalDate observationDate = timePoint.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        long waitingTime = observationDate.until(declarationDate,ChronoUnit.DAYS );

        nodeService.setProperty(entryKey, DatabaseModel.PROP_WAITING_ACTIVE, waitingTime);


        if (locked) {
            lockService.lock(entryKey, LockType.READ_ONLY_LOCK);
        }

        return JSONUtils.getSuccess();
    }

    public JSONObject calculateTotal(NodeRef entryKey) {


        Date creation = (Date) nodeService.getProperty(entryKey, DatabaseModel.PROP_CREATION_DATE);
        Date declaration = (Date) nodeService.getProperty(entryKey, DatabaseModel.PROP_DECLARATION_DATE);


        if (creation == null || declaration == null) {
            return JSONUtils.getError(new Exception("missing observation or creation date"));
        }


        boolean locked = lockService.isLockedAndReadOnly(entryKey);

        if (locked) {
            lockService.unlock(entryKey);
        }


        Date timePoint = (Date) nodeService.getProperty(entryKey, DatabaseModel.PROP_CREATION_DATE);
        LocalDate creationDate = timePoint.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        timePoint = (Date)nodeService.getProperty(entryKey, DatabaseModel.PROP_DECLARATION_DATE);
        LocalDate declarationDate = timePoint.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        long waitingTime = creationDate.until(declarationDate,ChronoUnit.DAYS );

        nodeService.setProperty(entryKey, DatabaseModel.PROP_WAITING_TOTAL, waitingTime);


        if (locked) {
            lockService.lock(entryKey, LockType.READ_ONLY_LOCK);
        }


        return JSONUtils.getSuccess();
    }





    public List<NodeRef> getEntries(String query, int skip, int maxItems, String sort, boolean desc) {

        SearchParameters sp = new SearchParameters();
        sp.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);



        sp.setLanguage("lucene");
        sp.setQuery(query);

        sp.setMaxItems(maxItems);
        sp.setSkipCount(skip);
        sp.addSort(sort, desc);
        ResultSet resultSet = searchService.query(sp);


        System.out.println("the query.... ****");
        System.out.println(sp.getQuery());

        return resultSet.getNodeRefs();
    }

    public Set<NodeRef> getEntries (String siteShortName) throws JSONException {
        NodeRef docLibRef = siteService.getContainer(siteShortName, SiteService.DOCUMENT_LIBRARY);
        return getEntries(docLibRef, 4);
    }

    private Set<NodeRef> getEntries(NodeRef parentNodeRef, int levels) {
        List<ChildAssociationRef> childrenRefs = nodeService.getChildAssocs(parentNodeRef);
        Set<NodeRef> result = new HashSet<>();

        levels--;
        for (ChildAssociationRef childRef : childrenRefs) {
            NodeRef nodeRef = childRef.getChildRef();
            if(levels == 0)
                result.add(nodeRef);
            else {
                Set<NodeRef> entries = getEntries(nodeRef, levels);
                if (entries != null)
                    result.addAll(entries);
            }
        }
        return result;
    }

    public ArrayList getNotClosedEntries (String siteShortName) throws JSONException {
        NodeRef docLibRef = siteService.getContainer(siteShortName, SiteService.DOCUMENT_LIBRARY);
        return this.getNotClosedEntries(docLibRef, 4);
    }

    private ArrayList getNotClosedEntries(NodeRef parentNodeRef, int levels) {
        List<ChildAssociationRef> childrenRefs = nodeService.getChildAssocs(parentNodeRef);
        ArrayList result = new ArrayList();

        levels--;
        for (ChildAssociationRef childRef : childrenRefs) {
            NodeRef nodeRef = childRef.getChildRef();
            if (levels == 0) {
                Serializable closed =  nodeService.getProperty(nodeRef, DatabaseModel.PROP_CLOSED);


                // when a case is created, the property closed is missing
                if (closed == null) {
                    result.add(nodeRef);
                }
                else {

                    boolean isclosed = (Boolean)closed;

                    if (isclosed == false) {
                        result.add(nodeRef);
                    }
                }
            }
            else {
                ArrayList entries = getNotClosedEntries(nodeRef, levels);
                if (entries != null)
                    result.addAll(entries);
            }
        }
        return result;
    }
}

