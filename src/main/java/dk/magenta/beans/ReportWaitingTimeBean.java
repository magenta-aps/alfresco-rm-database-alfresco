package dk.magenta.beans;

import dk.magenta.model.DatabaseModel;
import dk.magenta.utils.QueryUtils;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.odftoolkit.simple.SpreadsheetDocument;
import org.odftoolkit.simple.style.Font;
import org.odftoolkit.simple.style.StyleTypeDefinitions;
import org.odftoolkit.simple.table.Cell;
import org.odftoolkit.simple.table.Column;
import org.odftoolkit.simple.table.Table;
import org.odftoolkit.simple.table.TableContainer;
import org.odftoolkit.simple.text.TextHyperlink;

import java.io.File;
import java.io.Serializable;
import java.net.URI;
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

    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    private AuthenticationService authenticationService;

    public void setDatabaseBean(DatabaseBean databaseBean) {
        this.databaseBean = databaseBean;
    }

    private DatabaseBean databaseBean;

    private Properties properties;

    public void setProperties(Properties properties)
    {
        this.properties = properties;
    }

    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    private TransactionService transactionService;

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    private SiteService siteService;

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




    public NodeRef getReport(String from, String to) throws Exception {

        List<NodeRef> nodeRefs = this.query("declarationDate", from, to, false);
        System.out.println("nodeRefs");
        System.out.println(nodeRefs);

        NodeRef spreadSheet = setupSpreadSheet(nodeRefs);




        return spreadSheet;
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

    private List<NodeRef> query(String field, String startDate, String endDate, boolean bua) {

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

//            List<NodeRef> nodeRefs = entryBean.getEntriesbyQuery(query);
            List<NodeRef> nodeRefs = entryBean.getEntries(query,0,1000,"@rm:waiting_active", true);

            System.out.println("nodeRefs");
            System.out.println(nodeRefs.size());

            return nodeRefs;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private NodeRef setupSpreadSheet(List<NodeRef> nodeRefs) throws Exception {


        System.out.println(properties.getProperty("magenta.custom.override"));
        System.out.println(properties.getProperty("magenta.custom.override"));
        System.out.println(properties.getProperty("magenta.custom.override"));
        System.out.println(properties.getProperty("magenta.custom.override"));
        System.out.println(properties.getProperty("magenta.custom.override"));



        SpreadsheetDocument document = SpreadsheetDocument.newSpreadsheetDocument();
        table = document.getSheetByIndex(0);

        Cell sagsnr = table.getCellByPosition(0, 0);
        sagsnr.setStringValue("Sagsnr");
        Font fn = sagsnr.getFont();
        fn.setFontStyle(StyleTypeDefinitions.FontStyle.BOLD);



        Cell cpr = table.getCellByPosition(1, 0);
        cpr.setStringValue("Cpr");
        Font fnt = cpr.getFont();
        fnt.setFontStyle(StyleTypeDefinitions.FontStyle.BOLD);




        Cell aktivventetid = table.getCellByPosition(2, 0);
//        aktivventetid.setStringValue("Aktiv ventetid");
        Font fnc = cpr.getFont();
        fnc.setFontStyle(StyleTypeDefinitions.FontStyle.BOLD);
        aktivventetid.addParagraph("Aktiv ventetid").setFont(fnc);

        Cell link = table.getCellByPosition(3, 0);
        link.setStringValue("Links til sagen");

        Font fc = cpr.getFont();
        fc.setFontStyle(StyleTypeDefinitions.FontStyle.BOLD);

        System.out.println("aktivventetid.getFont().getFontStyle()");
        System.out.println(aktivventetid.getFont().getFontStyle());



        int nextRow = 0;
        for (int i =0; i <= nodeRefs.size()-1; i++) {

            NodeRef nodeRef = nodeRefs.get(i);

            int sagsNummer = (Integer) nodeService.getProperty(nodeRef, PROP_CASE_NUMBER);
            String cprNummer = (String) nodeService.getProperty(nodeRef, PROP_CPR);

            cprNummer = cprNummer.substring(0,6) + "-" + cprNummer.substring(6,10);


            entryBean.calculateActive(nodeRef);


            int aktivVentetid = (Integer) nodeService.getProperty(nodeRef, PROP_WAITING_ACTIVE);

            nextRow = nextRow+1;


            Cell sagsnrValue = table.getCellByPosition(0, nextRow);
            sagsnrValue.setStringValue(String.valueOf(sagsNummer));

            Cell cprValue = table.getCellByPosition(1, nextRow);
            cprValue.setStringValue(String.valueOf(cprNummer));

            Cell aktivventetidValue = table.getCellByPosition(2, nextRow);
            aktivventetidValue.setStringValue(String.valueOf(aktivVentetid));

            Cell urlValue = table.getCellByPosition(3, nextRow);
            URI uri = new URI("http://0.0.0.0:7674/#!/erklaeringer/sag/" + sagsNummer + "/patientdata");
            urlValue.addParagraph("").appendHyperlink("klik for at åbne sagen", uri);

        }

        table.getColumnList().get(0).setWidth(20);
        table.getColumnList().get(1).setWidth(40);
        table.getColumnList().get(2).setWidth(40);
        table.getColumnList().get(3).setWidth(40);





        NodeRef tmpFolder = siteService.getContainer("retspsyk", DatabaseModel.PROP_TMP);

        Map<QName, Serializable> properties = new HashMap<>();

//        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
//        LocalDateTime now = LocalDateTime.now();
//        System.out.println(dtf.format(now));
//
//        String stamp = dtf.format(now);
//
//        stamp = stamp.replace("/","_");



        // check if there is already a tmp node for this user

        // get user
        String username = authenticationService.getCurrentUserName();

        // check if any node with aspect and value

        String expression = username + DatabaseModel.DEFAULT_POST_ACTIVE_REPORT_TEXT;


        NodeRef child = nodeService.getChildByName(tmpFolder, ContentModel.ASSOC_CONTAINS, expression);
        ChildAssociationRef childAssociationRef = null;

        if (child == null) {
            QName qName = QName.createQName(DatabaseModel.CONTENT_MODEL_URI, DatabaseModel.AKTIV_REPORT_SPREADSHEET_NAME);
            properties.put(ContentModel.PROP_NAME, expression);

            child = nodeService.createNode(tmpFolder, ContentModel.ASSOC_CONTAINS, qName, ContentModel.TYPE_CONTENT, properties).getChildRef();
        }

        ContentWriter writer = contentService.getWriter(child, ContentModel.PROP_CONTENT, true);
        File f = new File("tmp");

        document.save(f);
        writer.putContent(f);
        return child;
    }

}
