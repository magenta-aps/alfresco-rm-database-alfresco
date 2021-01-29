package dk.magenta.webscripts.entry;

import dk.magenta.beans.DatabaseBean;
import dk.magenta.beans.EntryBean;
import dk.magenta.utils.JSONUtils;
import dk.magenta.utils.QueryUtils;
import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class GetAUTOcompleteEntries extends AbstractWebScript {

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
            String input = req.getParameter("input");

            String onlyFlow = req.getParameter("onlyflow");
            System.out.println("hvad er onlyflow");
            System.out.println(onlyFlow);

            String keyValue = "";


            if (input.contains("!")) {
                input = input.replace("!","");
                keyValue = "[{\"key\" :\"caseNumber\" , \"value\" : \"" + input +  "\"" + " , \"include\" :\"true\"}]";
            }
            else if (input.matches(".*\\d+.*")) {
                keyValue = "[{\"key\" :\"cprNumber\" , \"value\" : \"" + input +  "*\"" + " , \"include\" :\"true\"}]";
            }
            else {
                keyValue = "[{\"key\" :\"fullName\" , \"value\" : \"" + input +  "*\"" + " , \"include\" :\"true\"}]";
            }



            org.json.JSONArray entries = new org.json.JSONArray();


            String type = databaseBean.getType(siteShortName);
            String query = QueryUtils.getKeyValueQuery(siteShortName, type, new org.json.JSONArray(keyValue));

            if (onlyFlow != null && onlyFlow.equals("true")) {
                query += " AND -ASPECT:\"rm:skip_flowchart\"";
                query += " AND ISUNSET:\"rm:closed\"";
            }

            System.out.println("hvad er query");
            System.out.println(query);



            List<NodeRef> nodeRefs = entryBean.getEntries(query, skip, maxItems, "@rm:creationDate", true);


            Iterator<NodeRef> i = nodeRefs.iterator();

            while (i.hasNext()) {
                NodeRef nodeRef = i.next();


                JSONObject tmp = entryBean.toJSON(nodeRef);
                JSONObject e = new JSONObject();

                if (tmp.has("cprNumber")) {
                    e.put("cprNumber", tmp.get("cprNumber"));
                }

                e.put("caseNumber", tmp.get("caseNumber"));
                e.put("firstName", tmp.get("firstName"));
                e.put("lastName", tmp.get("lastName"));
                e.put("node-uuid", tmp.get("node-uuid"));

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

//http://localhost:8080/alfresco/service/database/retspsyk/page_entries?skip=0&maxItems=10&keyValue=[{%22key%22%20:%20%22cprNumber%22,%20%22value%22%20:%20%220111575415%22}]
