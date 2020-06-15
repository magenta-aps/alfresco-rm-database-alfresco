package dk.magenta.webscripts.entry;

import dk.magenta.beans.DatabaseBean;
import dk.magenta.beans.EntryBean;
import dk.magenta.beans.FlowChartBean;
import dk.magenta.beans.PropertyValuesBean;
import dk.magenta.model.DatabaseModel;
import dk.magenta.utils.JSONUtils;
import dk.magenta.utils.QueryUtils;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.codehaus.groovy.transform.SourceURIASTTransformation;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.io.Writer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FlowChart extends AbstractWebScript {

    private EntryBean entryBean;
    private DatabaseBean databaseBean;

    public void setPropertyValuesBean(PropertyValuesBean propertyValuesBean) {
        this.propertyValuesBean = propertyValuesBean;
    }

    private PropertyValuesBean propertyValuesBean;

    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    private AuthenticationService authenticationService;

    public void setFlowChartBean(FlowChartBean flowChartBean) {
        this.flowChartBean = flowChartBean;
    }

    private FlowChartBean flowChartBean;

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

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    PersonService personService;

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();

        res.setContentEncoding("UTF-8");
        Content c = req.getContent();
        JSONObject result = new JSONObject();

        String siteShortName = templateArgs.get("siteShortName");

        JSONObject jsonProperties = null;

        String defaultQuery = "ISUNSET:\"rm:closed\"";

        String userNameForbuaCheck = "";



            userNameForbuaCheck = authenticationService.getCurrentUserName();
            System.out.println("userNameForbuaCheck");
            System.out.println(userNameForbuaCheck);

        System.out.println("(nodeService.hasAspect(personService.getPerson(userNameForbuaCheck), DatabaseModel.ASPECT_BUA_USER)");
        System.out.println(nodeService.hasAspect(personService.getPerson(userNameForbuaCheck), DatabaseModel.ASPECT_BUA_USER));


            if (nodeService.hasAspect(personService.getPerson(userNameForbuaCheck), DatabaseModel.ASPECT_BUA_USER)) {
                System.out.println("true");
                defaultQuery += " AND ASPECT:\"rm:bua\"";
            }
            else {
                System.out.println("false");
                defaultQuery += " AND -ASPECT:\"rm:bua\"";
            }

        defaultQuery += " AND -ASPECT:\"rm:skip_flowchart\"";



        JSONObject json = null;
        try {

            //System.out.println("hvad er c content");
            //System.out.println(c.getContent());

            json = new JSONObject(c.getContent());
            jsonProperties = JSONUtils.getObject(json, "properties");
            String method = jsonProperties.getString("method");

            String sort = "";
            boolean desc = false;

            List<NodeRef> entries;

            String userName;

            switch (method) {
                case "arrestanter":

                    sort = jsonProperties.getString("sort");
                    desc = jsonProperties.getBoolean("desc");
                    entries = flowChartBean.getEntriesByStateArrestanter(siteShortName, defaultQuery, sort, desc);

                    result.put("entries", flowChartBean.nodeRefsTOData(entries));
                    result.put("total", entries.size());
                    break;
                case "observation":
                    sort = jsonProperties.getString("sort");
                    desc = jsonProperties.getBoolean("desc");
                    entries = flowChartBean.getEntriesByStateObservation(siteShortName, defaultQuery, sort, desc);
                    result.put("entries", flowChartBean.nodeRefsTOData(entries));
                    result.put("total", entries.size());
                    break;
                case "ventendegr":
                    sort = jsonProperties.getString("sort");
                    desc = jsonProperties.getBoolean("desc");

                    entries = flowChartBean.getEntriesByStateVentedeGR(siteShortName, defaultQuery, sort, desc);
                    result.put("entries", flowChartBean.nodeRefsTOData(entries));
                    result.put("total", entries.size());
                    break;
                case "user":
                    sort = jsonProperties.getString("sort");
                    desc = jsonProperties.getBoolean("desc");

                    userName = propertyValuesBean.getUserByUserName(authenticationService.getCurrentUserName());
                    result.put("entries", flowChartBean.getEntriesbyUser(userName, siteShortName, defaultQuery, sort, desc));
                    break;
                case "ongoing":
                    sort = jsonProperties.getString("sort");
                    desc = jsonProperties.getBoolean("desc");

                    entries = flowChartBean.getEntriesByIgangvaerende(siteShortName, defaultQuery, sort, desc);
                    result.put("entries", flowChartBean.nodeRefsTOData(entries));
                    result.put("total", entries.size());
                    break;
                case "waitinglist":
                    sort = jsonProperties.getString("sort");
                    desc = jsonProperties.getBoolean("desc");

                    entries = flowChartBean.getWaitingList(siteShortName, defaultQuery, sort, desc);

                    result.put("entries", flowChartBean.nodeRefsTOData(entries));
                    result.put("total", entries.size());
                    break;
                case "alle":
                    userName = propertyValuesBean.getUserByUserName(authenticationService.getCurrentUserName());
                    result.put("entries", flowChartBean.getAlle(siteShortName, defaultQuery, userName));
                    break;
                case "total":
                    userName = propertyValuesBean.getUserByUserName(authenticationService.getCurrentUserName());
                    result = flowChartBean.getTotals(siteShortName, defaultQuery, userName);
                    break;
            }




        } catch (JSONException e) {
            System.out.println("json exception");
            e.printStackTrace();
        }
        catch (Exception e) {
            System.out.println("this exception");
            System.out.println(e.toString());
            e.printStackTrace();
        }

        Writer webScriptWriter = res.getWriter();

//        try {
//
//            String currentUserLogin = authenticationService.getCurrentUserName();
//
//            String userName = propertyValuesBean.getUserByUserName(currentUserLogin);
//
//            if (userName != null) {
////                flowChartBean.getEntriesbyUser(userName, siteShortName, "");
////                flowChartBean.getWaitingList(siteShortName);
//
//                String[] states = new String[2];
//                states[0] = "Ambulant/arrestant";
//                states[1] = "Ambulant/surrogatanbragt";
//
////                flowChartBean.getEntriesByStatus(siteShortName,states);
//                flowChartBean.getEntriesByOngoing(siteShortName);
//            }
//
//
//
////            result.put("entries", entries);
////            result.put("back", skip);
////            result.put("next", skip + maxItems);
////            result.put("total", entryBean.getEntries(query, 0, 1000, "@rm:creationDate", true).size());
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            result = JSONUtils.getError(e);
//            res.setStatus(400);
//                }

        JSONUtils.write(webScriptWriter, result);

    }
}

// F.eks. curl -i -u admin:admin -X GET 'http://localhost:8080/alfresco/s/database/retspsyk/entry/445644-4545-4564-8848-1849155'

//http://localhost:8080/alfresco/service/database/retspsyk/page_entries?skip=0&maxItems=10&keyValue=[{"key":"cprNumber","value" : "220111571234", "include" : "true"}]



//{
//        "properties": {
//        "method": "ongoing",
//        "value": "121212-1212"
//        }
//
//        }
