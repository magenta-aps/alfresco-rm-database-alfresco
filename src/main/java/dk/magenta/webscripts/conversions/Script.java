package dk.magenta.webscripts.conversions;

import dk.magenta.beans.EntryBean;
import dk.magenta.beans.MailBean;
import dk.magenta.beans.ReportWaitingTimeBean;
import dk.magenta.beans.ScriptBean;
import dk.magenta.model.DatabaseModel;
import dk.magenta.utils.JSONUtils;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.PermissionService;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.io.Writer;
import java.util.*;


public class Script extends AbstractWebScript {


    public void setFileFolderService(FileFolderService fileFolderService) {
        this.fileFolderService = fileFolderService;
    }

    public void setScriptBean(ScriptBean scriptBean) {
        this.scriptBean = scriptBean;
    }

    private ScriptBean scriptBean;


    private FileFolderService fileFolderService;

    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    private PermissionService permissionService;

    public void setEntryBean(EntryBean entryBean) {
        this.entryBean = entryBean;
    }

    private EntryBean entryBean;

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    private NodeService nodeService;


    public void setReportWaitingTimeBean(ReportWaitingTimeBean reportWaitingTimeBean) {
        this.reportWaitingTimeBean = reportWaitingTimeBean;
    }

    private ReportWaitingTimeBean reportWaitingTimeBean;

    private ContentService contentService;

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }



    @Override
    public void execute(WebScriptRequest webScriptRequest, WebScriptResponse webScriptResponse) throws IOException {

        webScriptResponse.setContentEncoding("UTF-8");
        Content c = webScriptRequest.getContent();
        Writer webScriptWriter = webScriptResponse.getWriter();
        JSONObject result = new JSONObject();

        JSONObject jsonProperties = null;
        String uuid = null;
        String method = null;

        try {
            JSONObject json = new JSONObject(c.getContent());
            jsonProperties = JSONUtils.getObject(json, "properties");

            uuid = jsonProperties.getString("uuid");
            method = jsonProperties.getString("method");

            if (method.equals("view")) {
                scriptBean.traverse(new NodeRef("workspace://SpacesStore/" + uuid));
                System.out.println("hvad er listen: " + scriptBean.getList().size());

                List<NodeRef> nodeRefs = scriptBean.getList();
                for (int i=0;i<=nodeRefs.size()-1;i++) {
                    NodeRef n = nodeRefs.get(i);
                    System.out.println("name" + nodeService.getProperty(n, ContentModel.PROP_NAME) + " " + "nodeRef" + n);
                }
            }
            else if (method.equals("convert")) {
                scriptBean.traverse(new NodeRef("workspace://SpacesStore/" + uuid));
                System.out.println("size of list: " + scriptBean.getList().size());

                List<NodeRef> nodeRefs = scriptBean.getList();

                for (int i=0;i<=nodeRefs.size()-1;i++) {
                    NodeRef n = nodeRefs.get(i);
                    permissionService.setPermission(n, DatabaseModel.GROUP_ALLOWEDTODELETE, PermissionService.DELETE_NODE, true);
                    System.out.println("name" + nodeService.getProperty(n, ContentModel.PROP_NAME) + " " + "nodeRef" + n);
                }
            }
            else if (method.equals("testmail")) {
                try {
                    reportWaitingTimeBean.sendMail();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

           result.put("result", "done");
        }

        catch (JSONException e) {
            e.printStackTrace();
        }

        webScriptResponse.setStatus(400);
        JSONUtils.write(webScriptWriter, result);
    }
}
