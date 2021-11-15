package dk.magenta.beans;


import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.io.IOUtils;
import org.odftoolkit.simple.TextDocument;

import java.io.*;
import java.util.*;

public class TransformBean {

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    private ContentService contentService;

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    private NodeService nodeService;
    private SearchService searchService;

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    private Properties properties;

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    private TransactionService transactionService;

    private String getLOOLurl() {
        // todo get the url from alfresco-global.properties


        System.out.println("hvad er lool host");
        System.out.println(properties.getProperty("lool_host"));
        System.out.println(properties.getProperty("lool_host"));
        System.out.println(properties.getProperty("lool_host"));
        System.out.println(properties.getProperty("lool_host"));
        System.out.println(properties.getProperty("lool_host"));

        return properties.getProperty("lool_host");
    }

    public NodeRef transformODTtoPDF(NodeRef source, NodeRef targetParent) throws Exception {

        ContentReader source_content = contentService.getReader(source, ContentModel.PROP_CONTENT);

        TextDocument document = TextDocument.loadDocument(source_content.getContentInputStream());
        File from = new File ("from");
        document.save(from);

        File to = new File("to");

        Map<QName, Serializable> documentLibaryProps = new HashMap<>();
        ChildAssociationRef pdf = nodeService.createNode(targetParent, ContentModel.ASSOC_CONTAINS,
                                                         QName.createQName(ContentModel.USER_MODEL_URI, "thePDF"),
                                                         ContentModel.TYPE_CONTENT, documentLibaryProps);


        remoteConvert(this.getLOOLurl(), from, MimetypeMap.MIMETYPE_OPENDOCUMENT_TEXT, to, MimetypeMap.MIMETYPE_PDF);

        ContentWriter pdfWriter = contentService.getWriter(pdf.getChildRef(), ContentModel.PROP_CONTENT, true);
        pdfWriter.setMimetype(MimetypeMap.MIMETYPE_PDF);
        pdfWriter.putContent(to);

        return pdf.getChildRef();
    }


    /**
     * Handle remote OpenOffice server conversion
     */
    private void remoteConvert(String uri, File inputFile, String srcMimeType, File outputFile, String dstMimeType) throws ConversionException {

        PostMethod post = new PostMethod(uri);

        try {
            // todo test om de to parametre er nødvendige
            Part[] parts = {new FilePart(inputFile.getName(), inputFile), new StringPart("src_mime", srcMimeType),
                    new StringPart("dst_mime", dstMimeType)};
            post.setRequestEntity(new MultipartRequestEntity(parts, post.getParams()));
            HttpClient httpclient = new HttpClient();
            int rc = httpclient.executeMethod(post);

            System.out.println("hvad er rc");
            System.out.println(rc);
            System.out.println(rc);


            if (rc == HttpStatus.SC_OK) {
                FileOutputStream fos = new FileOutputStream(outputFile);
                BufferedInputStream bis = new BufferedInputStream(post.getResponseBodyAsStream());
                IOUtils.copy(bis, fos);
                bis.close();
                fos.close();
            } else {
                throw new IOException("Error in conversion: " + rc);
            }
        } catch (HttpException e) {
            throw new ConversionException("HTTP exception", e);
        } catch (FileNotFoundException e) {
            throw new ConversionException("File not found exeption", e);
        } catch (IOException e) {
            throw new ConversionException("IO exception", e);
        } finally {
            post.releaseConnection();
        }
    }

    public boolean cleanUpTMP() {
        return transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

            try {

                ResultSet resultSet = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "lucene", " PATH:\"/app:company_home/st:sites/cm:retspsyk/cm:tmp//*\" AND ASPECT:\"rm:tmp\" ");
                List<NodeRef> nodeRefs = resultSet.getNodeRefs();

                Iterator i = nodeRefs.iterator();

                while (i.hasNext()) {
                    NodeRef node = (NodeRef) i.next();
                    nodeService.deleteNode(node);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        });
    }



}
