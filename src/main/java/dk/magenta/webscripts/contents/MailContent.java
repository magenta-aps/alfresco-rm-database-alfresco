package dk.magenta.webscripts.contents;

import dk.magenta.beans.ContentsBean;
import dk.magenta.beans.MailBean;
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

public class MailContent extends AbstractWebScript {

    private MailBean mailBean;

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

        String nodeId = params.get("node");
        NodeRef nodeRef = new NodeRef("workspace://SpacesStore/" + nodeId);

        try {
            mailBean.transform(nodeRef);
//            result = JSONUtils.getObject("downloadNodeRef", downloadNodeRef.toString());
            result = JSONUtils.getSuccess();

        } catch (Exception e) {
            e.printStackTrace();
            result = JSONUtils.getError(e);
            webScriptResponse.setStatus(400);
        }
        JSONUtils.write(webScriptWriter, result);
    }
}
