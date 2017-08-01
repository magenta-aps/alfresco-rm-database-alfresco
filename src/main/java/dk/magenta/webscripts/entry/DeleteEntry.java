package dk.magenta.webscripts.entry;

import dk.magenta.beans.EntryBean;
import dk.magenta.utils.JSONUtils;
import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

public class DeleteEntry extends AbstractWebScript {

    private EntryBean entryBean;
    public void setEntryBean(EntryBean entryBean) {
        this.entryBean = entryBean;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();

        res.setContentEncoding("UTF-8");
        Writer webScriptWriter = res.getWriter();
        JSONObject result;

        try {
            String uuid = templateArgs.get("uuid");
            NodeRef nodeRef = entryBean.getEntry(uuid);
            entryBean.deleteEntry(nodeRef);
            result = JSONUtils.getSuccess();

        } catch (Exception e) {
            e.printStackTrace();
            result = JSONUtils.getError(e);
            res.setStatus(400);
        }
        JSONUtils.write(webScriptWriter, result);
    }
}

// F.eks. curl -i -u admin:admin -X DELETE 'http://localhost:8080/alfresco/s/entry/445644-4545-4564-8848-1849155'