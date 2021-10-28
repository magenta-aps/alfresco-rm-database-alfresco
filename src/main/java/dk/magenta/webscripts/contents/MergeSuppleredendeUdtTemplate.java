package dk.magenta.webscripts.contents;

import dk.magenta.beans.DocumentTemplateBean;
import dk.magenta.utils.JSONUtils;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.io.Writer;

public class MergeSuppleredendeUdtTemplate extends AbstractWebScript {

    public void setDocumentTemplateBean(DocumentTemplateBean documentTemplateBean) {
        this.documentTemplateBean = documentTemplateBean;
    }

    private DocumentTemplateBean documentTemplateBean;
    private JSONObject result;
    Writer webScriptWriter;


    @Override
    public void execute(WebScriptRequest webScriptRequest, WebScriptResponse webScriptResponse) throws IOException {
        Content c = webScriptRequest.getContent();
        JSONObject json = null;

        try {
            json = new JSONObject(c.getContent());

        webScriptResponse.setContentEncoding("UTF-8");
        webScriptWriter = webScriptResponse.getWriter();

        System.out.println("immer hefe snitzhel");

        NodeRef newDocument = documentTemplateBean.generateSuppleredeUdtalelseDocument(new NodeRef("workspace://SpacesStore/" + (String)json.get("id")));
        result = JSONUtils.getObject("id", newDocument.getId());
        JSONUtils.write(webScriptWriter, result);

        AuthenticationUtil.clearCurrentSecurityContext();

        }
        catch (org.alfresco.service.cmr.model.FileExistsException e) {
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
