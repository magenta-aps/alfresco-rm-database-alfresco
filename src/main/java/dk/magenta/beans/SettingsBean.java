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
import org.odftoolkit.simple.SpreadsheetDocument;
import org.odftoolkit.simple.TextDocument;
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
    public String getDefaultMailText() throws Exception {

        // get node - if not exists, create

        NodeRef sharedNodeRef;

        StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
        ResultSet rs = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE, "PATH:\"/app:company_home/app:shared\"");
        NodeRef childRef;

        if (rs.length() == 0) {
            throw new AlfrescoRuntimeException("Didn't find shared noderef");
        }
        sharedNodeRef = rs.getNodeRef(0);

        childRef = nodeService.getChildByName(sharedNodeRef, ContentModel.ASSOC_CONTAINS, DatabaseModel.DEFAULT_MAIL_TEXT_NAME);

        // create node
        if(childRef == null) {


            // create the node
            Map<QName, Serializable> properties = new HashMap<>();
            properties.put(ContentModel.PROP_NAME, DatabaseModel.DEFAULT_MAIL_TEXT_NAME);
            QName qName = QName.createQName(DatabaseModel.CONTENT_MODEL_URI, DatabaseModel.DEFAULT_MAIL_TEXT_NAME);
            ChildAssociationRef childAssociationRef = nodeService.createNode(sharedNodeRef, ContentModel.ASSOC_CONTAINS, qName, ContentModel.TYPE_CONTENT, properties);


            childRef = childAssociationRef.getChildRef();

            ContentData cd = (ContentData) nodeService.getProperty(childRef, ContentModel.PROP_CONTENT);
            ContentData newCd = ContentData.setMimetype(cd, "application/vnd.oasis.opendocument.text");
            nodeService.setProperty(childRef, ContentModel.PROP_CONTENT, newCd);

            // create the empty contents of the node

            TextDocument document = TextDocument.newTextDocument();

//            document.addParagraph(line);

            File f = new File("tmp");

            document.save(f);

            ContentWriter contentWriter = contentService.getWriter(childRef, org.alfresco.model.ContentModel.PROP_CONTENT, true);

            contentWriter.putContent(f);
        }
        else {
            // return text

            ContentReader contentReader = contentService.getReader(childRef, org.alfresco.model.ContentModel.PROP_CONTENT);
            TextDocument contents = TextDocument.loadDocument(contentReader.getContentInputStream());

            return contents.getContentRoot().getTextContent();
       }


        return "";
    }

    // # 361807
    public void setDefaultMailText(String txt) {

    }















}