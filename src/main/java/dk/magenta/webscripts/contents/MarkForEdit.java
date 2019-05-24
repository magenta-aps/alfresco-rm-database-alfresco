package dk.magenta.webscripts.contents;

import dk.magenta.beans.MailBean;
import dk.magenta.model.DatabaseModel;
import dk.magenta.utils.JSONUtils;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

public class MarkForEdit extends AbstractWebScript {

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    private NodeService nodeService;

    @Override
    public void execute(WebScriptRequest webScriptRequest, WebScriptResponse webScriptResponse) throws IOException {


        webScriptResponse.setContentEncoding("UTF-8");
        Content c = webScriptRequest.getContent();
        Writer webScriptWriter = webScriptResponse.getWriter();
        JSONObject result;

        JSONObject json = null;
        try {

            System.out.println("the json" + c.getContent());
            json = new JSONObject(c.getContent());

            String method = (String)json.get("method");
            String nodeRef = (String)json.get("nodeRef");

            if (method.equals("add")) {
                nodeService.addAspect(new NodeRef(nodeRef), DatabaseModel.ASPECT_DECLARATIONMARKEDFOREDIT, null);
                result = JSONUtils.getSuccess();
                JSONUtils.write(webScriptWriter, result);
            }
            else if (method.equals("remove")) {
                nodeService.removeAspect(new NodeRef(nodeRef), DatabaseModel.ASPECT_DECLARATIONMARKEDFOREDIT);
                result = JSONUtils.getSuccess();
                JSONUtils.write(webScriptWriter, result);
            }
            else  if (method.equals("state")) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("state", nodeService.hasAspect(new NodeRef(nodeRef), DatabaseModel.ASPECT_DECLARATIONMARKEDFOREDIT));

                result = jsonObject;
                JSONUtils.write(webScriptWriter, result);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            result = JSONUtils.getError(e);
            webScriptResponse.setStatus(400);
            JSONUtils.write(webScriptWriter, result);
        }
    }
}
