package dk.magenta.webscripts.entry;

import dk.magenta.beans.DatabaseBean;
import dk.magenta.beans.EntryBean;
import dk.magenta.beans.PrintBean;
import dk.magenta.model.DatabaseModel;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static dk.magenta.model.DatabaseModel.PROP_PSYC_LIBRARY_PSYCH_TYPE;
import static dk.magenta.model.DatabaseModel.RMPSY_MODEL_PREFIX;

public class GetPaginetedEntries extends AbstractWebScript {

    private EntryBean entryBean;
    private DatabaseBean databaseBean;

    public void setPrintBean(PrintBean printBean) {
        this.printBean = printBean;
    }

    private PrintBean printBean;

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

        try {
            String siteShortName = templateArgs.get("siteShortName");
            int skip = Integer.valueOf(req.getParameter("skip"));
            int maxItems = Integer.valueOf(req.getParameter("maxItems"));



            String keyValue = req.getParameter("keyValue");



            // setup query

            JSONObject input = new JSONObject(c.getContent());
            System.out.println("hvad er input");
            System.out.println(input);

            JSONArray queryArray = new JSONArray();


            LocalDateTime f_date = null;

            // date range for createtiondate

            if (input.has("createdFromDate")) {
                JSONObject o = new JSONObject();

                String f_formattedDate = (String)input.get("createdFromDate");

                o.put("key", "creationDate");

                if (input.has("createdToDate")) {


                    String t_formattedDate = (String)input.get("createdToDate");
                    o.put("value", QueryUtils.dateRangeQuery(f_formattedDate, t_formattedDate));
                }
                else {
                    o.put("value", QueryUtils.dateRangeQuery(f_formattedDate, "MAX"));
                }
                o.put("include", true);
                queryArray.put(o);
            }
            else if (input.has("createdToDate")) {
                JSONObject o = new JSONObject();

                String t_formattedDate = (String)input.get("createdToDate");
                o.put("key", "creationDate");
                o.put("value", QueryUtils.dateRangeQuery("MIN",t_formattedDate));
                o.put("include", true);
                queryArray.put(o);
            }


            // date range for tilbagesendt

            if (input.has("returnOfDeclarationFromDate")) {
                JSONObject o = new JSONObject();

                String f_formattedDate = (String)input.get("returnOfDeclarationFromDate");

                o.put("key", "returnOfDeclarationDate");

                if (input.has("returnOfDeclarationToDate")) {
                    String t_formattedDate = (String)input.get("returnOfDeclarationToDate");
                    o.put("value", QueryUtils.dateRangeQuery(f_formattedDate, t_formattedDate));
                }
                else {
                    o.put("value", QueryUtils.dateRangeQuery(f_formattedDate, "MAX"));
                }
                o.put("include", true);
                queryArray.put(o);
            }
            else if (input.has("returnOfDeclarationToDate")) {
                JSONObject o = new JSONObject();

                String t_formattedDate = (String)input.get("returnOfDeclarationToDate");
                o.put("key", "returnOfDeclarationDate");
                o.put("value", QueryUtils.dateRangeQuery("MIN",t_formattedDate));
                o.put("include", true);
                queryArray.put(o);
            }





            // date range for declarationsdate


            if (input.has("declarationFromDate")) {
                JSONObject o = new JSONObject();

                String f_formattedDate = (String)input.get("declarationFromDate");

                o.put("key", "declarationDate");

                if (input.has("declarationToDate")) {

                    String t_formattedDate = (String)input.get("declarationToDate");
                    o.put("value", QueryUtils.dateRangeQuery(f_formattedDate, t_formattedDate));
                }
                else {
                    o.put("value", QueryUtils.dateRangeQuery(f_formattedDate, "MAX"));
                }
                o.put("include", true);
                queryArray.put(o);
            }
            else if (input.has("declarationToDate")) {
                JSONObject o = new JSONObject();

                String t_formattedDate = (String)input.get("declarationToDate");
                o.put("key", "declarationDate");
                o.put("value", QueryUtils.dateRangeQuery("MIN",t_formattedDate));
                o.put("include", true);
                queryArray.put(o);
            }


            if (input.has("waitingTime")) {
                JSONObject o = new JSONObject();
                o.put("key", QueryUtils.mapWaitingType(input.getJSONObject("waitingTime").getString("time")));
                o.put("value", QueryUtils.waitingQuery(input.getJSONObject("waitingTime").getInt("days"), input.getJSONObject("waitingTime").getString("modifier")));
                o.put("include", true);
                queryArray.put(o);
            }

            if (input.has("mainCharge")) {
                JSONArray jsonArray = input.getJSONArray("mainCharge");
                String queryStringMainCharge = "";

                for (int i=0; i <= jsonArray.length()-1;i++) {
                    if (i == 0) {
                        queryStringMainCharge = queryStringMainCharge + "\"" + (String) jsonArray.get(i) + "\"";
                    }
                    else {
                        queryStringMainCharge = queryStringMainCharge + " "  + "\"" +(String) jsonArray.get(i) + "\"";
                    }
                }

                JSONObject o = new JSONObject();
                o.put("key", "mainCharge");
                o.put("value", "(" + queryStringMainCharge + ")");
                o.put("include", true);
                queryArray.put(o);
            }

            if (input.has("sanctionProposal")) {

                JSONObject o = new JSONObject();
                o.put("key", "sanctionProposal");


                JSONArray jsonArray = input.getJSONArray("sanctionProposal");
                String queryStringSanctionProp = "";

                for (int i=0; i <= jsonArray.length()-1;i++) {

                    String sanctionProposal = (String) jsonArray.get(i);

                    if (i == 0) {
                        queryStringSanctionProp = queryStringSanctionProp + "\"" + sanctionProposal + "\"";
                    }
                    else {
                        queryStringSanctionProp = queryStringSanctionProp + " "  + "\"" + sanctionProposal + "\"";
                    }
                }

                o.put("value", "(" + queryStringSanctionProp + ")");
                o.put("include", true);
                queryArray.put(o);
            }

            if (input.has("placement")) {
                JSONObject o = new JSONObject();
                o.put("key", "placement");

                JSONArray jsonArray = input.getJSONArray("placement");
                String queryStringPlacement = "";

                for (int i=0; i <= jsonArray.length()-1;i++) {

                    // hack to support the import of the names without titles in the old system
                    String placement = (String) jsonArray.get(i);

                    if (i == 0) {
                        queryStringPlacement = queryStringPlacement + "\"" + placement + "\"";
                    }
                    else {
                        queryStringPlacement = queryStringPlacement + " "  + "\"" + placement + "\"";
                    }
                }


                o.put("value", "(" + queryStringPlacement + ")");
                o.put("include", true);
                queryArray.put(o);
            }

            if (input.has("mainDiagnosis")) {

                String queryStringMainCharge = "";
                JSONArray jsonArray = input.getJSONArray("mainDiagnosis");
                for (int i=0; i <= jsonArray.length()-1;i++) {
                    if (i == 0) {
                        queryStringMainCharge = queryStringMainCharge + "\"" + (String) jsonArray.get(i) + "\"";
                    }
                    else {
                        queryStringMainCharge = queryStringMainCharge + " "  + "\"" +(String) jsonArray.get(i) + "\"";
                    }
                }

                JSONObject o = new JSONObject();
                o.put("key", "mainDiagnosis");
                o.put("value", "(" + queryStringMainCharge + ")");

                o.put("include", true);
                queryArray.put(o);
            }

            if (input.has("status")) {

                String queryStringMainCharge = "";
                JSONArray jsonArray = input.getJSONArray("status");
                for (int i=0; i <= jsonArray.length()-1;i++) {
                    if (i == 0) {
                        queryStringMainCharge = queryStringMainCharge + "\"" + (String) jsonArray.get(i) + "\"";
                    }
                    else {
                        queryStringMainCharge = queryStringMainCharge + " "  + "\"" +(String) jsonArray.get(i) + "\"";
                    }
                }


                JSONObject o = new JSONObject();
                o.put("key", "status");
                o.put("value", "(" + queryStringMainCharge + ")");
                o.put("include", true);
                queryArray.put(o);
            }

            if (input.has("givenDeclaration")) {
                JSONObject o = new JSONObject();
                o.put("key", "doctor");

                if (input.getBoolean("givenDeclaration")) {
                    o.put("value", "NULL");
                    o.put("include", false);
                    queryArray.put(o);
                }
            }

            if (input.has("doctor")) {
                JSONObject o = new JSONObject();
                o.put("key", "doctor");


                JSONArray jsonArray = input.getJSONArray("doctor");
                String queryStringDoctor = "";

                for (int i=0; i <= jsonArray.length()-1;i++) {

                    // hack to support the import of the names without titles in the old system
                    String doctor = (String) jsonArray.get(i);
                    doctor = doctor.split("-")[0];

                    if (i == 0) {
                        queryStringDoctor = queryStringDoctor + "\"" + doctor + "\"";
                    }
                    else {
                        queryStringDoctor = queryStringDoctor + " "  + "\"" + doctor + "\"";
                    }
                }


//                // hack to support the import of the names without titles in the old system
//                String doctor = (String)input.get("doctor");
//                doctor = doctor.split("-")[0];

                o.put("value", "(" + queryStringDoctor + ")");
                o.put("include", true);
                queryArray.put(o);
            }

            if (input.has("supervisingDoctor")) {
                JSONObject o = new JSONObject();
                o.put("key", "supervisingDoctor");
                o.put("value", input.get("supervisingDoctor"));
                o.put("include", true);
                queryArray.put(o);
            }

            if (input.has("noDeclaration")) {
                JSONObject o = new JSONObject();
                o.put("key", "closedWithoutDeclaration");
                o.put("value", input.get("noDeclaration"));
                o.put("include", true);
                queryArray.put(o);
            }

            if (input.has("closed")) {
                JSONObject o = new JSONObject();
                o.put("key", "closed");

                String theValue = input.getString("closed");

                if (theValue.equals("OPEN")) {
                    o.put("value", true);
                    o.put("include", false);
                    queryArray.put(o);

                }
                else {
                    o.put("value", true);
                    o.put("include", true);
                    queryArray.put(o);
                }
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
                o.put("key", "psychologist");


                if (input.getBoolean("psychEval")) {
                    o.put("value", "NULL");
                    o.put("include", false);
                    queryArray.put(o);
                }
            }

            if (input.has("psychologist")) {
                JSONObject o = new JSONObject();
                o.put("key", "psychologist");

                JSONArray jsonArray = input.getJSONArray("psychologist");
                String queryStringPsychologist = "";

                for (int i=0; i <= jsonArray.length()-1;i++) {

                    // hack to support the import of the names without titles in the old system
                    String psychologist = (String) jsonArray.get(i);
                    psychologist = psychologist.split("-")[0];

                    if (i == 0) {
                        queryStringPsychologist = queryStringPsychologist + "\"" + psychologist + "\"";
                    }
                    else {
                        queryStringPsychologist = queryStringPsychologist + " "  + "\"" + psychologist + "\"";
                    }
                }

//
//                // hack to support the import of the names without titles in the old system
//                String psychologist = (String)input.get("psychologist");
//                psychologist = psychologist.split("-")[0];

                o.put("value", "(" + queryStringPsychologist + ")");
                o.put("include", true);
                queryArray.put(o);
            }

            if (input.has("socialEval")) {
                JSONObject o = new JSONObject();
                o.put("key", "socialworker");

                if (input.getBoolean("socialEval"))
                    o.put("value", "NULL");
                o.put("include", false);
                queryArray.put(o);
            }

            if (input.has("firstName")) {
                String fornavn = input.getString("firstName");
                JSONObject o = new JSONObject();

                o.put("key", "firstName");
                o.put("value", fornavn + "*");
                o.put("include", true);
                queryArray.put(o);
            }

            if (input.has("cpr")) {
                String cpr = input.getString("cpr");
                JSONObject o = new JSONObject();

                o.put("key", "cprNumber");
                o.put("value", cpr + "*");
                o.put("include", true);
                queryArray.put(o);
            }

            if (input.has("socialworker")) {
                JSONObject o = new JSONObject();
                o.put("key", "socialworker");

                JSONArray jsonArray = input.getJSONArray("socialworker");
                String queryStringSocialworker = "";

                for (int i=0; i <= jsonArray.length()-1;i++) {

                    // hack to support the import of the names without titles in the old system
                    String socialworker = (String) jsonArray.get(i);
                    socialworker = socialworker.split("-")[0];

                    if (i == 0) {
                        queryStringSocialworker = queryStringSocialworker + "\"" + socialworker + "\"";
                    }
                    else {
                        queryStringSocialworker = queryStringSocialworker + " "  + "\"" + socialworker + "\"";
                    }
                }

//                // hack to support the import of the names without titles in the old system
//                String socialworker = (String)input.get("socialworker");
//                socialworker = socialworker.split("-")[0];

                o.put("value", "(" + queryStringSocialworker + ")");
                o.put("include", true);
                queryArray.put(o);
            }

            org.json.JSONArray entries = new org.json.JSONArray();

            String type = databaseBean.getType(siteShortName);
            String query = QueryUtils.getKeyValueQuery(siteShortName, type, queryArray);


            if (input.has("bua")) {
                String bua = input.getString("bua");
                if (bua.equals("BUA")) {
                    query = query + " AND +ASPECT:\"rm:bua\"";
                }
                else if (bua.equals("PS")) {
                    query = query + " AND -ASPECT:\"rm:bua\"";
                }
            }


            String instrumentQuery = "";
            if (input.has("instruments")) {

                JSONObject instruments = input.getJSONObject("instruments");
                System.out.println("tjek lige instruments..");
                System.out.println(instruments.length());

                Iterator i = instruments.keys();

                while (i.hasNext()) {
                    String instrument = (String) i.next();
                    JSONObject values = instruments.getJSONObject(instrument);

                    System.out.println("hvad er values");
                    System.out.println(values);



                    String instrumentQuerypart = createInstrumentQuery(instrument, values);

                    if (!instrumentQuerypart.equals("")) {
                        if (instrumentQuery.equals("")) {
                            instrumentQuery = "(" + instrumentQuerypart + ")";
                        } else {
                            instrumentQuery = instrumentQuery + " AND " + "(" + instrumentQuerypart + ")";
                        }
                    }
                }

                System.out.println("total query");
                System.out.println(instrumentQuery);
            }

            if (!instrumentQuery.equals("")) {
                query = query + " AND " + instrumentQuery;
            }

            System.out.println("the query");
            System.out.println(query);

            List<NodeRef> nodeRefs;
            if (input.has("preview")) {
                // make a printable list of all the entries, not just whats to be shown on the screen
                nodeRefs = entryBean.getEntries(query, 0, 1000, "@rm:creationDate", true);
            }
            else {
                nodeRefs = entryBean.getEntries(query, skip, maxItems, "@rm:creationDate", true);
            }


            Iterator<NodeRef> i = nodeRefs.iterator();

            while (i.hasNext()) {
                NodeRef nodeRef = i.next();

                JSONObject tmp = entryBean.toJSON(nodeRef);

                JSONObject e = new JSONObject();

                e.put("caseNumber", tmp.get("caseNumber"));

                if (tmp.has("cprNumber")) {
                    e.put("cprNumber", tmp.get("cprNumber"));
                }

                if (tmp.has("fullName")) {
                    e.put("fullName", tmp.get("fullName"));
                }

                e.put("creationDate", tmp.get("creationDate"));

                if (tmp.has("doctor") && !tmp.get("doctor").equals("null")) {
                    e.put("doctor", tmp.get("doctor"));
                }

                if (tmp.has("supervisingDoctor") && !tmp.get("supervisingDoctor").equals("null")) {
                    e.put("supervisingDoctor", tmp.get("supervisingDoctor"));
                }

                if (tmp.has("closed")) {
                    e.put("closed", tmp.get("closed"));
                }

                if (tmp.has("declarationDate")) {
                    e.put("declarationDate", tmp.get("declarationDate"));
                }

                if (tmp.has("psychologist") && !tmp.get("psychologist").equals("null") ) {
                    e.put("psychologist", tmp.get("psychologist"));
                }

                if (nodeService.hasAspect(nodeRef, DatabaseModel.ASPECT_BUA)) {
                    e.put("bua",true);
                }
                else {
                    e.put("bua",false);
                }

                entries.put(e);
            }

            if (input.has("preview")) {
                String nodeRef = printBean.printEntriesToPDF(entries);
                result.put("nodeRef", nodeRef);
            }
            else {
                result.put("entries", entries);
                result.put("back", skip);
                result.put("next", skip + maxItems);
                result.put("total", entryBean.getEntries(query, 0, 1000, "@rm:creationDate", true).size());
            }





        } catch (Exception e) {
            e.printStackTrace();
            result = JSONUtils.getError(e);
            res.setStatus(400);
        }

