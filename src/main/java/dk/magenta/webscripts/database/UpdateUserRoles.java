package dk.magenta.webscripts.database;

import dk.magenta.beans.DatabaseBean;
import dk.magenta.utils.JSONUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

public class UpdateUserRoles extends AbstractWebScript {

    private DatabaseBean databaseBean;
    public void setDatabaseBean(DatabaseBean databaseBean) {
        this.databaseBean = databaseBean;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();

        res.setContentEncoding("UTF-8");
        Content c = req.getContent();
        Writer webScriptWriter = res.getWriter();
        JSONObject result;

        try {
            JSONObject json = new JSONObject(c.getContent());
            String siteShortName = templateArgs.get("siteShortName");
            String username = templateArgs.get("username");
            JSONArray addGroups = JSONUtils.getArray(json, "addGroups");
            JSONArray removeGroups = JSONUtils.getArray(json, "removeGroups");
            result = databaseBean.updateUserRoles(siteShortName, username, addGroups, removeGroups);

        } catch (Exception e) {
            e.printStackTrace();
            result = JSONUtils.getError(e);
            res.setStatus(400);
        }
        JSONUtils.write(webScriptWriter, result);
    }
}

// F.eks. curl -i -u admin:admin -X GET 'http://localhost:8080/alfresco/s/database/retspsyk/role'