package dk.magenta.beans;

import com.sun.syndication.feed.rss.Content;
import dk.magenta.model.DatabaseModel;
import dk.magenta.utils.JSONUtils;
import dk.magenta.utils.TypeUtils;
import net.sf.acegisecurity.Authentication;
import net.sf.cglib.core.Local;
import org.activiti.engine.impl.util.json.JSONArray;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.search.impl.solr.facet.FacetQueryProvider;
import org.alfresco.repo.audit.AuditComponent;
import org.alfresco.repo.audit.AuditComponentImpl;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.orm.jpa.vendor.Database;

import javax.xml.crypto.Data;
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
    private AuditComponent auditComponent;

    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    private AuthenticationService authenticationService;

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
    public void setAuditComponent(AuditComponent auditComponent) {
        this.auditComponent = auditComponent;
    }

    public NodeRef addEntry (String siteShortName, String type, Map<QName, Serializable> properties, boolean bua) throws JSONException {

        //Get counter for this site document library
        NodeRef docLibRef = siteService.getContainer(siteShortName, SiteService.DOCUMENT_LIBRARY);
        Integer counter;

        // reuse and old counter

        // if any avail tjek PROP_FREE_CASENUMBERS på docLibRef for bua/ikkebua
        int nextCaseNumber = 0;
        if (bua) {

        }
        else {
//            nextCaseNumber = this.reuseDeletedeCaseNumbers();
            nextCaseNumber = 0;
        }

        if (nextCaseNumber != 0) {
            counter = nextCaseNumber;
        }
        // else, kør normal som nedenfor
        else {

            if (bua) {
                counter = (Integer) nodeService.getProperty(docLibRef, DatabaseModel.PROP_BUA_COUNTER);
            }
            else {
                counter = (Integer) nodeService.getProperty(docLibRef, ContentModel.PROP_COUNTER);
            }


            if(counter == null)
                counter = 50000;

            counter++;

            //Increment the site document library counter when the entry has been created successfully

            if (bua) {
                nodeService.setProperty(docLibRef, DatabaseModel.PROP_BUA_COUNTER, counter);
            }
            else {
                nodeService.setProperty(docLibRef, ContentModel.PROP_COUNTER, counter);
            }
        }

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





        // add the contents of the documenttemplate library

        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

        NodeRef nodeRef_documentsTemplateFolder = siteService.getContainer(siteShortName, DatabaseModel.PROP_TEMPLATE_LIBRARY);

        List<ChildAssociationRef> children = nodeService.getChildAssocs(nodeRef_documentsTemplateFolder);

        Iterator i = children.iterator();

        while (i.hasNext()) {

            ChildAssociationRef child = (ChildAssociationRef)i.next();
            try {
                FileInfo newNode = fileFolderService.copy(child.getChildRef(), nodeRef, (String)nodeService.getProperty(child.getChildRef(), ContentModel.PROP_NAME));
                nodeService.addAspect(newNode.getNodeRef(),ContentModel.ASPECT_HIDDEN,null);


            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        // add the contents of the documenttemplate library

        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

        NodeRef nodeRef_foldersTemplatesFolder = siteService.getContainer(siteShortName, DatabaseModel.PROP_FOLDER_TEMPLATE_LIBRARY);

        children = nodeService.getChildAssocs(nodeRef_foldersTemplatesFolder);

        i = children.iterator();

        while (i.hasNext()) {

            ChildAssociationRef child = (ChildAssociationRef)i.next();
            try {
                FileInfo newNode = fileFolderService.copy(child.getChildRef(), nodeRef, (String)nodeService.getProperty(child.getChildRef(), ContentModel.PROP_NAME));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        // add the ASPECT_BUA if the declaration is of that type

        if (bua) {
            nodeService.addAspect(nodeRef, DatabaseModel.ASPECT_BUA,null);
        }

        if (properties.containsKey(DatabaseModel.PROP_FLOWCHART_FLAG)) {
            nodeService.addAspect(nodeRef, DatabaseModel.ASPECT_REDFLAG, null);
        }

        return nodeRef;
    }

    public NodeRef addEntry_import (String siteShortName, String type, Map<QName, Serializable> properties, boolean bua) throws JSONException {

        String oldcaseid = (String)properties.get(DatabaseModel.PROP_CASE_NUMBER_OLD);

        //Get counter for this site document library
        NodeRef docLibRef = siteService.getContainer(siteShortName, SiteService.DOCUMENT_LIBRARY);
        Integer counter = Integer.parseInt(oldcaseid);

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

        // add the contents of the template library

        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

        NodeRef nodeRef_templateFolder = siteService.getContainer(siteShortName, DatabaseModel.PROP_TEMPLATE_LIBRARY);

        List<ChildAssociationRef> children = nodeService.getChildAssocs(nodeRef_templateFolder);

        Iterator i = children.iterator();



        while (i.hasNext()) {

            ChildAssociationRef child = (ChildAssociationRef)i.next();
            try {
                FileInfo newNode = fileFolderService.copy(child.getChildRef(), nodeRef, (String)nodeService.getProperty(child.getChildRef(), ContentModel.PROP_NAME));
                nodeService.addAspect(newNode.getNodeRef(),ContentModel.ASPECT_HIDDEN,null);


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

        String currentUser = authenticationService.getCurrentUserName();
        Serializable locked_for_edit = nodeService.getProperty(entryRef, DatabaseModel.PROP_LOCKED_FOR_EDIT);
        Serializable locked_for_edit_by = nodeService.getProperty(entryRef, DatabaseModel.PROP_LOCKED_FOR_EDIT_BY);


        // initialize locked_for_edit
        if (locked_for_edit == null) {
            locked_for_edit = false;
        }

        if ((boolean)locked_for_edit) {
            // check if its the same user that edits that holds the lock
            if (!((String)locked_for_edit_by).equals(currentUser)) {
                System.out.println(currentUser + "not allowed to update as case is locked by " + locked_for_edit_by);
                return;
            }
        }

        if (!nodeService.hasAspect(entryRef, DatabaseModel.ASPECT_FLOWCHART)) {
            nodeService.addAspect(entryRef, DatabaseModel.ASPECT_FLOWCHART, null);
        }


        boolean erklaringdate = false;



        boolean closedAfterTempEditing = false;

        // updating the properties
        for (Map.Entry<QName, Serializable> property : properties.entrySet()) {

            // handle empty postcode, fix for redmine #47807
            if (property.getKey().equals(DatabaseModel.PROP_POSTCODE)) {
                if (property.getValue().equals("")) {
                    nodeService.setProperty(entryRef, property.getKey(), null);
                }
                else {
                    nodeService.setProperty(entryRef, property.getKey(), property.getValue());
                }
            }
            else if (property.getKey().equals(DatabaseModel.PROP_DECLARATION_DATE)) {

                System.out.println("hvad er property.getValue()");
                System.out.println(property.getValue());

                if (property.getValue().equals("null")) {
                    nodeService.removeProperty(entryRef, property.getKey());
                }
                else {
                    nodeService.setProperty(entryRef, property.getKey(), property.getValue());
                }
            }
            else {
                nodeService.setProperty(entryRef, property.getKey(), property.getValue());
            }

            if (property.getKey().equals(DatabaseModel.PROP_DECLARATION_DATE)) {
                erklaringdate = true;
            }

            if (property.getKey().equals(DatabaseModel.PROP_FLOWCHART_FLAG)) {

                if (property.getValue().equals("true")) {
                    nodeService.addAspect(entryRef, DatabaseModel.ASPECT_REDFLAG, null);
                }
                else {
                    nodeService.removeAspect(entryRef, DatabaseModel.ASPECT_REDFLAG);
                }
            }

            if (property.getKey().equals(DatabaseModel.PROP_CLOSECASEBUTTONPRESSED)) {
                if (property.getValue().equals("true")) {
                    closedAfterTempEditing = true;
                }
            }
        }

        String uri = DatabaseModel.RM_MODEL_URI;
        QName closed = QName.createQName(uri, "closed");
        Boolean closedProp = (Boolean) nodeService.getProperty(entryRef, closed);

        if (closedProp != null && closedProp) {

            AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
            this.addMetaData(entryRef);

            if (!nodeService.hasAspect(entryRef, DatabaseModel.ASPECT_SUPOPL) && !nodeService.hasAspect(entryRef, DatabaseModel.ASPECT_SKIPFLOW) && !nodeService.hasAspect(entryRef, DatabaseModel.ASPECT_OPENEDIT) || closedAfterTempEditing) {
                lockEntry(entryRef);
            }

        }

        // #49794
        // check if datefield "erklæaring afgivet is set - then close the case # 31284
//        else if (closedProp == null && erklaringdate) {
//            nodeService.setProperty(entryRef, DatabaseModel.PROP_CLOSED, true);
//            AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
//            this.addMetaData(entryRef);
//
//            if (!nodeService.hasAspect(entryRef, DatabaseModel.ASPECT_SUPOPL) && !nodeService.hasAspect(entryRef, DatabaseModel.ASPECT_SKIPFLOW) && !nodeService.hasAspect(entryRef, DatabaseModel.ASPECT_OPENEDIT ) ) {
//                lockEntry(entryRef);
//            }
//        }
    }

    private void addMetaData(NodeRef entry) {



        if (nodeService.getProperty(entry, DatabaseModel.PROP_CLOSED_WITHOUT_DECLARATION) != null) {
            // if closedWithoutDeclaration - check if need to add ASPECT_RETURNDATEFORDECLARATION
            boolean  reason = (boolean)nodeService.getProperty(entry, DatabaseModel.PROP_CLOSED_WITHOUT_DECLARATION);
            if (reason) {
                Date returnDate = (Date)nodeService.getProperty(entry, DatabaseModel.PROP_RETURNOFDECLARATIONDATE);

                Map<QName, Serializable> prop = new HashMap<>();
                prop.put(DatabaseModel.PROP_RETURNOFDECLARATIONDATE, returnDate);
                nodeService.addAspect(entry, DatabaseModel.ASPECT_RETURNDATEFORDECLARATION,prop);
            }
        }


    }

    // used for converting data from the old system
    public NodeRef updateProperty (String caseid, Map<QName, Serializable> properties) throws JSONException {

//        String query = "@rm\\:caseNumber:\"" + caseid + "\"";
//
//        System.out.println("hvad er query:" + query);
//
//        NodeRef n = this.getEntry(query);
//        System.out.println("hvad er nodeRef" + n);
//
//
//        boolean temporaryUnlocked = false;
//
//        if (lockService.isLocked(n)) {
//            lockService.unlock(n);
//            temporaryUnlocked = true;
//        }
//
//        this.updateEntry(n,properties);
//
//        if (temporaryUnlocked) {
//            lockService.lock(n, LockType.READ_ONLY_LOCK);
//        }

        return null;
    }


    public void deleteEntry (NodeRef entryRef) { nodeService.deleteNode(entryRef); }


    public void undoCloseCase(NodeRef entryRef) {
        if (lockService.isLocked(entryRef)) {
                lockService.unlock(entryRef);
        }

        // remove the closedDate property and set closeddate to null; https://redmine.magenta-aps.dk/issues/46977
        nodeService.removeProperty(entryRef, DatabaseModel.PROP_CLOSED);
        nodeService.setProperty(entryRef, DatabaseModel.PROP_CLOSED_DATE, null);
    }

    private void lockEntry (NodeRef entryRef) {




        if (!lockService.getLockStatus(entryRef).equals(LockStatus.LOCKED)) {

            if (!nodeService.hasAspect(entryRef, ContentModel.ASPECT_LOCKABLE)) {
                Map<QName, Serializable> prop = new HashMap<>();
                prop.put(DatabaseModel.PROP_CLOSED_DATE, new Date());
                nodeService.addAspect(entryRef, ContentModel.ASPECT_LOCKABLE, prop);
            }
            // cleanup after a possible temporary editing of the case (#34257, 40320)
            if (nodeService.hasAspect(entryRef, DatabaseModel.ASPECT_SKIPFLOW)) {
                nodeService.removeAspect(entryRef, DatabaseModel.ASPECT_SKIPFLOW);
            }
            if (nodeService.hasAspect(entryRef, DatabaseModel.ASPECT_SUPOPL)) {
                nodeService.removeAspect(entryRef, DatabaseModel.ASPECT_SUPOPL);
            }
            if (nodeService.hasAspect(entryRef, DatabaseModel.ASPECT_OPENEDIT)) {
                nodeService.removeAspect(entryRef, DatabaseModel.ASPECT_OPENEDIT);
            }

            lockService.lock(entryRef, LockType.READ_ONLY_LOCK);
        }



    }

    //TODO: Make this generic. Atm this only work with forensicDeclarations
    public void unlockEntry (NodeRef entryRef, String mode) {

        // bugfix for #37857
        if (lockService.isLocked(entryRef)) {
            lockService.unlock(entryRef);
        }

//        String uri = DatabaseModel.RM_MODEL_URI;
//        QName closed = QName.createQName(uri, "closed");
//        QName closedWithoutDeclaration = QName.createQName(uri, "closedWithoutDeclaration");
//        QName closedWithoutDeclarationReason = QName.createQName(uri, "closedWithoutDeclarationReason");
//        QName closedWithoutDeclarationSentTo = QName.createQName(uri, "closedWithoutDeclarationSentTo");

        // 34111 - removing the property makes the declaration reappear in the flowchart
        // 40320 - they decided to change the behaviour when reopening a decl. Never change close status as it messes up with the statistics
        //         this is the reason why the next line has been commented out again.
        // nodeService.removeProperty(entryRef, closed);

//        nodeService.setProperty(entryRef, closedWithoutDeclaration, null);
//        nodeService.setProperty(entryRef, closedWithoutDeclarationReason, null);
//        nodeService.setProperty(entryRef, closedWithoutDeclarationSentTo, null);

        if (mode.equals(DatabaseModel.PROP_UNLOCK_FOR_EDIT)) {
            nodeService.addAspect(entryRef, DatabaseModel.ASPECT_OPENEDIT,null);
        }
        else  if (mode.equals(DatabaseModel.PROP_UNLOCK_FOR_SUPPOPL)) {
            nodeService.addAspect(entryRef, DatabaseModel.ASPECT_SUPOPL, null);

        }
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
            logEntryReadToAudit();
            return result.getNodeRef();
        }
        else return null;
    }

    public JSONObject calculatePassive(NodeRef entryKey) {


        Date creation = (Date) nodeService.getProperty(entryKey, DatabaseModel.PROP_CREATION_DATE);
        Date observation = (Date) nodeService.getProperty(entryKey, DatabaseModel.PROP_OBSERVATION_DATE);




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


    public List<NodeRef> getEntriesbyQuery(String query) {

        SearchParameters sp = new SearchParameters();
        sp.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);

        sp.setLanguage("lucene");
        sp.setQuery(query);

        ResultSet resultSet = searchService.query(sp);

        return resultSet.getNodeRefs();
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
                Serializable closed = nodeService.getProperty(nodeRef, DatabaseModel.PROP_CLOSED);


                // when a case is created, the property closed is missing
                if (closed == null) {
                    result.add(nodeRef);
                } else {

                    boolean isclosed = (Boolean) closed;

                    if (isclosed == false) {
                        result.add(nodeRef);
                    }
                }
            } else {
                ArrayList entries = getNotClosedEntries(nodeRef, levels);
                if (entries != null)
                    result.addAll(entries);
            }
        }
        return result;
    }
    private void logEntryReadToAudit() {
        String root = "/alfresco-access";
        Map<String, Serializable> map = new HashMap<>();
        map.put("/transaction/action", "READ");
        map.put("/transaction/sub-actions", "readContent");
        map.put("/transaction/type", "rm:forensicPsychiatryDeclaration");
        map.put("/transaction/user", "alexander");
        map.put("/transaction/path", "/app:company_home/st:sites/cm:retspsyk/cm:documentLibrary");
        auditComponent.recordAuditValues(root, map);
    }

    // returns a number if any available, otherwise return null
    private int reuseDeletedeCaseNumbers() {

        // get property PROP_FREE_CASENUMBERS
        NodeRef docLibRef = siteService.getContainer("retspsyk", SiteService.DOCUMENT_LIBRARY);

        if (nodeService.getProperty(docLibRef, DatabaseModel.PROP_FREE_CASENUMBERS) != null) {
            String caseNumbers = (String) nodeService.getProperty(docLibRef, DatabaseModel.PROP_FREE_CASENUMBERS);

            if (!caseNumbers.equals("")) {
                List<String> list = new ArrayList<String>(Arrays.asList(caseNumbers.split(",")));

                String next = list.remove(0).trim();

                if (!next.equals("")) {
                    int number = Integer.parseInt(next);
                    nodeService.setProperty(docLibRef, DatabaseModel.PROP_FREE_CASENUMBERS, String.join(",", list));

                    return number;
                }
                else {
                    return 0;
                }
            }
            else {
                return 0;
            }


        }
        else {
            return 0;
        }
    }


}

