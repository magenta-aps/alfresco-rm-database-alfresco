package dk.magenta.webscripts.entry;

import dk.magenta.beans.DatabaseBean;
import dk.magenta.beans.EntryBean;
import dk.magenta.utils.JSONUtils;
import dk.magenta.utils.QueryUtils;
import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.io.Writer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;




public class GetWaitingList extends AbstractWebScript {

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

        int skip = Integer.valueOf(req.getParameter("skip"));
        int maxItems = Integer.valueOf(req.getParameter("maxItems"));

        res.setContentEncoding("UTF-8");
        Writer webScriptWriter = res.getWriter();
        JSONObject result = new JSONObject();

        try {
            String siteShortName = templateArgs.get("siteShortName");


            org.json.JSONArray entries = new org.json.JSONArray();
            org.json.JSONArray entriesIncludingWaitingTime = new org.json.JSONArray();


            String type = databaseBean.getType(siteShortName);

            String keyValue = "[{\"key\" : \"closed\", \"value\" : \"true\", \"include\" : \"false\"}]";

            String query = QueryUtils.getKeyValueQuery(siteShortName, type, new org.json.JSONArray(keyValue));

            List<NodeRef> nodeRefs = entryBean.getEntries(query, skip, maxItems, "@rm:creationDate", true);

            Iterator<NodeRef> iterator = nodeRefs.iterator();

            while (iterator.hasNext()) {
                NodeRef nodeRef = iterator.next();
                entries.put(entryBean.toJSON(nodeRef));
            }

            // calculate the waiting time
            for (int i = 0; i < nodeRefs.size(); i++) {
                NodeRef entry = (NodeRef)nodeRefs.get(i);

                JSONObject e = entryBean.toJSON(entry);
                String creationDate = (String)e.get("creationDate");

                LocalDate d = LocalDate.parse(creationDate.substring(0,10));

                LocalDateTime timePoint = LocalDateTime.now();
                LocalDate now = timePoint.toLocalDate();

                e.put("waitingTime", d.until(now, ChronoUnit.DAYS));

                entriesIncludingWaitingTime.put(e);
            }

            result.put("entries", entriesIncludingWaitingTime);

            if ((skip-maxItems) < 0) {
                result.put("back", 0);
            }
            else {
                result.put("back", skip - maxItems);
            }


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