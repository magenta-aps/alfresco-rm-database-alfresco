package dk.magenta.webscripts.entries;

import dk.magenta.beans.EntryBean;
import dk.magenta.utils.JSONUtils;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.Map;

public class UpdateEntry extends AbstractWebScript {

    private EntryBean entryBean;
    public void setEntryBean(EntryBean entryBean) {
        this.entryBean = entryBean;
    }

    @Override
    public void execute(WebScriptRequest webScriptRequest, WebScriptResponse webScriptResponse) throws IOException {
        Map<String, String> params = JSONUtils.parseParameters(webScriptRequest.getURL());

        webScriptResponse.setContentEncoding("UTF-8");
        Content c = webScriptRequest.getContent();
        Writer webScriptWriter = webScriptResponse.getWriter();
        JSONObject result;

        try {
            String type = params.get("type");
            String entryKey = params.get("entryKey");
            String entryValue = params.get("entryValue");
            JSONObject json = new JSONObject(c.getContent());
            JSONObject jsonProperties = JSONUtils.getObject(json, "properties");
            Map<QName, Serializable> properties = JSONUtils.getMap(jsonProperties);

            NodeRef nodeRef = entryBean.getEntry(type, entryKey, entryValue);
            if(nodeRef == null)
                result = JSONUtils.getObject("error", "Entry with the type (" + type + ")" +
                        " and the variable (" + entryKey + " = " + entryValue + ") does not exist.");
            else {
                entryBean.updateEntry(nodeRef, properties);
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

// F.eks curl -i -u admin:admin -X PUT -H "Content-Type: application/json" -d '{ "properties" : {"motherEthnicity":"Svensk","doctor1":"Doctor New Name","verdictDate":"2018-08-3T00:00:00.000Z","isClosed":"true","petitionDate":"2018-07-20T00:00:00.000Z","endedWithoutDeclaration":"true"} }' 'http://localhost:8080/alfresco/s/entry?type=forensicPsychiatryDeclaration&entryKey=caseNumber&entryValue=71'