package dk.magenta.webscripts.database;

import dk.magenta.beans.EntryBean;
import dk.magenta.utils.JSONUtils;
import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONArray;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class GetEntries extends AbstractWebScript {

    private EntryBean entryBean;
    public void setEntryBean(EntryBean entryBean) {
        this.entryBean = entryBean;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();

        res.setContentEncoding("UTF-8");
        Writer webScriptWriter = res.getWriter();
        JSONArray result = new JSONArray();

        try {

            // refactored to be done in the entry package
            String siteShortName = templateArgs.get("siteShortName");

            Set<NodeRef> nodeRefs = entryBean.getEntries(siteShortName);
            Iterator<NodeRef> iterator = nodeRefs.iterator();

//
//            while (iterator.hasNext()) {
//                NodeRef nodeRef = iterator.next();
//                result.put(entryBean.toJSON(nodeRef));
//            }

        } catch (Exception e) {
            e.printStackTrace();
            result.put(JSONUtils.getError(e));
            res.setStatus(400);
        }
        JSONUtils.write(webScriptWriter, result);
    }
}

// F.eks. curl -i -u admin:admin -X GET 'http://localhost:8080/alfresco/s/entry?siteShortName=retspsyk'
