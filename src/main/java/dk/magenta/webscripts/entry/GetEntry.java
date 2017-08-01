package dk.magenta.webscripts.entry;

import dk.magenta.beans.DatabaseBean;
import dk.magenta.beans.EntryBean;
import dk.magenta.model.DatabaseModel;
import dk.magenta.utils.JSONUtils;
import dk.magenta.utils.QueryUtils;
import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

public class GetEntry extends AbstractWebScript {

    private EntryBean entryBean;
    private DatabaseBean databaseBean;

    public void setEntryBean(EntryBean entryBean) {
        this.entryBean = entryBean;
    }
    public void setDatabaseBean(DatabaseBean databaseBean) {
        this.databaseBean = databaseBean;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
        res.setContentEncoding("UTF-8");
        Writer webScriptWriter = res.getWriter();
        JSONObject result;

        try {
            String siteShortName = templateArgs.get("siteShortName");
            String entryValue = templateArgs.get("entryValue");

            String type = databaseBean.getType(siteShortName);
            String query = QueryUtils.getEntryQuery(siteShortName, type, entryValue);

            NodeRef nodeRef = entryBean.getEntry(query);
            result = entryBean.toJSON(nodeRef);

        } catch (Exception e) {
            e.printStackTrace();
            result = JSONUtils.getError(e);
            res.setStatus(400);
        }
        JSONUtils.write(webScriptWriter, result);
    }
}

// F.eks. curl -i -u admin:admin -X GET 'http://localhost:8080/alfresco/s/database/retspsyk/entry/445644-4545-4564-8848-1849155'