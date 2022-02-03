package dk.magenta.webscripts.contents;

import dk.magenta.beans.ContentsBean;
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
import org.json.JSONException;
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


public class MailContent extends AbstractWebScript {

    private MailBean mailBean;

    public void setLockService(LockService lockService) {
        this.lockService = lockService;
    }

    private LockService lockService;

    public void setVersionService(VersionService versionService) {
        this.versionService = versionService;
    }

    private VersionService versionService;

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    private PersonService personService;

    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    private AuthenticationService authenticationService;

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

    public void setMimetypeService(MimetypeService mimetypeService) {
        this.mimetypeService = mimetypeService;
    }

    private MimetypeService mimetypeService;

    public void setMailBean(MailBean mailBean) {
        this.mailBean = mailBean;
    }




    @Override
    public void execute(WebScriptRequest webScriptRequest, WebScriptResponse webScriptResponse) throws IOException {


        webScriptResponse.setContentEncoding("UTF-8");
        Content c = webScriptRequest.getContent();
        Writer webScriptWriter = webScriptResponse.getWriter();
        JSONObject result = new JSONObject();

        Map<String, String> params = JSONUtils.parseParameters(webScriptRequest.getURL());


        JSONObject json = null;

        boolean temporaryUnlocked = false;
        NodeRef declaration = null;

        String method = "";
        try {
            json = new JSONObject(c.getContent());
            method = (String) json.get("method");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        switch (method) {
            case "signitureAvailability":
                try {

                    JSONArray jsonNodeRefs = JSONUtils.getArray(json, "nodeRefs");
                    NodeRef[] nodeRefs = new NodeRef[jsonNodeRefs.length()];
                    for (int i = 0; i < jsonNodeRefs.length(); i++) {
                        JSONObject nodeRefStr = (JSONObject) jsonNodeRefs.get(i);
                        NodeRef nodeRef = new NodeRef(nodeRefStr.getString("nodeRef"));
                        nodeRefs[i] = nodeRef;
                    }

                    String caseid = (String) json.get("caseid");
                    String query = "@rm\\:caseNumber:\"" + caseid + "\"";
                    declaration = entryBean.getEntry(query);

                    boolean avail = mailBean.signituresAvailable(declaration, nodeRefs);

                    result.put("available", avail);
                    JSONUtils.write(webScriptWriter, result);
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case "preview":

                try {

                    String caseid = (String) json.get("caseid");

                    JSONArray jsonNodeRefs = JSONUtils.getArray(json, "nodeRefs");
                    NodeRef[] nodeRefs = new NodeRef[jsonNodeRefs.length()];
                    for (int i = 0; i < jsonNodeRefs.length(); i++) {
                        String nodeRefStr = jsonNodeRefs.getString(i);
                        NodeRef nodeRef = new NodeRef(nodeRefStr);
                        nodeRefs[i] = nodeRef;
                    }

                    //TODO setup option for signitures

                    String query = "@rm\\:caseNumber:\"" + caseid + "\"";
                    declaration = entryBean.getEntry(query);

                    NodeRef preview = mailBean.getPreviewOfPdfWithSignature(nodeRefs, declaration);


                    result.put("previewNode", preview.getId());

                    JSONUtils.write(webScriptWriter, result);


                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }


                break;

            case "send":

                try {

                    json = new JSONObject(c.getContent());
                    Boolean useSignature = (Boolean) json.get("useSignature");

                    JSONArray jsonNodeRefs = JSONUtils.getArray(json, "nodeRefs");
                    NodeRef[] nodeRefs = new NodeRef[jsonNodeRefs.length()];
                    for (int i = 0; i < jsonNodeRefs.length(); i++) {
                        String nodeRefStr = jsonNodeRefs.getString(i);
                        NodeRef nodeRef = new NodeRef(nodeRefStr);
                        nodeRefs[i] = nodeRef;
                    }

                    String subject = (String) json.get("subject");


                    String body = (String) json.get("body");


                    String authority = (String) json.get("authority");


                    String caseid = (String) json.get("caseid");

                    //TODO setup option for signitures

                    String query = "@rm\\:caseNumber:\"" + caseid + "\"";
                    declaration = entryBean.getEntry(query);



                    mailBean.sendEmail(nodeRefs, authority, body, subject, useSignature, declaration);


                    // pak dette væk i en bean senere


                    // unlock if the case is locked and lock after email has been sent

                    if (lockService.isLocked(declaration)) {
                        lockService.unlock(declaration);
                        temporaryUnlocked = true;
                    }


                    String currentUser = authenticationService.getCurrentUserName();
                    NodeRef personNode = personService.getPerson(currentUser);
                    PersonService.PersonInfo info = personService.getPerson(personNode);

                    Calendar cal = Calendar.getInstance();

                    int year = cal.get(Calendar.YEAR);
                    int day = cal.get(Calendar.DATE);
                    int month = (cal.get(Calendar.MONTH) + 1);


                    String line = "";
                    line += "Nedenstående filer er afsendt af " + info.getFirstName() + " " + info.getLastName();
                    line += " til " + authority;
                    line += " den " + day + "/" + month + " " + year + "\n";
                    line += "------------------------------------------";
                    line += "\n";


                    for (int i = 0; i <= nodeRefs.length - 1; i++) {
                        NodeRef n = nodeRefs[i];
                        line += nodeService.getProperty(n, org.alfresco.model.ContentModel.PROP_NAME) + "\n";
                    }


                    List<String> criteria = Arrays.asList(DatabaseModel.PROP_DEFAULTFOLDER_MAILRECEIPTS);
                    List<ChildAssociationRef> mailFolder = nodeService.getChildrenByName(declaration, org.alfresco.model.ContentModel.ASSOC_CONTAINS, criteria);


                    if (mailFolder.size() == 0) {

                        FileInfo mailFolderInfo = fileFolderService.create(declaration, DatabaseModel.PROP_DEFAULTFOLDER_MAILRECEIPTS, ContentModel.TYPE_FOLDER);

                        NodeRef mail_folder = mailFolderInfo.getNodeRef();

                        FileInfo newNode = fileFolderService.create(mail_folder, DatabaseModel.PROP_LOGFORMAILS, org.alfresco.model.ContentModel.TYPE_CONTENT);

                        nodeService.addAspect(newNode.getNodeRef(), org.alfresco.model.ContentModel.ASPECT_VERSIONABLE, null);

                        TextDocument log_entires = TextDocument.newTextDocument();
                        log_entires.addParagraph(line);
                        File f = new File("tmp");

                        log_entires.save(f);

                        ContentWriter contentWriter = contentService.getWriter(newNode.getNodeRef(), org.alfresco.model.ContentModel.PROP_CONTENT, true);

                        contentWriter.setMimetype("application/vnd.oasis.opendocument.text");

                        contentWriter.putContent(f);

                        nodeService.addAspect(newNode.getNodeRef(), org.alfresco.model.ContentModel.ASPECT_UNDELETABLE, null);

                    } else {

                        criteria = Arrays.asList(DatabaseModel.PROP_LOGFORMAILS);
                        List<ChildAssociationRef> log = nodeService.getChildrenByName(mailFolder.get(0).getChildRef(), org.alfresco.model.ContentModel.ASSOC_CONTAINS, criteria);

                        NodeRef log_node;

                        if (log.size() == 0) {
                            NodeRef mail_folder = mailFolder.get(0).getChildRef();
                            FileInfo newNode = fileFolderService.create(mail_folder, DatabaseModel.PROP_LOGFORMAILS, org.alfresco.model.ContentModel.TYPE_CONTENT);

                            nodeService.addAspect(newNode.getNodeRef(), org.alfresco.model.ContentModel.ASPECT_VERSIONABLE, null);

                            TextDocument log_entires = TextDocument.newTextDocument();
                            File f = new File("tmp");
                            log_entires.save(f);

                            ContentWriter contentWriter = contentService.getWriter(newNode.getNodeRef(), org.alfresco.model.ContentModel.PROP_CONTENT, true);

                            contentWriter.setMimetype("application/vnd.oasis.opendocument.text");

                            contentWriter.putContent(f);

                            nodeService.addAspect(newNode.getNodeRef(), org.alfresco.model.ContentModel.ASPECT_UNDELETABLE, null);

                            log_node = newNode.getNodeRef();
                        }
                        else {
                            log_node = log.get(0).getChildRef();
                        }

                        ContentReader contentReader = contentService.getReader(log_node, org.alfresco.model.ContentModel.PROP_CONTENT);

                        TextDocument log_entires = TextDocument.loadDocument(contentReader.getContentInputStream());

                        log_entires.addParagraph(line);

                        File f = new File("tmp");

                        log_entires.save(f);

                        Map<String, Serializable> properties = new HashMap<>();
                        properties.put("modifier", currentUser);

                        versionService.createVersion(log_node, properties);

                        ContentWriter contentWriter = contentService.getWriter(log_node, org.alfresco.model.ContentModel.PROP_CONTENT, true);

                        contentWriter.putContent(f);

                    }


                    result = JSONUtils.getSuccess();
                    JSONUtils.write(webScriptWriter, result);

                    if (temporaryUnlocked) {
                        lockService.lock(declaration, LockType.READ_ONLY_LOCK);
                    }


                } catch (Exception e) {

                    if (temporaryUnlocked) {
                        lockService.lock(declaration, LockType.READ_ONLY_LOCK);
                    }

                    e.printStackTrace();
                    result = JSONUtils.getError(e);
                    webScriptResponse.setStatus(400);
                    JSONUtils.write(webScriptWriter, result);
                }
                break;
        }

    }

    private NodeRef makeFolderForMailReceipts(NodeRef dec) {

        List<String> criteria = Arrays.asList(DatabaseModel.PROP_DEFAULTFOLDER_MAILRECEIPTS);
        List<ChildAssociationRef> documents = nodeService.getChildrenByName(dec, org.alfresco.model.ContentModel.ASSOC_CONTAINS, criteria);

        if (documents.size() == 0) {
            FileInfo newNode = fileFolderService.create(dec, DatabaseModel.PROP_LOGFORMAILS, ContentModel.TYPE_FOLDER);
            return newNode.getNodeRef();
        }
        else {
            return documents.get(0).getChildRef();
        }
    }

}
