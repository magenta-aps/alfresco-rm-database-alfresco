package dk.magenta.beans;

import dk.magenta.model.DatabaseModel;
import dk.magenta.utils.QueryUtils;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.odftoolkit.simple.SpreadsheetDocument;
import org.odftoolkit.simple.style.Font;
import org.odftoolkit.simple.style.StyleTypeDefinitions;
import org.odftoolkit.simple.table.Cell;
import org.odftoolkit.simple.table.Table;
import org.odftoolkit.simple.table.TableContainer;

import javax.faces.model.DataModel;
import java.awt.*;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.List;

import static dk.magenta.model.DatabaseModel.*;

public class WeeklyStatBean {

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

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    private SiteService siteService;

    private DatabaseBean databaseBean;

    public void setMailBean(MailBean mailBean) {
        this.mailBean = mailBean;
    }

    private MailBean mailBean;

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


    private NodeRef getWeeklyStatFolder() {

        NodeRef weeklyStatFolder = siteService.getContainer("retspsyk", PROP_WEEKLYSTAT);

        if (weeklyStatFolder != null) {
            return weeklyStatFolder;
        }
        else {
            return siteService.createContainer("retspsyk", PROP_WEEKLYSTAT, ContentModel.TYPE_FOLDER, null);
        }
    }

