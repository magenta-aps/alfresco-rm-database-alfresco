package dk.magenta.webscripts.entry;

import dk.magenta.beans.DatabaseBean;
import dk.magenta.beans.EntryBean;
import dk.magenta.utils.JSONUtils;
import dk.magenta.utils.QueryUtils;
import org.alfresco.service.cmr.repository.NodeRef;
import org.activiti.engine.impl.util.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

public class GetPaginetedEntries extends AbstractWebScript {

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

//        http://localhost:8080/alfresco/service/database/retspsyk/page_entries/?skip=0&maxItems=2&keyValue=[{%22key%22:%22lort%22,%20%22value%22:%22ihovedet%22}]

        Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
        res.setContentEncoding("UTF-8");
        Writer webScriptWriter = res.getWriter();
        JSONObject result = new JSONObject();

        try {
            String siteShortName = templateArgs.get("siteShortName");
            int skip = Integer.valueOf(req.getParameter("skip"));
            int maxItems = Integer.valueOf(req.getParameter("maxItems"));
            String keyValue = req.getParameter("keyValue");


            org.json.JSONArray entries = new org.json.JSONArray();


            String type = databaseBean.getType(siteShortName);
            String query = QueryUtils.getKeyValueQuery(siteShortName, type, new org.json.JSONArray(keyValue));

            List<NodeRef> nodeRefs = entryBean.getEntries(query, skip, maxItems, "@rm:creationDate", true);

            Iterator<NodeRef> i = nodeRefs.iterator();

            while (i.hasNext()) {
                NodeRef nodeRef = i.next();

                JSONObject tmp = entryBean.toJSON(nodeRef);

                JSONObject e = new JSONObject();

                e.put("caseNumber", tmp.get("caseNumber"));
                e.put("cpr", tmp.get("cprNumber"));
                e.put("fullName", tmp.get("fullName"));
                e.put("creationDate", tmp.get("creationDate"));
                e.put("doctor", tmp.get("doctor"));
                e.put("closed", tmp.get("doctor"));
                e.put("declarationDate", tmp.get("declarationDate"));
                e.put("psychologist", tmp.get("psychologist"));


                entries.put(e);
            }

            result.put("entries", entries);
            result.put("back", skip);
            result.put("next", skip + maxItems);
            result.put("total", entryBean.getEntries(query, 0, 1000, "@rm:creationDate", true).size());

        } catch (Exception e) {
            e.printStackTrace();
            result = JSONUtils.getError(e);
            res.setStatus(400);
        }

        JSONUtils.write(webScriptWriter, result);
    }
}

// F.eks. curl -i -u admin:admin -X GET 'http://localhost:8080/alfresco/s/database/retspsyk/entry/445644-4545-4564-8848-1849155'

//http://localhost:8080/alfresco/service/database/retspsyk/page_entries?skip=0&maxItems=10&keyValue=[{"key":"cprNumber","value" : "220111571234", "include" : "true"}]