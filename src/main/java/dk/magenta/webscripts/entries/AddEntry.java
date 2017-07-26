package dk.magenta.webscripts.entries;

import dk.magenta.beans.EntryBean;
import dk.magenta.model.DatabaseModel;
import dk.magenta.utils.JSONUtils;
import dk.magenta.utils.TypeUtils;
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
            String type = JSONUtils.getString(json, "type");
            JSONObject jsonProperties = JSONUtils.getObject(json, "properties");

            Map<QName, Serializable> properties = JSONUtils.getMap(jsonProperties);
            NodeRef nodeRef = entryBean.addEntry(siteShortName, type, properties);
            result = entryBean.toJSON(nodeRef);

        } catch (Exception e) {
            e.printStackTrace();
            result = JSONUtils.getError(e);
            webScriptResponse.setStatus(400);
        }
        JSONUtils.write(webScriptWriter, result);
    }
}

// F.eks. curl -i -u admin:admin -X POST -H "Content-Type: application/json" -d '{ "siteShortName" : "retspsyk", "type" : "forensicPsychiatryDeclaration", "properties" : {"motherEthnicity":"Svensk","doctor1":"Doctor New Name", "biDiagnoses":["hello, okay hører sammen","test, hører sammen. også med"], "verdictDate":"2018-08-03T00:00:00.000Z","isClosed":"true","petitionDate":"2018-07-20T00:00:00.000Z","endedWithoutDeclaration":"true"}  }' http://localhost:8080/alfresco/service/entry