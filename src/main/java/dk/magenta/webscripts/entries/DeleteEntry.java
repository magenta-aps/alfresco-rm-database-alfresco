package dk.magenta.webscripts.entries;

import dk.magenta.beans.EntryBean;
import dk.magenta.utils.JSONUtils;
import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

public class DeleteEntry extends AbstractWebScript {

    private EntryBean entryBean;
    public void setEntryBean(EntryBean entryBean) {
        this.entryBean = entryBean;
    }

    @Override
    public void execute(WebScriptRequest webScriptRequest, WebScriptResponse webScriptResponse) throws IOException {
        Map<String, String> params = JSONUtils.parseParameters(webScriptRequest.getURL());

        webScriptResponse.setContentEncoding("UTF-8");
        Writer webScriptWriter = webScriptResponse.getWriter();
        JSONObject result;

        try {
            String type = params.get("type");
            String entryKey = params.get("entryKey");
            String entryValue = params.get("entryValue");

            NodeRef nodeRef = entryBean.getEntry(type, entryKey, entryValue);
            if(nodeRef == null)
                result = JSONUtils.getObject("error", "Entry with the type (" + type + ")" +
                        " and the variable (" + entryKey + " = " + entryValue + ") does not exist.");
            else {
                entryBean.deleteEntry(nodeRef);
                result = JSONUtils.getSuccess();
            }

        } catch (Exception e) {
            e.printStackTrace();
            result = JSONUtils.getError(e);
            webScriptResponse.setStatus(400);
        }
        JSONUtils.write(webScriptWriter, result);
    }
}

// F.eks. curl -i -u admin:admin -X DELETE 'http://localhost:8080/alfresco/s/entry?type=forensicPsychiatryDeclaration&entryKey=caseNumber&entryValue=33'