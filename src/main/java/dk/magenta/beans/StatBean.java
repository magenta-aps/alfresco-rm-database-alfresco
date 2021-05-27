package dk.magenta.beans;

import dk.magenta.model.DatabaseModel;
import dk.magenta.utils.QueryUtils;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.odftoolkit.odfdom.pkg.OdfFileDom;
import org.odftoolkit.odfdom.type.Color;
import org.odftoolkit.simple.SpreadsheetDocument;
import org.odftoolkit.simple.TextDocument;
import org.odftoolkit.simple.style.Font;
import org.odftoolkit.simple.style.StyleTypeDefinitions;
import org.odftoolkit.simple.table.Cell;
import org.odftoolkit.simple.table.Table;
import org.odftoolkit.simple.table.TableContainer;
import org.alfresco.service.transaction.TransactionService;

import static dk.magenta.model.DatabaseModel.*;

public class StatBean {

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

    public double query(String field, boolean override, int override_months) {

        String query = "";
        JSONArray queryArray = new JSONArray();

        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);

        try {

            LocalDateTime now;
            LocalDateTime onemonthback;

            if (override) {
                now = LocalDateTime.now().minusMonths(override_months * -1);
                onemonthback = now.minusMonths(1);
            }
            else {
                now = LocalDateTime.now();
                onemonthback = now.minusMonths(1);
            }


            JSONObject o = new JSONObject();

            String from_formattedDate = outputFormatter.format(now);
            String to_formattedDate = outputFormatter.format(onemonthback);

            o.put("key", field);
            o.put("value", QueryUtils.dateRangeQuery(to_formattedDate, from_formattedDate));
            o.put("include", true);
            queryArray.put(o);

            query = QueryUtils.getKeyValueQuery(DatabaseModel.TYPE_PSYC_SITENAME, DatabaseModel.TYPE_PSYC_DEC, queryArray);

            query = query + " AND -ASPECT:\"rm:bua\"";

            List<NodeRef> nodeRefs = entryBean.getEntriesbyQuery(query);



            return Double.valueOf(nodeRefs.size());


        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }


    private NodeRef getSpreadSheetNodeRef() {

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

                 childRef = nodeService.getChildByName(sharedNodeRef, ContentModel.ASSOC_CONTAINS, DatabaseModel.MONTHLY_REPORT_SPREADSHEET_NAME);

                if(childRef == null) {
                    Map<QName, Serializable> properties = new HashMap<>();
                    properties.put(ContentModel.PROP_NAME, DatabaseModel.MONTHLY_REPORT_SPREADSHEET_NAME);
                    QName qName = QName.createQName(DatabaseModel.CONTENT_MODEL_URI, DatabaseModel.MONTHLY_REPORT_SPREADSHEET_NAME);
                    ChildAssociationRef childAssociationRef = nodeService.createNode(sharedNodeRef, ContentModel.ASSOC_CONTAINS, qName, ContentModel.TYPE_CONTENT, properties);


                    childRef = childAssociationRef.getChildRef();

                    ContentData cd = (ContentData) nodeService.getProperty(childRef, ContentModel.PROP_CONTENT);
                    ContentData newCd = ContentData.setMimetype(cd, "application/vnd.oasis.opendocument.spreadsheet");
                    nodeService.setProperty(childRef, ContentModel.PROP_CONTENT, newCd);

                    // also make the conent of the node

                    SpreadsheetDocument document = SpreadsheetDocument.newSpreadsheetDocument();


                    table = document.getSheetByIndex(0);

                    Cell newcases_date = table.getCellByPosition(INIT_NEXT_NEWCASES_X, INIT_NEXT_NEWCASES_Y);
                    newcases_date.setStringValue("Tidspunkt");
                    Font fn = newcases_date.getFont();
                    fn.setFontStyle(StyleTypeDefinitions.FontStyle.BOLD);


                    Cell newcases_title = table.getCellByPosition(INIT_NEXT_NEWCASES_X+1, INIT_NEXT_NEWCASES_Y);
                    newcases_title.setStringValue("Nye sager");
                    Font fnt = newcases_title.getFont();
                    fnt.setFontStyle(StyleTypeDefinitions.FontStyle.BOLD);

                    Cell closedcases_date = table.getCellByPosition(INIT_NEXT_CLOSEDCASES_X, INIT_NEXT_CLOSEDCASES_Y);
                    closedcases_date.setStringValue("Tidspunkt");
                    Font fc = newcases_date.getFont();
                    fc.setFontStyle(StyleTypeDefinitions.FontStyle.BOLD);


                    Cell closedcases_title = table.getCellByPosition(INIT_NEXT_CLOSEDCASES_X+1, INIT_NEXT_CLOSEDCASES_Y);
                    closedcases_title.setStringValue("Afsluttede sager");
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
            }
            finally {
                rs.close();
            }
            return childRef;
        });
    }





    public boolean writeToDocument() {

        return transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

            try {

                NodeRef spreadSheetNodeRef = this.getSpreadSheetNodeRef();

                ContentReader contentReader = contentService.getReader(spreadSheetNodeRef, ContentModel.PROP_CONTENT);
                SpreadsheetDocument spreadsheetDocument = SpreadsheetDocument.loadDocument(contentReader.getContentInputStream());

                NewCasesXY newCasesXY = new NewCasesXY();
                newCasesXY.next(spreadSheetNodeRef);

                ClosedCasesXY closedCasesXY = new ClosedCasesXY();
                closedCasesXY.next(spreadSheetNodeRef);

                table = spreadsheetDocument.getSheetByIndex(0);

                Cell e = table.getCellByPosition(newCasesXY.getX(), newCasesXY.getY());

                if (override) {

                    Date referenceDate = new Date();
                    Calendar c = Calendar.getInstance();
                    c.setTime(referenceDate);
                    c.add(Calendar.MONTH, override_months);

                    e.setDateValue(c);
                }
                else {
                    e.setDateValue(Calendar.getInstance());
                }

                e.setFormatString("MM-yyyy");

                Cell e2 = table.getCellByPosition(newCasesXY.getX()+1, newCasesXY.getY());
                e2.setDoubleValue(this.query("creationDate", override, override_months));

                Cell e3 = table.getCellByPosition(closedCasesXY.getX(), closedCasesXY.getY());

                if (override) {
                    Date referenceDate = new Date();
                    Calendar c = Calendar.getInstance();
                    c.setTime(referenceDate);
                    c.add(Calendar.MONTH, override_months);

                    e3.setDateValue(c);
                }
                else {
                    e3.setDateValue(Calendar.getInstance());
                }


                e3.setFormatString("MM-yyyy");

                Cell e4 = table.getCellByPosition(closedCasesXY.getX()+1, closedCasesXY.getY());
                e4.setDoubleValue(this.query("closedDate", override, override_months));



                ContentWriter writer = contentService.getWriter(spreadSheetNodeRef, ContentModel.PROP_CONTENT, true);

                File f = new File("tmp");


                spreadsheetDocument.save(f);
                writer.putContent(f);


            } catch (Exception e) {
                System.out.println(e);
                e.printStackTrace();
            }

            return true;
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
}
