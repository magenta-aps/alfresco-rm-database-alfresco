package dk.magenta.webscripts.database;

import dk.magenta.beans.DatabaseBean;
import dk.magenta.utils.JSONUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

public class GetRole extends AbstractWebScript {

    private DatabaseBean databaseBean;
    public void setDatabaseBean(DatabaseBean databaseBean) {
        this.databaseBean = databaseBean;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
        res.setContentEncoding("UTF-8");
        Writer webScriptWriter = res.getWriter();
        JSONArray result = new JSONArray();

        try {
            String siteShortName = templateArgs.get("siteShortName");
            result = JSONUtils.getArray(databaseBean.getRole(siteShortName));

        } catch (Exception e) {
            e.printStackTrace();
            result.put(JSONUtils.getError(e));
            res.setStatus(400);
        }
        JSONUtils.write(webScriptWriter, result);
    }
}

// F.eks. curl -i -u admin:admin -X GET 'http://localhost:8080/alfresco/s/database/retspsyk/role'