    private NodeRef getYearFolderForWeeklyStat(String year) {
        NodeRef weeklyRootFolder = this.getWeeklyStatFolder();
        NodeRef yearFolder = this.getOrCreateChildByName(weeklyRootFolder, year);
        return yearFolder;
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

    public void initYear(String year) {
        for (int i=1; i <=52; i++) {
            this.calculate(String.valueOf(i), year);
        }
    }

    public void calculate(String week, String year) {

        // int year = Calendar.getInstance().get(Calendar.YEAR);
        NodeRef yearFolder = this.getYearFolderForWeeklyStat(year);

        int weekInYear = Integer.valueOf(week);
        WeekFields weekFields = WeekFields.of(Locale.FRANCE);

        LocalDate dateStart = LocalDate.ofYearDay(Integer.valueOf(year), 1)
                .with(weekFields.weekOfYear(), weekInYear)
                .with(weekFields.dayOfWeek(), 1);

        LocalDate dateEnd = dateStart.plusDays(6);
        System.out.println(dateStart);
        System.out.println(dateEnd);

        // afsendte
        int receivedCount = this.query("creationDate", dateStart, dateEnd, false);
        int sentCount = this.query("declarationDate", dateStart, dateEnd, false);
        this.createNodeRefForWeekYear(week, year, String.valueOf(sentCount), String.valueOf(receivedCount));

    }

    private NodeRef createNodeRefForWeekYear(String week, String year, String sent, String received) {

        NodeRef childRef = nodeService.getChildByName(this.getYearFolderForWeeklyStat(year), ContentModel.ASSOC_CONTAINS, week);

//        System.out.println("hvad er childRef");
//        System.out.println(childRef);

        // recalculation - delete the old node
        if(childRef != null) {
//            System.out.println("deleting node");
            nodeService.deleteNode(childRef);
        }

        Map<QName, Serializable> properties = new HashMap<>();
        properties.put(ContentModel.PROP_NAME, week);
        properties.put(DatabaseModel.PROP_WEEK, week);
        properties.put(DatabaseModel.PROP_YEAR, year);
        properties.put(DatabaseModel.PROP_SENT, sent);
        properties.put(DatabaseModel.PROP_RECEIVED, received);
        QName qName = QName.createQName(DatabaseModel.CONTENT_MODEL_URI, week);
        ChildAssociationRef childAssociationRef = nodeService.createNode(this.getYearFolderForWeeklyStat(year), ContentModel.ASSOC_CONTAINS, qName, PROP_WEEKLY_TYPE, properties);
        childRef = childAssociationRef.getChildRef();

        return childRef;

    }

    private int query(String field, LocalDate startDate, LocalDate endDate, boolean bua) {

        String query = "";
        JSONArray queryArray = new JSONArray();

        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);

        try {

            JSONObject o = new JSONObject();

            String from_formattedDate = outputFormatter.format(startDate);
            String to_formattedDate = outputFormatter.format(endDate);

            o.put("key", field);
            o.put("value", QueryUtils.dateRangeQuery(from_formattedDate , to_formattedDate));
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


    public NodeRef getChartB(String year) {
        String yearM1 = String.valueOf(Integer.valueOf(year)-1);
        String yearM2 = String.valueOf(Integer.valueOf(year)-2);

        List<WeekNode> yearWeekNodes = this.getWeekNodesForYear(year);
        List<WeekNode> yearWeekNodesM1 = this.getWeekNodesForYear(yearM1);
        List<WeekNode> yearWeekNodesM2 = this.getWeekNodesForYear(yearM2);

        if ( (yearWeekNodes.size() > 0) && (yearWeekNodesM1.size() > 0) && (yearWeekNodesM2.size() > 0)) {

            XYSeriesCollection dataset = new XYSeriesCollection();
            dataset = this.getDataSetForYearChart(year, yearWeekNodes, yearWeekNodesM1, yearWeekNodesM2);


            JFreeChart chart = ChartFactory.createXYLineChart(
                    "----",
                    "Uge",
                    "Antal",
                    dataset,
                    PlotOrientation.VERTICAL,
                    true,
                    true,
                    false
            );

            XYPlot plot = chart.getXYPlot();

            XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
            renderer.setSeriesPaint(0, java.awt.Color.RED);
            renderer.setSeriesStroke(0, new BasicStroke(2.0f));

            renderer.setSeriesPaint(1, java.awt.Color.BLUE);
            renderer.setSeriesStroke(1, new BasicStroke(2.0f));

            renderer.setSeriesPaint(2, java.awt.Color.GREEN);
            renderer.setSeriesStroke(2, new BasicStroke(2.0f));

            plot.setRenderer(renderer);
            plot.setBackgroundPaint((Paint) java.awt.Color.WHITE);

            plot.setRangeGridlinesVisible(true);
            plot.setRangeGridlinePaint((Paint) java.awt.Color.BLACK);

            plot.setDomainGridlinesVisible(true);
            plot.setDomainGridlinePaint((Paint) java.awt.Color.BLACK);

            chart.getLegend().setFrame(BlockBorder.NONE);

            chart.setTitle(new TextTitle("Rapport pr år for " + year,
                            new java.awt.Font("Serif", java.awt.Font.BOLD, 18)
                    )
            );

            OutputStream out = null;
            File f = new File("aar.png");
            try {
                out = new FileOutputStream(f);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                ChartUtilities.writeChartAsPNG(out, chart,800,1000);

                NodeRef tmpFolder = siteService.getContainer("retspsyk", DatabaseModel.PROP_TMP);
                QName qName = QName.createQName(DatabaseModel.CONTENT_MODEL_URI, "test");
                Map<QName, Serializable> properties = new HashMap<>();
                properties.put(ContentModel.PROP_NAME, "tmpchart.png");
                ChildAssociationRef childAssociationRef = nodeService.createNode(tmpFolder, ContentModel.ASSOC_CONTAINS, qName, ContentModel.TYPE_CONTENT, properties);
                nodeService.setProperty(childAssociationRef.getChildRef(), ContentModel.PROP_NAME, childAssociationRef.getChildRef().getId());

                ContentWriter writer = contentService.getWriter(childAssociationRef.getChildRef(), ContentModel.PROP_CONTENT, true);

                writer.setMimetype("image/png");
                writer.putContent(f);

                return childAssociationRef.getChildRef();

            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        else {
            return null;
        }
    }

    public NodeRef createChartB(String year) {

        String yearM1 = String.valueOf(Integer.valueOf(year)-1);
        String yearM2 = String.valueOf(Integer.valueOf(year)-2);

        List<WeekNode> yearWeekNodes = this.getWeekNodesForYear(year);
        List<WeekNode> yearWeekNodesM1 = this.getWeekNodesForYear(yearM1);
        List<WeekNode> yearWeekNodesM2 = this.getWeekNodesForYear(yearM2);

        if ( (yearWeekNodes.size() > 0) && (yearWeekNodesM1.size() > 0) && (yearWeekNodesM2.size() > 0)) {
            return this.writeToDocumentChartB(year, yearWeekNodes, yearWeekNodesM1, yearWeekNodesM2);
        }
        else {
            return null;
        }


    }

    public NodeRef createChartA(String year) {
        List<WeekNode> yearWeekNodes = this.getWeekNodesForYear(year);

//        System.out.println("hvad er yearWeekNodes");
//        System.out.println(yearWeekNodes);

        if (yearWeekNodes.size() > 0) {
            return this.writeToDocument(year, yearWeekNodes);
        }
        else {
            return null;
        }
    }

    public XYSeries getWeekNodesForYearChartReceived(String year) {

        XYSeries series = new XYSeries("Modtaget");

        NodeRef yearFolder = this.getYearFolderForWeeklyStat(year);

        List<ChildAssociationRef> weekNodeRef = nodeService.getChildAssocs(yearFolder);

        Iterator i = weekNodeRef.iterator();

        while (i.hasNext()) {
            NodeRef week = ((ChildAssociationRef) i.next()).getChildRef();

            String weekString = (String)nodeService.getProperty(week, PROP_WEEK);
//            String sent = (String)nodeService.getProperty(week, PROP_SENT);
            String received = (String)nodeService.getProperty(week, PROP_RECEIVED);
            System.out.println("sent");
            System.out.println(received);
            System.out.println("Double.valueOf(weekString), Double.valueOf(sent)");
            System.out.println(Double.valueOf(weekString));
            System.out.println(Double.valueOf(received));

            series.add(Double.valueOf(weekString), Double.valueOf(received));
        }
        return series;
    }

    public XYSeries getWeekNodesForYearChartReceivedAkk(String year) {

        XYSeries series = new XYSeries("Modtaget Akk");

        NodeRef yearFolder = this.getYearFolderForWeeklyStat(year);

        List<ChildAssociationRef> weekNodeRef = nodeService.getChildAssocs(yearFolder);

        Iterator i = weekNodeRef.iterator();

        int receivedAkk = 0;
        while (i.hasNext()) {
            NodeRef week = ((ChildAssociationRef) i.next()).getChildRef();

            String weekString = (String)nodeService.getProperty(week, PROP_WEEK);
//            String sent = (String)nodeService.getProperty(week, PROP_SENT);
            String received = (String)nodeService.getProperty(week, PROP_RECEIVED);

            receivedAkk = receivedAkk + Integer.valueOf(received);
            series.add(Double.valueOf(weekString), Double.valueOf(receivedAkk));
        }
        return series;
    }

    public XYSeries getWeekNodesForYearChartSent(String year) {

        XYSeries series = new XYSeries("Sendt");

        NodeRef yearFolder = this.getYearFolderForWeeklyStat(year);

        List<ChildAssociationRef> weekNodeRef = nodeService.getChildAssocs(yearFolder);

        Iterator i = weekNodeRef.iterator();

        while (i.hasNext()) {
            NodeRef week = ((ChildAssociationRef) i.next()).getChildRef();

            String weekString = (String)nodeService.getProperty(week, PROP_WEEK);
            String sent = (String)nodeService.getProperty(week, PROP_SENT);
            series.add(Double.valueOf(weekString), Double.valueOf(sent));
        }
        return series;
    }

    public XYSeries getWeekNodesForYearChartSentAkk(String year) {

        XYSeries series = new XYSeries("Sendt Akk.");

        NodeRef yearFolder = this.getYearFolderForWeeklyStat(year);

        List<ChildAssociationRef> weekNodeRef = nodeService.getChildAssocs(yearFolder);

        Iterator i = weekNodeRef.iterator();

        int sentAkk = 0;
        while (i.hasNext()) {
            NodeRef week = ((ChildAssociationRef) i.next()).getChildRef();

            String weekString = (String)nodeService.getProperty(week, PROP_WEEK);
            String sent = (String)nodeService.getProperty(week, PROP_SENT);

            sentAkk = sentAkk + Integer.valueOf(sent);
            series.add(Double.valueOf(weekString), Double.valueOf(sentAkk));
        }
        return series;
    }

    public List<WeekNode> getWeekNodesForYear(String year) {

        NodeRef yearFolder = this.getYearFolderForWeeklyStat(year);

        List<WeekNode> weeks = new ArrayList<>();

        List<ChildAssociationRef> weekNodeRef = nodeService.getChildAssocs(yearFolder);

        Iterator i = weekNodeRef.iterator();

        while (i.hasNext()) {
            NodeRef week = ((ChildAssociationRef) i.next()).getChildRef();

            WeekNode weekNode = new WeekNode();

            weekNode.year = (String)nodeService.getProperty(week, PROP_YEAR);
            String weekString = (String)nodeService.getProperty(week, PROP_WEEK);
            weekNode.week = Integer.valueOf(weekString);
            weekNode.sent = (String)nodeService.getProperty(week, PROP_SENT);
            weekNode.received = (String)nodeService.getProperty(week, PROP_RECEIVED);

            weeks.add(weekNode);
        }


        // Sorting
        Collections.sort(weeks, new Comparator<WeekNode>()
        {
            @Override
            public int compare(WeekNode o1, WeekNode o2)
            {
                return o1.week.compareTo(o2.week);
            }
        });

        return weeks;
    }

    public boolean deleteTmpChartFile(NodeRef nodeRef) {
        try {
            nodeService.deleteNode(nodeRef);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    private NodeRef getSpreadSheetNodeRefChartA(String year) {

        return transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

            final NodeRef sharedNodeRef;

            StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
            ResultSet rs = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE, "PATH:\"/app:company_home/app:shared\"");
            NodeRef childRef;

            try
            {
                if (rs.length() == 0) {
                    throw new AlfrescoRuntimeException("Didn't find shared noderef");
                }
                 sharedNodeRef = rs.getNodeRef(0);

                 childRef = nodeService.getChildByName(sharedNodeRef, ContentModel.ASSOC_CONTAINS, DatabaseModel.WEEKLY_REPORT_SPREADSHEET_A_NAME);

                if(childRef != null) {
                    nodeService.deleteNode(childRef);
                }


                    Map<QName, Serializable> properties = new HashMap<>();
                    properties.put(ContentModel.PROP_NAME, DatabaseModel.WEEKLY_REPORT_SPREADSHEET_A_NAME);
                    QName qName = QName.createQName(DatabaseModel.CONTENT_MODEL_URI, DatabaseModel.WEEKLY_REPORT_SPREADSHEET_A_NAME);
                    ChildAssociationRef childAssociationRef = nodeService.createNode(sharedNodeRef, ContentModel.ASSOC_CONTAINS, qName, ContentModel.TYPE_CONTENT, properties);


                    childRef = childAssociationRef.getChildRef();

                    ContentData cd = (ContentData) nodeService.getProperty(childRef, ContentModel.PROP_CONTENT);
                    ContentData newCd = ContentData.setMimetype(cd, "application/vnd.oasis.opendocument.spreadsheet");
                    nodeService.setProperty(childRef, ContentModel.PROP_CONTENT, newCd);

                    // also make the conent of the node

                    SpreadsheetDocument document = SpreadsheetDocument.newSpreadsheetDocument();


                    table = document.getSheetByIndex(0);

                    Cell newcases_date = table.getCellByPosition(INIT_NEXT_NEWCASES_X, INIT_NEXT_NEWCASES_Y);
                    newcases_date.setStringValue("Uge" + "for år " + year);
                    Font fn = newcases_date.getFont();
                    fn.setFontStyle(StyleTypeDefinitions.FontStyle.BOLD);


                    Cell newcases_title = table.getCellByPosition(INIT_NEXT_NEWCASES_X+1, INIT_NEXT_NEWCASES_Y);
                    newcases_title.setStringValue("Sendt");
                    Font fnt = newcases_title.getFont();
                    fnt.setFontStyle(StyleTypeDefinitions.FontStyle.BOLD);

                    Cell closedcases_date = table.getCellByPosition(INIT_NEXT_NEWCASES_X+2, INIT_NEXT_NEWCASES_Y);
                    closedcases_date.setStringValue("Modtaget");
                    Font fc = newcases_date.getFont();
                    fc.setFontStyle(StyleTypeDefinitions.FontStyle.BOLD);


                    Cell closedcases_title = table.getCellByPosition(INIT_NEXT_NEWCASES_X+3, INIT_NEXT_NEWCASES_Y);
                    closedcases_title.setStringValue("Sendt akk.");
                    Font fnc = newcases_title.getFont();
                    fnc.setFontStyle(StyleTypeDefinitions.FontStyle.BOLD);

                    Cell ReceivedAkk_title = table.getCellByPosition(INIT_NEXT_NEWCASES_X+4, INIT_NEXT_NEWCASES_Y);
                    ReceivedAkk_title.setStringValue("Modtaget akk.");
                     fnc = ReceivedAkk_title.getFont();
                    fnc.setFontStyle(StyleTypeDefinitions.FontStyle.BOLD);

                    ContentWriter writer = contentService.getWriter(childRef, ContentModel.PROP_CONTENT, true);

                    File f = new File("tmp");

                    document.save(f);
                    writer.putContent(f);


                    Map<QName, Serializable> aspectProps = new HashMap<>();
                    aspectProps.put(DatabaseModel.PROP_NEXT_NEWCASES_X, INIT_NEXT_NEWCASES_X);
                    aspectProps.put(DatabaseModel.PROP_NEXT_NEWCASES_Y, INIT_NEXT_NEWCASES_Y);

                    aspectProps.put(DatabaseModel.PROP_NEXT_CLOSEDCASES_X, INIT_NEXT_CLOSEDCASES_X);
                    aspectProps.put(DatabaseModel.PROP_NEXT_CLOSEDCASES_Y, INIT_NEXT_CLOSEDCASES_Y);

                    nodeService.addAspect(childRef, DatabaseModel.ASPECT_STAT, aspectProps);

            }

            finally {
                rs.close();
            }
            return childRef;
        });
    }


    private NodeRef getSpreadSheetNodeRefChartB(String year) {

        return transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

            final NodeRef sharedNodeRef;

            StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
            ResultSet rs = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE, "PATH:\"/app:company_home/app:shared\"");
            NodeRef childRef;

            try
            {
                if (rs.length() == 0) {
                    throw new AlfrescoRuntimeException("Didn't find shared noderef");
                }
                sharedNodeRef = rs.getNodeRef(0);

                childRef = nodeService.getChildByName(sharedNodeRef, ContentModel.ASSOC_CONTAINS, DatabaseModel.WEEKLY_REPORT_SPREADSHEET_B_NAME);

                if(childRef != null) {
                    nodeService.deleteNode(childRef);
                }


                Map<QName, Serializable> properties = new HashMap<>();
                properties.put(ContentModel.PROP_NAME, DatabaseModel.WEEKLY_REPORT_SPREADSHEET_B_NAME);
                QName qName = QName.createQName(DatabaseModel.CONTENT_MODEL_URI, DatabaseModel.WEEKLY_REPORT_SPREADSHEET_B_NAME);
                ChildAssociationRef childAssociationRef = nodeService.createNode(sharedNodeRef, ContentModel.ASSOC_CONTAINS, qName, ContentModel.TYPE_CONTENT, properties);


                childRef = childAssociationRef.getChildRef();

                ContentData cd = (ContentData) nodeService.getProperty(childRef, ContentModel.PROP_CONTENT);
                ContentData newCd = ContentData.setMimetype(cd, "application/vnd.oasis.opendocument.spreadsheet");
                nodeService.setProperty(childRef, ContentModel.PROP_CONTENT, newCd);

                // also make the conent of the node

                SpreadsheetDocument document = SpreadsheetDocument.newSpreadsheetDocument();


                table = document.getSheetByIndex(0);

                Cell newcases_date = table.getCellByPosition(INIT_NEXT_NEWCASES_X, INIT_NEXT_NEWCASES_Y);
                newcases_date.setStringValue("Uge");
                Font fn = newcases_date.getFont();
                fn.setFontStyle(StyleTypeDefinitions.FontStyle.BOLD);


                Cell newcases_title = table.getCellByPosition(INIT_NEXT_NEWCASES_X+1, INIT_NEXT_NEWCASES_Y);
                newcases_title.setStringValue(year);
                Font fnt = newcases_title.getFont();
                fnt.setFontStyle(StyleTypeDefinitions.FontStyle.BOLD);

                Cell closedcases_date = table.getCellByPosition(INIT_NEXT_NEWCASES_X+2, INIT_NEXT_NEWCASES_Y);

                int y = Integer.valueOf(year) -1;
                closedcases_date.setStringValue(String.valueOf(y));
                Font fc = newcases_date.getFont();
                fc.setFontStyle(StyleTypeDefinitions.FontStyle.BOLD);


                Cell closedcases_title = table.getCellByPosition(INIT_NEXT_NEWCASES_X+3, INIT_NEXT_NEWCASES_Y);

                y = Integer.valueOf(year) -2;
                closedcases_title.setStringValue(String.valueOf(y));
                Font fnc = newcases_title.getFont();
                fnc.setFontStyle(StyleTypeDefinitions.FontStyle.BOLD);

                ContentWriter writer = contentService.getWriter(childRef, ContentModel.PROP_CONTENT, true);

                File f = new File("tmp");

                document.save(f);
                writer.putContent(f);


                Map<QName, Serializable> aspectProps = new HashMap<>();
                aspectProps.put(DatabaseModel.PROP_NEXT_NEWCASES_X, INIT_NEXT_NEWCASES_X);
                aspectProps.put(DatabaseModel.PROP_NEXT_NEWCASES_Y, INIT_NEXT_NEWCASES_Y);

                aspectProps.put(DatabaseModel.PROP_NEXT_CLOSEDCASES_X, INIT_NEXT_CLOSEDCASES_X);
                aspectProps.put(DatabaseModel.PROP_NEXT_CLOSEDCASES_Y, INIT_NEXT_CLOSEDCASES_Y);

                nodeService.addAspect(childRef, DatabaseModel.ASPECT_STAT, aspectProps);

            }

            finally {
                rs.close();
            }
            return childRef;
        });
    }

