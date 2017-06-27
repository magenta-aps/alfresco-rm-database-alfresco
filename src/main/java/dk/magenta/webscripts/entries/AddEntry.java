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
            System.out.println(json);
            JSONObject jsonProperties = JSONUtils.getObject(json, "properties");

            QName type = QName.createQName(DatabaseModel.RM_MODEL_URI, typeStr);
            Map<QName, Serializable> properties = JSONUtils.getMap(jsonProperties);

            JSONObject caseNumber = entryBean.addEntry(siteShortName, type, properties);
            System.out.println(caseNumber);
            System.out.println(caseNumber.get("caseNumber"));
            result = JSONUtils.getSuccess(caseNumber.getString("caseNumber"));

        } catch (Exception e) {
            e.printStackTrace();
            result = JSONUtils.getError(e);
            webScriptResponse.setStatus(400);
        }
        JSONUtils.write(webScriptWriter, result);
    }
}
