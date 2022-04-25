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

    public void setMailBean(MailBean mailBean) {
        this.mailBean = mailBean;
    }

    private MailBean mailBean;

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



    public boolean sendMail() throws Exception {
        return transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime nowMinus14 = LocalDateTime.now().minusDays(14);


            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);
            String to_formattedDate = outputFormatter.format(now);
            String from_formattedDate = outputFormatter.format(nowMinus14);

            NodeRef report = this.getReport(from_formattedDate, to_formattedDate, "");

            NodeRef[] attachmentList = new NodeRef[1];
            attachmentList[0] = report;

            mailBean.sendEmailNoTransform(attachmentList,"ps.o.faelles.post@rm.dk", "rapport", "");
            return true;
        });
    }


    public NodeRef getReport(String from, String to, String statusCriteria) throws Exception {

        List<NodeRef> nodeRefs = this.query("declarationDate", from, to, false, statusCriteria);
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

    private List<NodeRef> query(String field, String startDate, String endDate, boolean bua, String statusCriteria) {

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

            // https://redmine.magenta-aps.dk/issues/37799 - filter out closed without a declaration
            query = query + "AND NOT @rm\\:closedWithoutDeclaration:\"true\"";

            // skal du have en status med her? - så den kan vælge mellem kun - alle ambulante eller kun indlagte

            // query ambulante

            if (statusCriteria.equals(DatabaseModel.statusCriteriaAmbulant)) {

                String[] status = new String[6];
                status[0] = "Ambulant";
                status[1] = "Ambulant/arrestant";
                status[2] = "Ambulant/surrogatanbragt";

                status[3] = "Gr-Ambulant";
                status[4] = "Gr-Ambulant/arrestant";
                status[5] = "Gr-Ambulant/surrogatanbragt";

                String statusQuery = " AND ( ";

                for (int i = 0; i <= status.length - 1; i++) {

                    String state = status[i];
                    if (statusQuery.equals(" AND ( ")) {
                        statusQuery += QueryUtils.getParameterQuery("status", state, false);
                    } else {
                        statusQuery += " OR " + QueryUtils.getParameterQuery("status", state, false);
                    }
                }
                statusQuery += ") ";

                query += statusQuery;
            }
            else if (statusCriteria.equals(DatabaseModel.statusCriteriaIndlagt)) {

                String[] status = new String[2];
                status[0] = "Indlagt til observation";

                status[1] = "Gr-Indlagt til observation";

                String statusQuery = " AND ( ";

                for (int i = 0; i <= status.length - 1; i++) {

                    String state = status[i];
                    if (statusQuery.equals(" AND ( ")) {
                        statusQuery += QueryUtils.getParameterQuery("status", state, false);
                    } else {
                        statusQuery += " OR " + QueryUtils.getParameterQuery("status", state, false);
                    }
                }
                statusQuery += ") ";

                query += statusQuery;

            }


            // query indlagte

            System.out.println("hvad blev din query?");
            System.out.println(query);



            List<NodeRef> nodeRefs = entryBean.getEntries(query,0,1000,"@rm:caseNumber", true);
            return nodeRefs;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private NodeRef setupSpreadSheet(List<NodeRef> nodeRefs) throws Exception {

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


        Cell passivventetid = table.getCellByPosition(2, 0);
        Font fncp = passivventetid.getFont();
        fncp.setFontStyle(StyleTypeDefinitions.FontStyle.BOLD);
        passivventetid.addParagraph("Passiv ventetid").setFont(fncp);


        Cell aktivventetid = table.getCellByPosition(3, 0);
        Font fnc = cpr.getFont();
        fnc.setFontStyle(StyleTypeDefinitions.FontStyle.BOLD);
        aktivventetid.addParagraph("Aktiv ventetid").setFont(fnc);



        Cell samletventetid = table.getCellByPosition(4, 0);
        Font fncps = samletventetid.getFont();
        fncps.setFontStyle(StyleTypeDefinitions.FontStyle.BOLD);
        samletventetid.addParagraph("Samlet ventetid").setFont(fncps);

        Font fc = cpr.getFont();
        fc.setFontStyle(StyleTypeDefinitions.FontStyle.BOLD);

        int nextRow = 0;

        int sumAktiv = 0;
        int sumPassiv = 0;
        int sumSamlet = 0;

        int aktivAverageCount = 0;
        int passivAverageCount = 0;
        int totalAverageCount = 0;

        for (int i =0; i <= nodeRefs.size()-1; i++) {
            NodeRef nodeRef = nodeRefs.get(i);

            int sagsNummer = (Integer) nodeService.getProperty(nodeRef, PROP_CASE_NUMBER);
            String cprNummer = (String) nodeService.getProperty(nodeRef, PROP_CPR);

            cprNummer = cprNummer.substring(0,6) + "-" + cprNummer.substring(6,10);

            entryBean.calculateActive(nodeRef);
            entryBean.calculatePassive(nodeRef);
            entryBean.calculateTotal(nodeRef);

            // check if it was possible to calculate the waiting times
            int aktivVentetidInt = 99999;
            if (nodeService.getProperty(nodeRef, PROP_WAITING_ACTIVE) != null) {
                aktivVentetidInt = (Integer) nodeService.getProperty(nodeRef, PROP_WAITING_ACTIVE);
                sumAktiv = sumAktiv + aktivVentetidInt;
                aktivAverageCount = aktivAverageCount +1;
            }

            int passivVentetidInt = 99999;
            if (nodeService.getProperty(nodeRef, PROP_WAITING_PASSIVE) != null) {
                passivVentetidInt = (Integer) nodeService.getProperty(nodeRef, PROP_WAITING_PASSIVE);
                sumPassiv = sumPassiv + passivVentetidInt;
                passivAverageCount = passivAverageCount +1;
            }

            int samletVentetidInt = 99999;
            if (nodeService.getProperty(nodeRef, PROP_WAITING_TOTAL) != null) {
                samletVentetidInt = (Integer) nodeService.getProperty(nodeRef, PROP_WAITING_TOTAL);
                sumSamlet = sumSamlet + samletVentetidInt;
                totalAverageCount = totalAverageCount +1;
            }

            nextRow = nextRow+1;

            Cell sagsnrValue = table.getCellByPosition(0, nextRow);
            sagsnrValue.setStringValue(String.valueOf(sagsNummer));

//            Cell cprValue = table.getCellByPosition(1, nextRow);
//            cprValue.setStringValue(String.valueOf(cprNummer));

            Cell passivventetidCell = table.getCellByPosition(2, nextRow);
            if (passivVentetidInt != 99999) {
                passivventetidCell.setStringValue(String.valueOf(passivVentetidInt));
            }
            else {
                passivventetidCell.setStringValue("kunne ikke beregnes");
            }

            Cell aktivventetidCell = table.getCellByPosition(3, nextRow);
            if (aktivVentetidInt != 99999) {
                aktivventetidCell.setStringValue(String.valueOf(aktivVentetidInt));
            }
            else {
                aktivventetidCell.setStringValue("kunne ikke beregnes");
            }


            Cell samletVentetidCell = table.getCellByPosition(4, nextRow);
            if (samletVentetidInt != 99999) {
                samletVentetidCell.setStringValue(String.valueOf(samletVentetidInt));
            }
            else {
                samletVentetidCell.setStringValue("kunne ikke beregnes");
            }


            Cell urlValue = table.getCellByPosition(1, nextRow);
            //            URI uri = new URI("http://0.0.0.0:7674/#!/erklaeringer/sag/" + sagsNummer + "/patientdata");
            //            URI uri = new URI("http://0.0.0.0:7674/#!/erklaeringer/sag/" + sagsNummer + "/patientdata");
            URI uri = new URI("https://oda.rm.dk/#!/erklaeringer/sag/" + sagsNummer + "/patientdata");

            urlValue.addParagraph("").appendHyperlink(cprNummer, uri);



        }

        Cell tekstGennemsnit = table.getCellByPosition(0, nextRow+2);
        tekstGennemsnit.setStringValue("Gennemsnit");


        if (passivAverageCount > 0) {
            Cell passivGennemsnit = table.getCellByPosition(2, nextRow + 2);
            passivGennemsnit.setStringValue(String.valueOf((sumPassiv / passivAverageCount)));
        }

        if (aktivAverageCount > 0) {
            Cell aktivGennemsnit = table.getCellByPosition(3, nextRow + 2);
            aktivGennemsnit.setStringValue(String.valueOf((sumAktiv / aktivAverageCount)));
        }

        if (totalAverageCount > 0) {
            Cell totalGennemsnit = table.getCellByPosition(4, nextRow + 2);
            totalGennemsnit.setStringValue(String.valueOf((sumSamlet / totalAverageCount)));
        }

        table.getColumnList().get(0).setWidth(20);
        table.getColumnList().get(1).setWidth(40);
        table.getColumnList().get(2).setWidth(40);
        table.getColumnList().get(3).setWidth(40);
        table.getColumnList().get(4).setWidth(40);


        NodeRef tmpFolder = siteService.getContainer("retspsyk", DatabaseModel.PROP_TMP);

        Map<QName, Serializable> properties = new HashMap<>();

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




