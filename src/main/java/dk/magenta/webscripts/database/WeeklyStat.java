package dk.magenta.webscripts.database;

import dk.magenta.beans.EntryBean;
import dk.magenta.beans.WeeklyStatBean;
import dk.magenta.utils.JSONUtils;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.Map;

public class WeeklyStat extends AbstractWebScript {

    public void setWeeklyStatBean(WeeklyStatBean weeklyStatBean) {
        this.weeklyStatBean = weeklyStatBean;
    }

    private WeeklyStatBean weeklyStatBean;

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();

        res.setContentEncoding("UTF-8");
        Content c = req.getContent();
        Writer webScriptWriter = res.getWriter();
        JSONObject result = new JSONObject();


        try {
            JSONObject json = new JSONObject(c.getContent());

            String year = JSONUtils.getString(json, "year");
            String week = JSONUtils.getString(json, "week");

            weeklyStatBean.calculate(week,year);

//            NodeRef nodeRef = entryBean.addEntry(siteShortName, type, properties, bua);
//            result = entryBean.toJSON(nodeRef);
            result.put("bua", "true");

        } catch (Exception e) {
            e.printStackTrace();
            result = JSONUtils.getError(e);
            res.setStatus(400);
        }
        JSONUtils.write(webScriptWriter, result);
    }
}

// F.eks. curl -i -u admin:admin -X POST -H "Content-Type: application/json" -d '{ "siteShortName" : "retspsyk", "type" : "forensicPsychiatryDeclaration", "properties" : {"motherEthnicity":"Svensk","doctor1":"Doctor New Name", "biDiagnoses":["hello, okay hører sammen","test, hører sammen. også med"], "verdictDate":"2018-08-03T00:00:00.000Z","isClosed":"true","petitionDate":"2018-07-20T00:00:00.000Z","endedWithoutDeclaration":"true"}  }' http://localhost:8080/alfresco/service/entry
