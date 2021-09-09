package dk.magenta.beans;

import dk.magenta.model.DatabaseModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.transform.ContentTransformer;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.odftoolkit.simple.TextDocument;
import org.odftoolkit.simple.table.Row;
import org.odftoolkit.simple.table.Table;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class PrintBean {

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    private SiteService siteService;
    private NodeService nodeService;
    private ContentService contentService;


    public NodeRef printEntriesToPDF() throws Exception {
//    public NodeRef printEntriesToPDF(org.json.JSONArray entries) {
        System.out.println("printEntriesTOPDF");

        TextDocument document = TextDocument.newTextDocument();
        Table table = document.addTable();
        Row row = table.appendRow();
        row.getCellByIndex(0).addParagraph("test");
        row.getCellByIndex(1).addParagraph("test");
        row.getCellByIndex(3).addParagraph("test");

        row = table.appendRow();
        row.getCellByIndex(0).addParagraph("test2");
        row.getCellByIndex(1).addParagraph("test2");
        row.getCellByIndex(3).addParagraph("test2");

        row = table.appendRow();
        row.getCellByIndex(0).addParagraph("test3");
        row.getCellByIndex(1).addParagraph("test3");
        row.getCellByIndex(3).addParagraph("test3");

        File f = new File("tmp.odt");
        document.save(f);


        return null;
    }

    private NodeRef transform(NodeRef source) {

        String source_name = (String)nodeService.getProperty(source, ContentModel.PROP_NAME);

        NodeRef tmpFolder = siteService.getContainer("retspsyk", DatabaseModel.PROP_TMP);

        // Create new PDF
        Map<QName, Serializable> documentLibaryProps = new HashMap<>();
        documentLibaryProps.put(ContentModel.PROP_NAME, source_name + ".pdf");


        ChildAssociationRef pdf = nodeService.createNode(tmpFolder, ContentModel.ASSOC_CONTAINS,
                QName.createQName(ContentModel.USER_MODEL_URI, "thePDF"),
                ContentModel.TYPE_CONTENT, documentLibaryProps);


        ContentData contentData = (ContentData) nodeService.getProperty(source, ContentModel.PROP_CONTENT);
        String originalMimeType = contentData.getMimetype();

        ContentReader pptReader = contentService.getReader(source, ContentModel.PROP_CONTENT);
        ContentWriter pdfWriter = contentService.getWriter(pdf.getChildRef(), ContentModel.PROP_CONTENT, true);

        pdfWriter.setMimetype(MimetypeMap.MIMETYPE_PDF);

        ContentTransformer pptToPdfTransformer = contentService.getTransformer(originalMimeType, MimetypeMap.MIMETYPE_PDF);

        pptToPdfTransformer.transform(pptReader, pdfWriter);

        return pdf.getChildRef();
    }

}
