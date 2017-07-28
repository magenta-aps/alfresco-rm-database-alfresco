package dk.magenta.webscripts.contents;

import dk.magenta.beans.ContentsBean;
import dk.magenta.utils.JSONUtils;
import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

public class GetDownloadStatus extends AbstractWebScript {

    private ContentsBean contentsBean;
    public void setContentsBean(ContentsBean contentsBean) {
        this.contentsBean = contentsBean;
    }

    @Override
    public void execute(WebScriptRequest webScriptRequest, WebScriptResponse webScriptResponse) throws IOException {

        Map<String, String> params = JSONUtils.parseParameters(webScriptRequest.getURL());
        webScriptResponse.setContentEncoding("UTF-8");
        Writer webScriptWriter = webScriptResponse.getWriter();
        JSONObject result;

        try {
            String nodeRefStr = params.get("nodeRef");
            NodeRef nodeRef = new NodeRef(nodeRefStr);

            result = JSONUtils.getObject("downloadStatus", contentsBean.getDownloadStatus(nodeRef));

        } catch (Exception e) {
            e.printStackTrace();
            result = JSONUtils.getError(e);
            webScriptResponse.setStatus(400);
        }
        JSONUtils.write(webScriptWriter, result);
    }
}
