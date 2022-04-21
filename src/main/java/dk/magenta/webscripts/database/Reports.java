package dk.magenta.webscripts.database;

import dk.magenta.beans.ReportWaitingTimeBean;
import dk.magenta.beans.WeeklyStatBean;
import dk.magenta.utils.JSONUtils;
import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Reports extends AbstractWebScript {


    public void setReportWaitingTimeBean(ReportWaitingTimeBean reportWaitingTimeBean) {
        this.reportWaitingTimeBean = reportWaitingTimeBean;
    }

    private ReportWaitingTimeBean reportWaitingTimeBean;

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();

        Content c = req.getContent();
        res.setContentEncoding("UTF-8");
        JSONObject result = null;

        try {
            result = new JSONObject();
            JSONObject json = new JSONObject(c.getContent());
            String method = JSONUtils.getString(json, "method");

            switch (method) {
                case "waitingtime":

                    String fromDate = JSONUtils.getString(json, "createdFrom");
                    String toDate = JSONUtils.getString(json, "createdTo");
                    String statusCriteria = "";

                    if (json.has("statusCriteria")) {
                        statusCriteria = json.getString("statusCriteria");
                    }

                    System.out.println("hvad er statusCriteria");
                    System.out.println(statusCriteria);

                    NodeRef report = reportWaitingTimeBean.getReport(fromDate, toDate, statusCriteria);

                    result.put("spreadsheet", report);
                    break;
            }


//            this.reportWaitingTimeBean.getReport();

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Writer webScriptWriter = res.getWriter();


        JSONUtils.write(webScriptWriter, result);
    }
}

// F.eks. curl -i -u admin:admin -X POST -H "Content-Type: application/json" -d '{ "siteShortName" : "retspsyk", "type" : "forensicPsychiatryDeclaration", "properties" : {"motherEthnicity":"Svensk","doctor1":"Doctor New Name", "biDiagnoses":["hello, okay hører sammen","test, hører sammen. også med"], "verdictDate":"2018-08-03T00:00:00.000Z","isClosed":"true","petitionDate":"2018-07-20T00:00:00.000Z","endedWithoutDeclaration":"true"}  }' http://localhost:8080/alfresco/service/entry
