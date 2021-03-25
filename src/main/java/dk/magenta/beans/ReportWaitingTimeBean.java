package dk.magenta.beans;

import dk.magenta.model.DatabaseModel;
import dk.magenta.utils.QueryUtils;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.odftoolkit.simple.SpreadsheetDocument;
import org.odftoolkit.simple.style.Font;
import org.odftoolkit.simple.style.StyleTypeDefinitions;
import org.odftoolkit.simple.table.Cell;
import org.odftoolkit.simple.table.Table;
import org.odftoolkit.simple.table.TableContainer;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static dk.magenta.model.DatabaseModel.*;

public class ReportWaitingTimeBean {

    public void setOverride(boolean override) {
        this.override = override;
    }

    public void setOverride_months(int override_months) {
        this.override_months = override_months;
    }

    private boolean override;
    private int override_months;

    private TableContainer document;
    private Table table;

    public void setDatabaseBean(DatabaseBean databaseBean) {
        this.databaseBean = databaseBean;
    }

    private DatabaseBean databaseBean;

    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    private TransactionService transactionService;

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    private NodeService nodeService;

    public void setFileFolderService(FileFolderService fileFolderService) {
        this.fileFolderService = fileFolderService;
    }

    private FileFolderService fileFolderService;

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    private ContentService contentService;

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    private SearchService searchService;

    public void setEntryBean(EntryBean entryBean) {
        this.entryBean = entryBean;
    }

    private EntryBean entryBean;



    //
    public void getReport(String from, String to) {

        int receivedCount = this.query("declarationDate", from, to, false);
        System.out.println("receivedCount");
        System.out.println(receivedCount);

    }

    private void getNodesForReport(String f_formattedDate, String t_formattedDate) throws JSONException {

        // all decs that have been closed within this period

        // hvordan kan man se at en sag er afsluttet og hvornår det er sket?

        String siteShortName = "retspsyk";
        JSONArray queryArray = new JSONArray();

        JSONObject o = new JSONObject();
        o.put("value", QueryUtils.dateRangeQuery(f_formattedDate, t_formattedDate));

        queryArray.put(o);

        String type = databaseBean.getType("retspsyk");
        String query = QueryUtils.getKeyValueQuery(siteShortName, type, queryArray);
        query = query + " AND -ASPECT:\"rm:bua\"";

        List<NodeRef> nodeRefs = entryBean.getEntries(query, 0, 300, "@rm:creationDate", true);

    }

    private int query(String field, String startDate, String endDate, boolean bua) {

        String query = "";
        JSONArray queryArray = new JSONArray();

        try {

            JSONObject o = new JSONObject();

            o.put("key", field);
            o.put("value", QueryUtils.dateRangeQuery(startDate , endDate));
            o.put("include", true);
            queryArray.put(o);

            // return only closed cases
            o = new JSONObject();
            o.put("key", "closed");
            o.put("value", true);
            o.put("include", true);
            queryArray.put(o);

            query = QueryUtils.getKeyValueQuery(DatabaseModel.TYPE_PSYC_SITENAME, DatabaseModel.TYPE_PSYC_DEC, queryArray);

            if (!bua) {
                query = query + " AND -ASPECT:\"rm:bua\"";
            }
            else {
                query = query + " AND +ASPECT:\"rm:bua\"";
            }

            System.out.println("the query");
            System.out.println(query);

            List<NodeRef> nodeRefs = entryBean.getEntriesbyQuery(query);

            System.out.println("nodeRefs");
            System.out.println(nodeRefs.size());

            return nodeRefs.size();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

}
