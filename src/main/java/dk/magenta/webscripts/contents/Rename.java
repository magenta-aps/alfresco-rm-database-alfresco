

package dk.magenta.webscripts.contents;

import bsh.EvalError;

import freemarker.template.TemplateException;

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



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import dk.magenta.beans.ContentsBean;
import dk.magenta.model.DatabaseModel;
import dk.magenta.utils.JSONUtils;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeUtils;
import org.jodconverter.local.JodConverter;
import org.jodconverter.local.office.LocalOfficeManager;
import org.json.JSONException;
import org.json.JSONObject;
import org.odftoolkit.simple.TextDocument;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.alfresco.service.namespace.QName;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Rename extends AbstractWebScript {

    public void setContentsBean(ContentsBean contentsBean) {
        this.contentsBean = contentsBean;
    }

    ContentsBean contentsBean;

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    private NodeService nodeService;

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    private SiteService siteService;

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    private ContentService contentService;


    @Override
    public void execute(WebScriptRequest webScriptRequest, WebScriptResponse webScriptResponse) throws IOException {

        webScriptResponse.setContentEncoding("UTF-8");
        Content c = webScriptRequest.getContent();
        Writer webScriptWriter = webScriptResponse.getWriter();
        JSONObject result;

        JSONObject json = null;

        try {
            json = new JSONObject(c.getContent());

            String nodeRef = (String)json.get("nodeRef");
            String name = (String)json.get("name");


            NodeRef source = new NodeRef(nodeRef);


            ContentReader reader = contentService.getReader(source, ContentModel.PROP_CONTENT);



        // Create new PDF
        String source_name = (String)nodeService.getProperty(source, ContentModel.PROP_NAME);
        NodeRef tmpFolder = siteService.getContainer("retspsyk", DatabaseModel.PROP_TMP);

        Map<QName, Serializable> documentLibaryProps = new HashMap<>();
//        documentLibaryProps.put(ContentModel.PROP_NAME, source_name + ".pdf");

        ChildAssociationRef pdf = nodeService.createNode(tmpFolder, ContentModel.ASSOC_CONTAINS,
                QName.createQName(ContentModel.USER_MODEL_URI, "thePDF"),
                ContentModel.TYPE_CONTENT, documentLibaryProps);


        ContentData contentData = (ContentData) nodeService.getProperty(source, ContentModel.PROP_CONTENT);
        ContentWriter pdfWriter = contentService.getWriter(pdf.getChildRef(), ContentModel.PROP_CONTENT, true);

        pdfWriter.setMimetype(MimetypeMap.MIMETYPE_PDF);


        // Create an office manager using the default configuration.
        // The default port is 2002. Note that when an office manager
        // is installed, it will be the one used by default when
        // a converter is created.

        TextDocument document = TextDocument.loadDocument(reader.getContentInputStream());
        File from = new File ("from");
        document.save(from);

        File f = new File("test");
        File to = new File("test");
//
//        final LocalOfficeManager officeManager = LocalOfficeManager.builder()
//                .install()
//                .officeHome("/opt/libreoffice6.4")
//                .portNumbers(9090)
//                .build();
//        try {
//
//            // Start an office process and connect to the started instance (on port 2002).
//            officeManager.start();
//
//            // Convert
//            JodConverter
//                    .convert(from)
//                    .to(f).as(DefaultDocumentFormatRegistry.PDF)
//                    .execute();
//
//
//
//
//
//            pdfWriter.putContent(f);
//
//        } catch (
//                OfficeException e) {
//            e.printStackTrace();
//        } finally {
//            // Stop the office process
//            OfficeUtils.stopQuietly(officeManager);
//        }


//            remoteConvert("https://oda-lool-test.rm.dk:9980/", from,MimetypeMap.MIMETYPE_OPENDOCUMENT_TEXT, to, MimetypeMap.MIMETYPE_PDF);
            remoteConvert("https://oda-lool-test.rm.dk:9980/lool/convert-to/pdf", from,MimetypeMap.MIMETYPE_OPENDOCUMENT_TEXT, to, MimetypeMap.MIMETYPE_PDF);
            pdfWriter.putContent(to);

            contentsBean.rename(new NodeRef(nodeRef), name);

            result = JSONUtils.getSuccess();
            JSONUtils.write(webScriptWriter, result);

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Handle remote OpenOffice server conversion
     */
    public void remoteConvert(String uri, File inputFile, String srcMimeType, File outputFile, String dstMimeType)
            throws ConversionException {
        PostMethod post = new PostMethod(uri);

        try {
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



}

