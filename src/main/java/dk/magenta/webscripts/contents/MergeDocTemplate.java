package dk.magenta.webscripts.contents;

import dk.magenta.beans.DocumentTemplateBean;
import dk.magenta.beans.MailBean;
import dk.magenta.model.DatabaseModel;
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
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

public class MergeDocTemplate extends AbstractWebScript {

    public void setDocumentTemplateBean(DocumentTemplateBean documentTemplateBean) {
        this.documentTemplateBean = documentTemplateBean;
    }

    private DocumentTemplateBean documentTemplateBean;
    private JSONObject result;
    Writer webScriptWriter;


    @Override
    public void execute(WebScriptRequest webScriptRequest, WebScriptResponse webScriptResponse) throws IOException {
        Content c = webScriptRequest.getContent();
        System.out.println("status på mailBean" + documentTemplateBean);
        JSONObject json = null;

        try {
            json = new JSONObject(c.getContent());
            System.out.println(json);


        webScriptResponse.setContentEncoding("UTF-8");
        webScriptWriter = webScriptResponse.getWriter();


        Map<String, String> params = JSONUtils.parseParameters(webScriptRequest.getURL());

        String nodeRef = params.get("nodeRef");
        System.out.println(nodeRef);

        if (json.has("type")) {

            if (json.get("type").equals(DatabaseModel.PROP_TEMPLATE_DOC_KENDELSE)) {

                if (json.has("dato") && json.has("retten")) {
                    String newDocument = documentTemplateBean.populateDocument(new NodeRef("workspace://SpacesStore/" + json.get("id")), (String)json.get("type") , (String)json.get("retten"), (String)json.get("dato") );
                    result = JSONUtils.getObject("id", newDocument.toString());
                    JSONUtils.write(webScriptWriter, result);
                }
                else {
                    result = JSONUtils.getError(new Exception("wrong parameters supplied"));
                    JSONUtils.write(webScriptWriter, result);
                }
            }
            else {
                String newDocument = documentTemplateBean.populateDocument(new NodeRef("workspace://SpacesStore/" + json.get("id")), (String)json.get("type") , "", "" );
                result = JSONUtils.getObject("id", newDocument.toString());
                JSONUtils.write(webScriptWriter, result);
            }
        }
        else {
            result = JSONUtils.getError(new Exception("wrong parameters supplied"));
            JSONUtils.write(webScriptWriter, result);
        }


        }
        catch (org.alfresco.service.cmr.model.FileExistsException e) {
            System.out.println(e.toString());
            result = JSONUtils.getError("document already exists");
            JSONUtils.write(webScriptWriter, result);
        }
        catch (Exception e) {
            e.printStackTrace();
            result = JSONUtils.getError(new Exception(e.toString()));
            JSONUtils.write(webScriptWriter, result);

        }

    }

}
