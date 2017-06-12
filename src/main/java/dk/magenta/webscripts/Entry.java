package dk.magenta.webscripts;

import dk.magenta.alfresco.utils.Utils;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.json.simple.JSONArray;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

public class Entry extends AbstractWebScript {

    public void setNodeService(NodeService nodeService) { this.nodeService = nodeService; }

    private NodeService nodeService;

    @Override
    public void execute(WebScriptRequest webScriptRequest, WebScriptResponse webScriptResponse) throws IOException {
        Map<String, String> params = Utils.parseParameters(webScriptRequest.getURL());

        webScriptResponse.setContentEncoding("UTF-8");
        Writer webScriptWriter = webScriptResponse.getWriter();
        JSONArray result;

        try {
            String nodeId = params.get("nodeRef");
            NodeRef nodeRef = new NodeRef(nodeId);
            result = Utils.convertEntryToJSON(nodeService, nodeRef);

        } catch (Exception e) {
            e.printStackTrace();
            result = Utils.getJSONError(e);
            webScriptResponse.setStatus(400);
        }
        Utils.writeJSONArray(webScriptWriter, result);
    }
}