        JSONUtils.write(webScriptWriter, result);
    }

    public String createInstrumentQuery(String instrument, JSONObject values) throws JSONException {

        String stdQuery = "@" + RMPSY_MODEL_PREFIX + "\\:" + instrument + ":";
        String returnQuery = "";

        ArrayList selected = new ArrayList();

        Iterator keys = values.keys();

        while (keys.hasNext()) {
            String key = (String)keys.next();
            if (values.getBoolean(key)) {

                if (returnQuery.equals("")) {
                    returnQuery = stdQuery + key;
                }
                else {
                    returnQuery = returnQuery + " OR " + stdQuery + key;
                }
                selected.add(key);
            }
        }

        System.out.println("selected for:" + instrument);
        System.out.println(String.join(",", selected));

        System.out.println("hvad er returnQuery");
        System.out.println(returnQuery);



//        switch (instrument) {
//
//            case PROP_PSYC_LIBRARY_PSYCH_TYPE:
//                break;
//        }

        return returnQuery;
    }
}

// F.eks. curl -i -u admin:admin -X GET 'http://localhost:8080/alfresco/s/database/retspsyk/entry/445644-4545-4564-8848-1849155'

//http://localhost:8080/alfresco/service/database/retspsyk/page_entries?skip=0&maxItems=10&keyValue=[{"key":"cprNumber","value" : "220111571234", "include" : "true"}]
