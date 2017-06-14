package dk.magenta.webscripts.entries;

import dk.magenta.beans.EntryBean;
import dk.magenta.model.DatabaseModel;
import dk.magenta.utils.JSONUtils;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.site.SiteService;
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

public class AddEntry extends AbstractWebScript {

    private EntryBean entryBean;
    public void setEntryBean(EntryBean entryBean) {
        this.entryBean = entryBean;
    }

    @Override
    public void execute(WebScriptRequest webScriptRequest, WebScriptResponse webScriptResponse) throws IOException {
        webScriptResponse.setContentEncoding("UTF-8");
        Content c = webScriptRequest.getContent();
        Writer webScriptWriter = webScriptResponse.getWriter();
        JSONObject result;

        try {
            JSONObject json = new JSONObject(c.getContent());
            String siteShortName = JSONUtils.getString(json, "siteShortName");
            String typeStr = JSONUtils.getString(json, "type");
            JSONObject jsonProperties = JSONUtils.getObject(json, "properties");

            QName type = QName.createQName(DatabaseModel.RM_MODEL_URI, typeStr);
            Map<QName, Serializable> properties = JSONUtils.getMap(jsonProperties);

            entryBean.addEntry(siteShortName, type, properties);
            result = JSONUtils.getSuccess();

        } catch (Exception e) {
            e.printStackTrace();
            result = JSONUtils.getError(e);
            webScriptResponse.setStatus(400);
        }
        JSONUtils.write(webScriptWriter, result);
    }
}

// F.eks curl -i -u admin:admin -X POST -H "Content-Type: application/json" -d '{ "siteShortName" : "retspsyk", "type" : "forensicPsychiatryDeclaration", "properties" : {"motherEthnicity":"Dansk","doctor1":"Doctor 33","verdictDate":"2017-08-3T00:00:00.000Z","isClosed":"false","petitionDate":"2006-07-20T00:00:00.000Z","endedWithoutDeclaration":"false"} }' http://localhost:8080/alfresco/s/entry