    private int last4weeksTotal(WeekNode weekNode, List<WeekNode> weeks, String property) {

            int total = 0;

            int possibleStepsBack = 0;

            if (weekNode.week >= 4) {
                possibleStepsBack = 3;
            }
            else if (weekNode.week == 3) {
                possibleStepsBack = 2;
            }
            else if (weekNode.week == 2) {
                possibleStepsBack = 1;
            }
            else if (weekNode.week == 1) {
                possibleStepsBack = 0;
            }


            for (int i=0; i<=possibleStepsBack; i++) {
                WeekNode w = weeks.get((weekNode.week-1) - i);  // (weekNode.week-1) as the array is indexed from 0

                if (property.equals("received")) {
                    total = total + Integer.valueOf(w.received);
                }
                else {
                    total = total + Integer.valueOf(w.sent);
                }
            }
            return total;
    }


    public XYSeriesCollection getDataSetForYearChart (String year, List<WeekNode> weeksC, List<WeekNode> weeksM1, List<WeekNode> weeksM2) {

        XYSeries a = new XYSeries(year);
        XYSeries b = new XYSeries(Integer.valueOf(year)-1);
        XYSeries c = new XYSeries(Integer.valueOf(year)-2);

        for (int i=0; i<=weeksC.size()-1; i++) {

            WeekNode weekNode = weeksC.get(i);
            WeekNode weekNodeM1 = weeksM1.get(i);
            WeekNode weekNodeM2 = weeksM2.get(i);

            a.add(Double.valueOf(weekNode.week), Double.valueOf(weekNode.sent));
            b.add(Double.valueOf(weekNode.week), Double.valueOf(weekNodeM1.sent));
            c.add(Double.valueOf(weekNode.week), Double.valueOf(weekNodeM2.sent));
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(a);
        dataset.addSeries(b);
        dataset.addSeries(c);

        return dataset;
    }

    public NodeRef writeToDocumentChartB (String year, List<WeekNode> weeksC, List<WeekNode> weeksM1, List<WeekNode> weeksM2) {


        NodeRef spreadSheetNodeRef = this.getSpreadSheetNodeRefChartB(year);

        return transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

            try {
                ContentReader contentReader = contentService.getReader(spreadSheetNodeRef, ContentModel.PROP_CONTENT);
                SpreadsheetDocument spreadsheetDocument = SpreadsheetDocument.loadDocument(contentReader.getContentInputStream());

                NewCasesXY newCasesXY = new NewCasesXY();
                newCasesXY.next(spreadSheetNodeRef);

                ClosedCasesXY closedCasesXY = new ClosedCasesXY();
                closedCasesXY.next(spreadSheetNodeRef);

                table = spreadsheetDocument.getSheetByIndex(0);

                for (int i=0; i<=weeksC.size()-1; i++) {

                    WeekNode weekNode = weeksC.get(i);
                    WeekNode weekNodeM1 = weeksM1.get(i);
                    WeekNode weekNodeM2 = weeksM2.get(i);

                    // week
                    Cell week = table.getCellByPosition(newCasesXY.getX(), newCasesXY.getY());
                    week.setDoubleValue(Double.valueOf(weekNode.week));

                    // current
                    Cell current = table.getCellByPosition(newCasesXY.getX()+1, newCasesXY.getY());
                    current.setDoubleValue(Double.valueOf(weekNode.sent));

                    // current -1
                    Cell weekM1 = table.getCellByPosition(newCasesXY.getX()+2, newCasesXY.getY());
                    weekM1.setDoubleValue(Double.valueOf(weekNodeM1.sent));

                    // current -2
                    Cell weekM2 = table.getCellByPosition(newCasesXY.getX()+3, newCasesXY.getY());
                    weekM2.setDoubleValue(Double.valueOf(weekNodeM2.sent));

                    newCasesXY.y = newCasesXY.y+1;
                }

                // write timestamp and user

                String timestamp = "Rapport pr. " + new Date() + " af " + AuthenticationUtil.getFullyAuthenticatedUser();
                Cell e1 = table.getCellByPosition(newCasesXY.getX(), newCasesXY.getY()+5);
                e1.setStringValue(timestamp);

                ContentWriter writer = contentService.getWriter(spreadSheetNodeRef, ContentModel.PROP_CONTENT, true);

                File f = new File("tmp");

                spreadsheetDocument.save(f);
                writer.putContent(f);

            } catch (Exception e) {
//                System.out.println(e);
                e.printStackTrace();
            }

            return spreadSheetNodeRef;
        });
    }

    public NodeRef writeToDocument(String year, List<WeekNode> weeks) {

//        System.out.println("writing " + weeks.size() + " of weeks" + "staring with [0] " + weeks.get(0).week) ;

        NodeRef spreadSheetNodeRef = this.getSpreadSheetNodeRefChartA(year);

        return transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

            try {

                ContentReader contentReader = contentService.getReader(spreadSheetNodeRef, ContentModel.PROP_CONTENT);
                SpreadsheetDocument spreadsheetDocument = SpreadsheetDocument.loadDocument(contentReader.getContentInputStream());

                NewCasesXY newCasesXY = new NewCasesXY();
                newCasesXY.next(spreadSheetNodeRef);

                ClosedCasesXY closedCasesXY = new ClosedCasesXY();
                closedCasesXY.next(spreadSheetNodeRef);

                table = spreadsheetDocument.getSheetByIndex(0);

                Iterator i = weeks.iterator();

                while (i.hasNext()) {
                    WeekNode weekNode = (WeekNode) i.next();
                    Cell week = table.getCellByPosition(newCasesXY.getX(), newCasesXY.getY());
                    week.setDoubleValue(Double.valueOf(weekNode.week));

                    Cell sent = table.getCellByPosition(newCasesXY.getX()+1, newCasesXY.getY());
                    sent.setDoubleValue(Double.valueOf(weekNode.sent));

                    Cell received = table.getCellByPosition(newCasesXY.getX()+2, newCasesXY.getY());
                    received.setDoubleValue(Double.valueOf(weekNode.received));

                    Cell sent_akk_cell = table.getCellByPosition(newCasesXY.getX()+3, newCasesXY.getY());
                    int sent_akk = this.last4weeksTotal(weekNode, weeks, "sent");
                    sent_akk_cell.setDoubleValue(Double.valueOf(sent_akk));

                    Cell received_akk_cell = table.getCellByPosition(newCasesXY.getX()+4, newCasesXY.getY());
                    int received_akk = this.last4weeksTotal(weekNode, weeks, "received");
                    received_akk_cell.setDoubleValue(Double.valueOf(received_akk));

                    newCasesXY.y = newCasesXY.y+1;
                }

                // write timestamp and user

                String timestamp = "Rapport pr. " + new Date() + " af " + AuthenticationUtil.getFullyAuthenticatedUser();
                Cell e1 = table.getCellByPosition(newCasesXY.getX(), newCasesXY.getY()+5);
                e1.setStringValue(timestamp);

                ContentWriter writer = contentService.getWriter(spreadSheetNodeRef, ContentModel.PROP_CONTENT, true);

                File f = new File("tmp");



                spreadsheetDocument.save(f);
                writer.putContent(f);

            } catch (Exception e) {
                System.out.println(e);
                e.printStackTrace();
            }

            return spreadSheetNodeRef;
        });
    }

        public class NewCasesXY {
            private int x;
            private int y;

            public void next(NodeRef n) {
                x = (int)nodeService.getProperty(n, DatabaseModel.PROP_NEXT_NEWCASES_X);
                y = (int)nodeService.getProperty(n, DatabaseModel.PROP_NEXT_NEWCASES_Y) +1;

                nodeService.setProperty(n, DatabaseModel.PROP_NEXT_NEWCASES_Y, y);
            }

            public int getX() {
                return x;
            }

            public int getY() {
                return y;
            }
        }

    public class ClosedCasesXY {
        private int x;
        private int y;

        public void next(NodeRef n) {
            x = (int)nodeService.getProperty(n, DatabaseModel.PROP_NEXT_CLOSEDCASES_X);
            y = (int)nodeService.getProperty(n, DatabaseModel.PROP_NEXT_CLOSEDCASES_Y) +1;

            nodeService.setProperty(n, DatabaseModel.PROP_NEXT_CLOSEDCASES_Y, y);
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }
    }
    class WeekNode  {
        Integer week;
        String year;
        String sent;
        String received;
    }

    public boolean sendMailCurrentYear() throws Exception {
        return transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
            LocalDateTime now = LocalDateTime.now();

            NodeRef report = this.mailBean.doChart(String.valueOf(now.getYear()));

            NodeRef[] attachmentList = new NodeRef[1];
            attachmentList[0] = report;

//            mailBean.sendEmailNoTransform(attachmentList,"ps.o.faelles.post@rm.dk", "rapport for sendte og modtagne erklæringer i indeværende år", "");
//            mailBean.sendEmailNoTransform(attachmentList,"fhp@magenta.dk", "", "TEST1 rapport for sendte og modtagne erklæringer i indeværende år");
            mailBean.sendEmailNoTransform(attachmentList,"ps.o.faelles.post@rm.dk", "", "Rapport for sendte og modtagne erklæringer i indeværende år");
            return true;
        });
    }

}
