package dk.magenta.webscripts.entry;

import dk.magenta.beans.DatabaseBean;
import dk.magenta.beans.EntryBean;
import dk.magenta.utils.JSONUtils;
import dk.magenta.utils.QueryUtils;
import org.alfresco.service.cmr.repository.NodeRef;

import org.alfresco.service.cmr.repository.NodeService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

public class GetPaginetedEntries extends AbstractWebScript {

    private EntryBean entryBean;
    private DatabaseBean databaseBean;

    public NodeService getNodeService() {
        return nodeService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    private NodeService nodeService;

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
        Content c = req.getContent();

        Writer webScriptWriter = res.getWriter();
        JSONObject result = new JSONObject();
        System.out.println("hejjjjjj");

        try {
            String siteShortName = templateArgs.get("siteShortName");
            int skip = Integer.valueOf(req.getParameter("skip"));
            int maxItems = Integer.valueOf(req.getParameter("maxItems"));



            String keyValue = req.getParameter("keyValue");


            // setup query

            JSONObject input = new JSONObject(c.getContent());
            JSONArray queryArray = new JSONArray();

            if (input.has("waitingTime")) {
                JSONObject o = new JSONObject();
                o.put("key", QueryUtils.mapWaitingType(input.getJSONObject("waitingTime").getString("time")));
                o.put("value", QueryUtils.waitingQuery(input.getJSONObject("waitingTime").getInt("days"), input.getJSONObject("waitingTime").getString("modifier")));
                o.put("include", true);
                queryArray.put(o);
            }

            if (input.has("mainCharge")) {
                JSONObject o = new JSONObject();
                o.put("key", "mainCharge");
                o.put("value", "\"" + input.get("mainCharge") + "\"");
                o.put("include", true);
                queryArray.put(o);
            }

            if (input.has("sanctionProposal")) {
                JSONObject o = new JSONObject();
                o.put("key", "sanctionProposal");
                o.put("value", "\"" + input.get("sanctionProposal") + "\"");
                o.put("include", true);
                queryArray.put(o);
            }

            if (input.has("placement")) {
                JSONObject o = new JSONObject();
                o.put("key", "placement");
                o.put("value", "\"" + input.get("placement") + "\"");
                o.put("include", true);
                queryArray.put(o);
            }

            if (input.has("mainDiagnosis")) {
                JSONObject o = new JSONObject();
                o.put("key", "mainDiagnosis");
                o.put("value", "\"" + input.get("mainDiagnosis") + "\"");
                o.put("include", true);
                queryArray.put(o);
            }

            if (input.has("status")) {
                JSONObject o = new JSONObject();
                o.put("key", "status");
                o.put("value", input.get("status"));
                o.put("include", true);
                queryArray.put(o);
            }

            if (input.has("givenDeclaration")) {
                JSONObject o = new JSONObject();
                o.put("key", "givenDeclaration");
                o.put("value", input.get("givenDeclaration"));
                o.put("include", true);
                queryArray.put(o);
            }

            if (input.has("doctor")) {
                JSONObject o = new JSONObject();
                o.put("key", "doctor");
                o.put("value", input.get("doctor"));
                o.put("include", true);
                queryArray.put(o);
            }

            if (input.has("noDeclaration")) {
                JSONObject o = new JSONObject();
                o.put("key", "noDeclaration");
                o.put("value", input.get("noDeclaration"));
                o.put("include", true);
                queryArray.put(o);
            }

            if (input.has("closed")) {
                JSONObject o = new JSONObject();
                o.put("key", "closed");
                o.put("value", input.get("closed"));
                o.put("include", true);
                queryArray.put(o);
            }

            if (input.has("closedWithoutDeclarationReason")) {
                JSONObject o = new JSONObject();
                o.put("key", "closedWithoutDeclarationReason");
                o.put("value", input.get("closedWithoutDeclarationReason"));
                o.put("include", true);
                queryArray.put(o);
            }

            if (input.has("psychEval")) {
                JSONObject o = new JSONObject();
                o.put("key", "psychEval");


                if (input.get("psychEval").equals("true"))
                    o.put("value", "-@rm\\:psychologist:NULL");
                else {
                    o.put("value", "@rm\\:psychologist:NULL");
                }
                o.put("include", true);
                queryArray.put(o);
            }

            if (input.has("psychologist")) {
                JSONObject o = new JSONObject();
                o.put("key", "psychologist");
                o.put("value", input.get("psychologist"));
                o.put("include", true);
                queryArray.put(o);
            }

            if (input.has("socialEval")) {
                JSONObject o = new JSONObject();
                o.put("key", "socialEval");

                if (input.get("socialEval").equals("true"))
                    o.put("value", "-@rm\\:socialworker:NULL");
                else {
                    o.put("value", "@rm\\:socialworker:NULL");
                }
                o.put("include", true);
                queryArray.put(o);
            }

            if (input.has("socialworker")) {
                JSONObject o = new JSONObject();
                o.put("key", "socialworker");
                o.put("value", input.get("socialworker"));
                o.put("include", true);
                queryArray.put(o);
            }

            org.json.JSONArray entries = new org.json.JSONArray();

            String type = databaseBean.getType(siteShortName);
            String query = QueryUtils.getKeyValueQuery(siteShortName, type, queryArray);
            System.out.println("the query");
            System.out.println(query);


            if (input.has("bua")) {
                boolean bua = input.getBoolean("bua");

                if (bua) {
                    query = query + " AND +ASPECT:\"rm:bua\"";
                }
            }


            List<NodeRef> nodeRefs = entryBean.getEntries(query, skip, maxItems, "@rm:creationDate", true);

            Iterator<NodeRef> i = nodeRefs.iterator();

            while (i.hasNext()) {
                NodeRef nodeRef = i.next();

                JSONObject tmp = entryBean.toJSON(nodeRef);

                JSONObject e = new JSONObject();

                e.put("caseNumber", tmp.get("caseNumber"));

                if (e.has("cprNumber")) {
                    e.put("cprNumber", tmp.get("cprNumber"));
                }

                e.put("fullName", tmp.get("fullName"));
                e.put("creationDate", tmp.get("creationDate"));

                if (tmp.has("doctor")) {
                    e.put("doctor", tmp.get("doctor"));
                }

                if (tmp.has("closed")) {
                    e.put("closed", tmp.get("closed"));
                }

                if (tmp.has("declarationDate")) {
                    e.put("declarationDate", tmp.get("declarationDate"));
                }

                if (tmp.has("psychologist")) {
                    e.put("psychologist", tmp.get("psychologist"));
                }

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

