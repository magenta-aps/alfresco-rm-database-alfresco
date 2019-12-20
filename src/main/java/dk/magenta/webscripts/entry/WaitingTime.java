package dk.magenta.webscripts.entry;

import dk.magenta.beans.DatabaseBean;
import dk.magenta.beans.EntryBean;
import dk.magenta.model.DatabaseModel;
import dk.magenta.utils.JSONUtils;
import dk.magenta.utils.QueryUtils;
import net.sf.cglib.core.Local;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.JSONArray;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.io.Writer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class WaitingTime extends AbstractWebScript {

    private EntryBean entryBean;
    private DatabaseBean databaseBean;

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    private NodeService nodeService;

    public void setLockService(LockService lockService) {
        this.lockService = lockService;
    }

    private LockService lockService;

    public void setEntryBean(EntryBean entryBean) {
        this.entryBean = entryBean;
    }
    public void setDatabaseBean(DatabaseBean databaseBean) {
        this.databaseBean = databaseBean;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
        res.setContentEncoding("UTF-8");
        Writer webScriptWriter = res.getWriter();
        JSONObject result = new JSONObject();

        try {
            String siteShortName = templateArgs.get("siteShortName");

            String method = req.getParameter("method");
            String entryValue = req.getParameter("entryValue");

            String type = databaseBean.getType(siteShortName);
            String query = QueryUtils.getEntryQuery(siteShortName, type, entryValue);

            NodeRef nodeRef = entryBean.getEntry(query);

            if (method != null) {
                switch (method) {
                    case "calculatePassive":
                        result =  entryBean.calculatePassive(nodeRef);
                        break;

                    case "calculateActive":
                        result = entryBean.calculateActive(nodeRef);
                        break;

                    case "calculateTotal":
                        result = entryBean.calculateTotal(nodeRef);
                        break;

                    case "calculateALLDeclarations":
                        result = this.calculateAllDeclarations(siteShortName);
                        break;

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            result = JSONUtils.getError(e);
            res.setStatus(400);
        }
        JSONUtils.write(webScriptWriter, result);
    }

    private JSONObject calculateAllDeclarations(String siteShortName) {

        Set<NodeRef> nodeRefs = null;
        try {
            nodeRefs = entryBean.getEntries(siteShortName);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Iterator<NodeRef> iterator = nodeRefs.iterator();
        int i = 1;

        while (iterator.hasNext()) {
            NodeRef nodeRef = iterator.next();

            entryBean.calculatePassive(nodeRef);

            entryBean.calculateActive(nodeRef);

            entryBean.calculateTotal(nodeRef);
        }

        return JSONUtils.getSuccess();
    }


}

// F.eks. curl -i -u admin:admin -X GET 'http://localhost:8080/alfresco/s/database/retspsyk/entry/445644-4545-4564-8848-1849155'