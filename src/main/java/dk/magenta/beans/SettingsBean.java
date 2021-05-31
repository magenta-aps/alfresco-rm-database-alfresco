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
import org.json.JSONObject;
import org.odftoolkit.odfdom.pkg.MediaType;
import org.odftoolkit.odfdom.pkg.OdfElement;
import org.odftoolkit.simple.Document;
import org.odftoolkit.simple.SpreadsheetDocument;
import org.odftoolkit.simple.TextDocument;
import org.odftoolkit.simple.common.EditableTextExtractor;
import org.odftoolkit.simple.common.TextExtractor;
import org.odftoolkit.simple.style.Font;
import org.odftoolkit.simple.style.StyleTypeDefinitions;
import org.odftoolkit.simple.table.Cell;
import org.odftoolkit.simple.table.Table;
import org.odftoolkit.simple.table.TableContainer;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static dk.magenta.model.DatabaseModel.*;
import static org.apache.poi.hslf.record.RecordTypes.Document;

public class SettingsBean {


    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    private NodeService nodeService;



    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    private ContentService contentService;

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    private SearchService searchService;



    // # 361807
    public String getDefaultMailText(NodeRef decl, String dropdown) throws Exception {

        // get node - if not exists, create

        NodeRef sharedNodeRef;

        StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
        ResultSet rs = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE, "PATH:\"/app:company_home/app:shared\"");
        NodeRef childRef;

        if (rs.length() == 0) {
            throw new AlfrescoRuntimeException("Didn't find shared noderef");
        }
        sharedNodeRef = rs.getNodeRef(0);

        String nodeName = "";


        if (dropdown.equals(DEFAULT_MAIL_TEXT_SEND_VALUE)) {
            System.out.println("default");
            nodeName = DatabaseModel.DEFAULT_MAIL_TEXT_NAME;
            childRef = nodeService.getChildByName(sharedNodeRef, ContentModel.ASSOC_CONTAINS, DatabaseModel.DEFAULT_MAIL_TEXT_NAME);
        }
        else {
            System.out.println("returnering");
            childRef = nodeService.getChildByName(sharedNodeRef, ContentModel.ASSOC_CONTAINS, DEFAULT_MAIL_TEXT_RETURN);
            nodeName = DEFAULT_MAIL_TEXT_RETURN;
        }

        System.out.println("childRef" + childRef);


        // create node
        if(childRef == null) {

            // create the node
            Map<QName, Serializable> properties = new HashMap<>();
            properties.put(ContentModel.PROP_NAME, nodeName);
            QName qName = QName.createQName(DatabaseModel.CONTENT_MODEL_URI, nodeName);
            ChildAssociationRef childAssociationRef = nodeService.createNode(sharedNodeRef, ContentModel.ASSOC_CONTAINS, qName, ContentModel.TYPE_CONTENT, properties);


            childRef = childAssociationRef.getChildRef();

            ContentData cd = (ContentData) nodeService.getProperty(childRef, ContentModel.PROP_CONTENT);
            ContentData newCd = ContentData.setMimetype(cd, "application/vnd.oasis.opendocument.text");
            nodeService.setProperty(childRef, ContentModel.PROP_CONTENT, newCd);

            // create the empty contents of the node

            TextDocument document = TextDocument.newTextDocument();

            File f = new File("tmp");

            document.save(f);

            ContentWriter contentWriter = contentService.getWriter(childRef, org.alfresco.model.ContentModel.PROP_CONTENT, true);

            contentWriter.putContent(f);
        }
        else {
            // return text

            ContentReader contentReader = contentService.getReader(childRef, org.alfresco.model.ContentModel.PROP_CONTENT);
            TextDocument contents = TextDocument.loadDocument(contentReader.getContentInputStream());

            OdfElement elem=contents.getContentRoot();
            EditableTextExtractor extractorE = EditableTextExtractor.newOdfEditableTextExtractor(elem);

            String value = extractorE.getText();

            // substitut journalnumber, name and cpr

            String navn = (String)nodeService.getProperty(decl, DatabaseModel.PROP_FIRST_NAME) + " " + (String)nodeService.getProperty(decl, DatabaseModel.PROP_LAST_NAME);

            String journalnummer = "xxx";
            if (nodeService.getProperty(decl, DatabaseModel.PROP_JOURNALNUMMER) != null) {
                journalnummer = (String)nodeService.getProperty(decl, DatabaseModel.PROP_JOURNALNUMMER);
            }

            String cpr = (String)nodeService.getProperty(decl, DatabaseModel.PROP_CPR);
            cpr = cpr.substring(0,6) + "-" + cpr.substring(6,10);

            value = value.replace("##journalnummer", journalnummer);
            value = value.replace("##navn", navn);
            value = value.replace("##cpr", cpr);

            return value;
       }


        return "";
    }

    // # 361807
    public void setDefaultMailText(String txt) {

    }















}

