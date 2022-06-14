package dk.magenta.webscripts.entry;

import dk.magenta.beans.*;
import dk.magenta.model.DatabaseModel;
import dk.magenta.utils.JSONUtils;
import dk.magenta.utils.QueryUtils;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.codehaus.groovy.transform.SourceURIASTTransformation;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import javax.xml.soap.Node;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static dk.magenta.model.DatabaseModel.ASPECT_EXPIRYUSER;
import static dk.magenta.model.DatabaseModel.ASPECT_PSYCDATA;

public class FlowChart extends AbstractWebScript {

    private EntryBean entryBean;
    private DatabaseBean databaseBean;

    public void setMailBean(MailBean mailBean) {
        this.mailBean = mailBean;
    }

    private MailBean mailBean;

    public void setUserBean(UserBean userBean) {
        this.userBean = userBean;
    }

    private UserBean userBean;

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
        String defaultQueryForTemporaryEditedDeclaration = "";

        String userNameForbuaCheck = "";



        userNameForbuaCheck = authenticationService.getCurrentUserName();
        String buaQuery = "";

        if (nodeService.hasAspect(personService.getPerson(userNameForbuaCheck), DatabaseModel.ASPECT_BUA_USER)) {
            buaQuery += " AND ASPECT:\"rm:bua\"";
            defaultQuery += " AND ASPECT:\"rm:bua\"";
            defaultQueryForTemporaryEditedDeclaration += "ASPECT:\"rm:bua\"";
        }
        else {
            buaQuery += " AND -ASPECT:\"rm:bua\"";
            defaultQuery += " AND -ASPECT:\"rm:bua\"";
            defaultQueryForTemporaryEditedDeclaration += "-ASPECT:\"rm:bua\"";
        }

        defaultQuery += " AND -ASPECT:\"rm:skip_flowchart\"";
        defaultQueryForTemporaryEditedDeclaration += " AND -ASPECT:\"rm:skip_flowchart\"";
        defaultQueryForTemporaryEditedDeclaration += " AND ASPECT:\"rm:supopl\"";


        JSONObject json = null;
        try {

            json = new JSONObject(c.getContent());
            jsonProperties = JSONUtils.getObject(json, "properties");
            String method = jsonProperties.getString("method");

            System.out.println("hvad er method");
            System.out.println(method);

            String sort = "";
            boolean desc = false;

            List<NodeRef> entries;

            String userName;

            switch (method) {
                case "getStateOfDeclaration":
                    String casenumber = jsonProperties.getString("casenumber");
                    result.put("state", flowChartBean.getStateOfDeclaration(casenumber));
                    result.put("temporaryEdit", flowChartBean.isDeclarationMarkedForTemporaryEditing(casenumber));
                    result.put("hasAspectSupopl", flowChartBean.hasAspectSupopl(casenumber));
                    break;

                case "redflag":
                    boolean flag = jsonProperties.getBoolean("flag");
                    String nodeRef = jsonProperties.getString("nodeRef");
                    flowChartBean.toggleFlag(flag,new NodeRef("workspace://SpacesStore/" + nodeRef));
                    break;
                case "visitator":
                    String visitatorData = jsonProperties.getString("visitatorData");
                    nodeRef = jsonProperties.getString("nodeRef");
                    flowChartBean.updateVisitatorData(visitatorData,new NodeRef("workspace://SpacesStore/" + nodeRef));
                    JSONObject o = new JSONObject(visitatorData);

                    break;
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
                case "igangvaerendegr":
                    sort = jsonProperties.getString("sort");
                    desc = jsonProperties.getBoolean("desc");

                    entries = flowChartBean.getEntriesByStateIgangvaerendeGR(siteShortName, defaultQuery, sort, desc);
                    result.put("entries", flowChartBean.nodeRefsTOData(entries));
                    result.put("total", entries.size());
                    break;
                case "supopl":
                    sort = jsonProperties.getString("sort");
                    desc = jsonProperties.getBoolean("desc");

                    entries = flowChartBean.getEntriesByStateSUPOPL(siteShortName, defaultQueryForTemporaryEditedDeclaration, sort, desc);
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
                    result = flowChartBean.getTotals(siteShortName, defaultQuery, userName, buaQuery);
                    break;
                case "resetEditLock":

                    System.out.println("json?");
                    System.out.println(jsonProperties);

                    String cpr = jsonProperties.getString("cpr");
                    cpr = cpr.replace("-","");
                    String sagsnr = jsonProperties.getString("caseNumber");

                    String query = "@rm\\:caseNumber:\"" + sagsnr + "\" AND ";
                    query = query + "@rm\\:cprNumber:\"" + cpr + "\"";

                    System.out.println("hvad er query");
                    System.out.println(query);

                    NodeRef declaration = entryBean.getEntry(query);

                    flowChartBean.resetReadOnlyLock(declaration);
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

        JSONUtils.write(webScriptWriter, result);

    }
}
