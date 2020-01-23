package dk.magenta.webscripts.conversions;

import dk.magenta.beans.EntryBean;
import dk.magenta.beans.MailBean;
import dk.magenta.model.DatabaseModel;
import dk.magenta.utils.JSONUtils;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.version.VersionService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.odftoolkit.simple.TextDocument;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.*;


public class Script extends AbstractWebScript {


    public void setFileFolderService(FileFolderService fileFolderService) {
        this.fileFolderService = fileFolderService;
    }

    private FileFolderService fileFolderService;

    public void setEntryBean(EntryBean entryBean) {
        this.entryBean = entryBean;
    }

    private EntryBean entryBean;

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    private NodeService nodeService;
    private ContentService contentService;

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }



    @Override
    public void execute(WebScriptRequest webScriptRequest, WebScriptResponse webScriptResponse) throws IOException {

        webScriptResponse.setContentEncoding("UTF-8");
        Content c = webScriptRequest.getContent();
        Writer webScriptWriter = webScriptResponse.getWriter();
        JSONObject result;


        try {

            // get all declarations with a receipt with the old name

            // old value for PROP_LOGFORMAILS
            String PROP_LOGFORMAILS = "mail_kvitteringer.txt";

            String query = "@cm\\:name:\"" + PROP_LOGFORMAILS + "\"";

            List<NodeRef> declarations = entryBean.getEntriesbyQuery(query);
            Iterator i = declarations.iterator();

            System.out.println("total found to convert:" + declarations.size());


            // check if folder exists - or else, create one

            while (i.hasNext()) {

                NodeRef actual_document = (NodeRef) i.next();
                NodeRef dec = nodeService.getPrimaryParent(actual_document).getParentRef();

                List<String> criteria = Arrays.asList(DatabaseModel.PROP_DEFAULTFOLDER_MAILRECEIPTS);
                List<ChildAssociationRef> mailFolder = nodeService.getChildrenByName(dec, org.alfresco.model.ContentModel.ASSOC_CONTAINS, criteria);

                FileInfo mailFolderInfo = null;
                NodeRef mail_folder = null;

                if (mailFolder.size() == 0) {
                    mailFolderInfo = fileFolderService.create(dec, DatabaseModel.PROP_DEFAULTFOLDER_MAILRECEIPTS, ContentModel.TYPE_FOLDER);
                    mail_folder = mailFolderInfo.getNodeRef();
                }
                else {
                    mail_folder = mailFolder.get(0).getChildRef();
                }


                // get the old contents

                criteria = new ArrayList<>();
                criteria = Arrays.asList(PROP_LOGFORMAILS);
                List<ChildAssociationRef> documents = nodeService.getChildrenByName(dec, org.alfresco.model.ContentModel.ASSOC_CONTAINS, criteria);

                NodeRef template_doc = documents.get(0).getChildRef();
                ContentReader contentReader = contentService.getReader(template_doc, org.alfresco.model.ContentModel.PROP_CONTENT);
                String content = contentReader.getContentString();


                // add the old contents to the odt file
                FileInfo newNode = fileFolderService.create(mail_folder, DatabaseModel.PROP_LOGFORMAILS, org.alfresco.model.ContentModel.TYPE_CONTENT);


                nodeService.addAspect(newNode.getNodeRef(), org.alfresco.model.ContentModel.ASPECT_VERSIONABLE, null);

                TextDocument log_entires = TextDocument.newTextDocument();

                log_entires.addParagraph(content);
                File f = new File("tmp");

                log_entires.save(f);

                ContentWriter contentWriter = contentService.getWriter(newNode.getNodeRef(), org.alfresco.model.ContentModel.PROP_CONTENT, true);

                contentWriter.setMimetype("application/vnd.oasis.opendocument.text");

                contentWriter.putContent(f);

                // delete the old receipts - add the hidden aspect for now and change the name.

                fileFolderService.rename(template_doc, "converted_receipts.txt");
                nodeService.addAspect(template_doc, ContentModel.ASPECT_HIDDEN, null);

            }

        } catch (Exception e) {
            e.printStackTrace();
            result = JSONUtils.getError(e);
            webScriptResponse.setStatus(400);
            JSONUtils.write(webScriptWriter, result);
        }
    }
}
