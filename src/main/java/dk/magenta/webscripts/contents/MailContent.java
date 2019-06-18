package dk.magenta.webscripts.contents;

import dk.magenta.beans.ContentsBean;
import dk.magenta.beans.EntryBean;
import dk.magenta.beans.MailBean;
import dk.magenta.model.DatabaseModel;
import dk.magenta.utils.JSONUtils;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import javax.swing.text.html.parser.ContentModel;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class MailContent extends AbstractWebScript {

    private MailBean mailBean;

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

        System.out.println("status på mailBean" + mailBean);


        webScriptResponse.setContentEncoding("UTF-8");
        Content c = webScriptRequest.getContent();
        Writer webScriptWriter = webScriptResponse.getWriter();
        JSONObject result;

        Map<String, String> params = JSONUtils.parseParameters(webScriptRequest.getURL());


        JSONObject json = null;
        try {

            System.out.println("the json" + c.getContent());
            json = new JSONObject(c.getContent());

            JSONArray jsonNodeRefs = JSONUtils.getArray(json, "nodeRefs");
            NodeRef[] nodeRefs = new NodeRef[jsonNodeRefs.length()];
            for (int i=0; i<jsonNodeRefs.length(); i++) {
                String nodeRefStr = jsonNodeRefs.getString(i);
                NodeRef nodeRef = new NodeRef(nodeRefStr);
                nodeRefs[i] = nodeRef;
            }

            String subject = (String)json.get("subject");
            System.out.println("the subject: " + subject);

            String body = (String)json.get("body");
            System.out.println("the body: " + body);


            String authority = (String)json.get("authority");
            System.out.println("authority: " + authority);

            String caseid = (String)json.get("caseid");
            System.out.println("caseid: " + caseid);

//            mailBean.sendEmail(nodeRefs, authority, body, subject);

            // add to list of recorded emails

            // pak dette væk i en bean senere


            String query = "@rm\\:caseNumber:\"" + caseid + "\"";
            System.out.println("hvad er query:" + query);

            NodeRef declaration = entryBean.getEntry(query);

            List<String> criteria = Arrays.asList(DatabaseModel.PROP_LOGFORMAILS);
            List<ChildAssociationRef> documents = nodeService.getChildrenByName(declaration, org.alfresco.model.ContentModel.ASSOC_CONTAINS, criteria);


            String currentUser = authenticationService.getCurrentUserName();
            NodeRef personNode = personService.getPerson(currentUser);
            PersonService.PersonInfo info = personService.getPerson(personNode);

            Calendar cal = Calendar.getInstance();

            int year = cal.get(Calendar.YEAR);
            int day = cal.get(Calendar.DATE);
            int month = (cal.get(Calendar.MONTH)+1);





            String line = "";
            line +=  "Nedenstående filer er afsendt af " + info.getFirstName() + " " + info.getLastName();
            line += " til " + authority;
            line += " den " + day + "/" + month + " " + year + "\n";
            line += "------------------------------------------";
            line += "\n";


            for (int i=0;i <= nodeRefs.length-1; i++) {
                NodeRef n = nodeRefs[i];
                line += nodeService.getProperty(n, org.alfresco.model.ContentModel.PROP_NAME) + "\n";
            }



            if (documents.size() == 0) {
                FileInfo newNode = fileFolderService.create(declaration, DatabaseModel.PROP_LOGFORMAILS, org.alfresco.model.ContentModel.TYPE_CONTENT);
                ContentWriter contentWriter = contentService.getWriter(newNode.getNodeRef(), org.alfresco.model.ContentModel.PROP_CONTENT, true);

                contentWriter.setMimetype("text/plain");

                System.out.println("den nye node");
                System.out.println(newNode.getNodeRef());

                contentWriter.putContent(line);


                nodeService.addAspect(newNode.getNodeRef(), org.alfresco.model.ContentModel.ASPECT_UNDELETABLE, null);

            }
            else {

                NodeRef template_doc = documents.get(0).getChildRef();
                ContentReader contentReader = contentService.getReader(template_doc, org.alfresco.model.ContentModel.PROP_CONTENT);
                String content = contentReader.getContentString();

                line += "\n\n\n";
                line += content;

                ContentWriter contentWriter = contentService.getWriter(template_doc, org.alfresco.model.ContentModel.PROP_CONTENT, true);
                contentWriter.putContent(line);

                System.out.println("dette er kvitteringsnoden: ");
                System.out.println(template_doc);

            }





            result = JSONUtils.getSuccess();
            JSONUtils.write(webScriptWriter, result);


        } catch (JSONException e) {
            e.printStackTrace();
            result = JSONUtils.getError(e);
            webScriptResponse.setStatus(400);
            JSONUtils.write(webScriptWriter, result);
        }
    }
}
