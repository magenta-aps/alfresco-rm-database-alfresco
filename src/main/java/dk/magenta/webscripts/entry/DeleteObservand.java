package dk.magenta.webscripts.entry;

import dk.magenta.beans.EntryBean;
import dk.magenta.model.DatabaseModel;
import dk.magenta.utils.JSONUtils;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

public class DeleteObservand extends AbstractWebScript {

    private EntryBean entryBean;

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    private NodeService nodeService;
    private SiteService siteService;

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
//            NodeRef nodeRef = entryBean.getNodeRef(uuid);

            // slet sag

            // tilføj frigivet sagsnummer til jsonlisten i PROP_FREE_CASENUMBERS på docLibRef

              // læs
              NodeRef docLibRef = siteService.getContainer("retspsyk", SiteService.DOCUMENT_LIBRARY);


//              String json = (String) nodeService.getProperty(docLibRef, DatabaseModel.PROP_BUA_COUNTER);

            System.out.println("hvad er json først gang?");
            System.out.println(nodeService.getProperty(docLibRef, DatabaseModel.PROP_BUA_COUNTER));
            System.out.println(nodeService.getProperty(docLibRef, DatabaseModel.PROP_BUA_COUNTER));

//              JSONArray list = new JSONArray(json);



//            entryBean.deleteEntry(nodeRef);

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
