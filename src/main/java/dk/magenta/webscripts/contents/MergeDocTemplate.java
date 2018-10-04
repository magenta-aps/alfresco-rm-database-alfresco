package dk.magenta.webscripts.contents;

import dk.magenta.beans.DocumentTemplateBean;
import dk.magenta.beans.MailBean;
import dk.magenta.utils.JSONUtils;
import org.alfresco.service.cmr.repository.NodeRef;
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

public class MergeDocTemplate extends AbstractWebScript {

    public void setDocumentTemplateBean(DocumentTemplateBean documentTemplateBean) {
        this.documentTemplateBean = documentTemplateBean;
    }

    private DocumentTemplateBean documentTemplateBean;



    @Override
    public void execute(WebScriptRequest webScriptRequest, WebScriptResponse webScriptResponse) throws IOException {
        Content c = webScriptRequest.getContent();
        System.out.println("status på mailBean" + documentTemplateBean);
        JSONObject json = null;

        try {
            json = new JSONObject(c.getContent());
            System.out.println(json);


        webScriptResponse.setContentEncoding("UTF-8");
        Writer webScriptWriter = webScriptResponse.getWriter();
        JSONObject result;

        Map<String, String> params = JSONUtils.parseParameters(webScriptRequest.getURL());

        String nodeRef = params.get("nodeRef");
        System.out.println(nodeRef);

        documentTemplateBean.populateDocument(new NodeRef("workspace://SpacesStore/" + json.get("id")), (String)json.get("type") , (String)json.get("retten"), (String)json.get("dato") );
            result = JSONUtils.getSuccess();
            JSONUtils.write(webScriptWriter, result);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
