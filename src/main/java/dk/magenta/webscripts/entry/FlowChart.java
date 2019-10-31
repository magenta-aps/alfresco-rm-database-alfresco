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

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();

        System.out.println("templateArgs");
        System.out.println(templateArgs);

        res.setContentEncoding("UTF-8");
        Content c = req.getContent();
        JSONObject result = new JSONObject();

        String siteShortName = templateArgs.get("siteShortName");

        JSONObject jsonProperties = null;

        String defaultQuery = "ISUNSET:\"rm:closed\"";
        defaultQuery += " AND -ASPECT:\"rm:bua\"";



        JSONObject json = null;
        try {

            System.out.println("hvad er c content");
            System.out.println(c.getContent());

            json = new JSONObject(c.getContent());
            jsonProperties = JSONUtils.getObject(json, "properties");
            String method = jsonProperties.getString("method");


            List<NodeRef> entries;

            switch (method) {
                case "arrestanter":
                    entries = flowChartBean.getEntriesByArrestanter(siteShortName, defaultQuery);
                    result.put("entries", flowChartBean.nodeRefsTOData(entries));
                    result.put("total", entries.size());

                    break;
                case "user":
                    String user = jsonProperties.getString("user");
                    entries = flowChartBean.getEntriesbyUser(user, siteShortName, defaultQuery);
                    result.put("entries", flowChartBean.nodeRefsTOData(entries));
                    result.put("total", entries.size());
                    break;
                case "ongoing":
                    entries = flowChartBean.getEntriesByOngoing(siteShortName, defaultQuery);
                    result.put("entries", flowChartBean.nodeRefsTOData(entries));
                    result.put("total", entries.size());
                    break;
                case "waitinglist":
//                    nodeRef = nodeBean.getNodeByPath(OpenDeskModel.PATH_TEXT_TEMPLATES);
                    break;
            }



        } catch (JSONException e) {
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