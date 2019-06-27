package dk.magenta.beans;

import dk.magenta.model.DatabaseModel;
import dk.magenta.utils.QueryUtils;
import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.odftoolkit.odfdom.pkg.OdfFileDom;
import org.odftoolkit.simple.SpreadsheetDocument;
import org.odftoolkit.simple.table.Table;
import org.odftoolkit.simple.table.TableContainer;

public class StatBean {


    private TableContainer document;
    private Table table;

    public void setDatabaseBean(DatabaseBean databaseBean) {
        this.databaseBean = databaseBean;
    }

    private DatabaseBean databaseBean;

    public void setEntryBean(EntryBean entryBean) {
        this.entryBean = entryBean;
    }

    private EntryBean entryBean;

    public int query(String field) {
        String query = "";
        JSONArray queryArray = new JSONArray();

        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);

        try {

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime onemonthback = now.minusMonths(1);

            System.out.println("now");
            System.out.println(now);
            System.out.println(onemonthback);

            JSONObject o = new JSONObject();

            String from_formattedDate = outputFormatter.format(now);
            String to_formattedDate = outputFormatter.format(onemonthback);

            o.put("key", field);
            o.put("value", QueryUtils.dateRangeQuery(to_formattedDate, from_formattedDate));
            o.put("include", true);
            queryArray.put(o);

            query = QueryUtils.getKeyValueQuery(DatabaseModel.TYPE_PSYC_SITENAME, DatabaseModel.TYPE_PSYC_DEC, queryArray);

            query = query + " AND -ASPECT:\"rm:bua\"";

            System.out.println("the query");
            System.out.println(query);

            List<NodeRef> nodeRefs = entryBean.getEntriesbyQuery(query);

            return nodeRefs.size();


        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void writeToDocument()   {

        try {
            SpreadsheetDocument document = SpreadsheetDocument.newSpreadsheetDocument();

            table = document.getSheetByIndex(0);
            Table.newTable(this.document);
        }
        catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }

    }
}