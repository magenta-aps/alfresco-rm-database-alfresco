package dk.magenta.webscripts.entry;

import dk.magenta.beans.EntryBean;
import dk.magenta.model.DatabaseModel;
import dk.magenta.utils.JSONUtils;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    public void setLockService(LockService lockService) {
        this.lockService = lockService;
    }

    private LockService lockService;

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

            Content c = req.getContent();
            JSONObject json = null;
            json = new JSONObject(c.getContent());

            String method = json.getString("method");

            switch (method) {
                case "delete":

                    String uuid = json.getString("uuid");

//                    String uuid = templateArgs.get("uuid");
                    NodeRef nodeRef = entryBean.getNodeRef(uuid);

                    System.out.println("hvad er nodeRef");
                    System.out.println(nodeRef);

                    // slet sag, evt. lås den op først.

                    // tilføj frigivet sagsnummer til jsonlisten i PROP_FREE_CASENUMBERS på docLibRef

                    int sagsnummer = (int)nodeService.getProperty(nodeRef, DatabaseModel.PROP_CASE_NUMBER);
                    addCaseNumberToReuseList(sagsnummer);

                    lockService.unlock(nodeRef);
                    entryBean.deleteEntry(nodeRef);

                    result = JSONUtils.getSuccess();
                    JSONUtils.write(webScriptWriter, result);

                    break;

                case "confirm":

                    String cpr = json.getString("cpr");
                    String sagsnr = json.getString("caseNumber");

                    String query = "@rm\\:caseNumber:\"" + sagsnr + "\" AND ";
                    query = query + "@rm\\:cprNumber:\"" + cpr + "\"";

                    System.out.println("hvad er query");
                    System.out.println(query);

                    NodeRef declaration = entryBean.getEntry(query);

                    System.out.println("har du fundet en declaration");



                    break;
            }




        } catch (Exception e) {
            e.printStackTrace();
            result = JSONUtils.getError(e);
            res.setStatus(400);
        }
//        JSONUtils.write(webScriptWriter, result);
    }

    private void addCaseNumberToReuseList(int caseNumber) {

        // get property PROP_FREE_CASENUMBERS
        NodeRef docLibRef = siteService.getContainer("retspsyk", SiteService.DOCUMENT_LIBRARY);

        System.out.println("hvad er nodeService.getProperty(docLibRef, DatabaseModel.PROP_FREE_CASENUMBERS");
        System.out.println(nodeService.getProperty(docLibRef, DatabaseModel.PROP_FREE_CASENUMBERS));
        System.out.println(nodeService.getProperty(docLibRef, DatabaseModel.PROP_FREE_CASENUMBERS) != null);

        if (nodeService.getProperty(docLibRef, DatabaseModel.PROP_FREE_CASENUMBERS) != null ) {
            String caseNumbers = (String) nodeService.getProperty(docLibRef, DatabaseModel.PROP_FREE_CASENUMBERS);

            System.out.println("hvad er indlæst caseNumbers");
            System.out.println(caseNumbers);


            List<String> list = new ArrayList<String>(Arrays.asList(caseNumbers.split(",")));

            list.add(String.valueOf(caseNumber));

            nodeService.setProperty(docLibRef, DatabaseModel.PROP_FREE_CASENUMBERS, String.join(", ", list));
        }
        else {
            List<String> list = new ArrayList<String>();
            list.add(String.valueOf(caseNumber));

            nodeService.setProperty(docLibRef, DatabaseModel.PROP_FREE_CASENUMBERS, String.join(", ", list));
        }

        }

}

// F.eks. curl -i -u admin:admin -X DELETE 'http://localhost:8080/alfresco/s/entry/445644-4545-4564-8848-1849155'
