package dk.magenta.webscripts.entry;

import dk.magenta.beans.EntryBean;
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

public class UpdateEntry extends AbstractWebScript {

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

            String uuid = templateArgs.get("uuid");
            NodeRef nodeRef = entryBean.getNodeRef(uuid);
            entryBean.updateEntry(nodeRef, properties);


            // TODO make a proper setup for handling business rules - if the system needs to be used at different cusotmers


            System.out.println("tak for lor22t");

            System.out.println(jsonProperties.toString());

            if ( jsonProperties.has("observationDate") && jsonProperties.has("declarationDate") ) {
                System.out.println("all three");

                entryBean.calculateActive(nodeRef);
                entryBean.calculatePassive(nodeRef);
                entryBean.calculateTotal(nodeRef);


            } else if (jsonProperties.has("observationDate") && !jsonProperties.has("declarationDate")) {
                System.out.println("only dat one");
                entryBean.calculatePassive(nodeRef);
            }




//            if (jsonProperties.get(Conten) ==) {
//
//            }
//            else if (){
//
//            }

            result = entryBean.toJSON(nodeRef);

        } catch (Exception e) {
            e.printStackTrace();
            result = JSONUtils.getError(e);
            res.setStatus(400);
        }
        JSONUtils.write(webScriptWriter, result);
    }
}

// F.eks curl -i -u admin:admin -X PUT -H "Content-Type: application/json" -d '{ "properties" : {"motherEthnicity":"Svensk","doctor1":"Doctor New Name","verdictDate":"2018-08-03T00:00:00.000Z","isClosed":"true","petitionDate":"2018-07-20T00:00:00.000Z","endedWithoutDeclaration":"true"} }' 'http://localhost:8080/alfresco/s/entry/445644-4545-4564-8848-1849155'