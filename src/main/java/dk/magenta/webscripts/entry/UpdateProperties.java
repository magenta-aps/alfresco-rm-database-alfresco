package dk.magenta.webscripts.entry;

import dk.magenta.beans.EntryBean;
import dk.magenta.utils.JSONUtils;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;

public class UpdateProperties extends AbstractWebScript {

    private EntryBean entryBean;
    public void setEntryBean(EntryBean entryBean) {
        this.entryBean = entryBean;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();

        res.setContentEncoding("UTF-8");
        Content c = req.getContent();
        Writer webScriptWriter = res.getWriter();
        JSONObject result;

        try {
            JSONObject json = new JSONObject(c.getContent());
            JSONObject jsonProperties = JSONUtils.getObject(json, "properties");
            Map<QName, Serializable> properties = JSONUtils.getMap(jsonProperties);

            String caseid = templateArgs.get("uuid");
            
            NodeRef n = entryBean.updateProperty(caseid, properties);



            result = entryBean.toJSON(n);

        } catch (Exception e) {
            e.printStackTrace();
            result = JSONUtils.getError(e);
            res.setStatus(400);
        }
        JSONUtils.write(webScriptWriter, result);
    }
}

// F.eks curl -i -u admin:admin -X PUT -H "Content-Type: application/json" -d '{ "properties" : {"motherEthnicity":"Svensk","doctor1":"Doctor New Name","verdictDate":"2018-08-03T00:00:00.000Z","isClosed":"true","petitionDate":"2018-07-20T00:00:00.000Z","endedWithoutDeclaration":"true"} }' 'http://localhost:8080/alfresco/s/entry/445644-4545-4564-8848-1849155'


//{properties : {verdict : "domsafsigelse1"}}