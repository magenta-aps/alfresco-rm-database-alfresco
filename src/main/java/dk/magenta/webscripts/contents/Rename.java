

package dk.magenta.webscripts.contents;

import dk.magenta.beans.ContentsBean;
import dk.magenta.utils.JSONUtils;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.io.Writer;

public class Rename extends AbstractWebScript {

    public void setContentsBean(ContentsBean contentsBean) {
        this.contentsBean = contentsBean;
    }

    ContentsBean contentsBean;

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
            json = new JSONObject(c.getContent());

            String nodeRef = (String)json.get("nodeRef");
            String name = (String)json.get("name");

            contentsBean.rename(new NodeRef(nodeRef), name);

            result = JSONUtils.getSuccess();
            JSONUtils.write(webScriptWriter, result);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}

