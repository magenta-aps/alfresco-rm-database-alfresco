package dk.magenta.webscripts.entry;

import dk.magenta.beans.EntryBean;
import dk.magenta.model.DatabaseModel;
import dk.magenta.utils.JSONUtils;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import javax.xml.crypto.Data;
import javax.xml.soap.Node;
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

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    private PersonService personService;


    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    private AuthenticationService authenticationService;

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

            // service not implemented for bua
            boolean bua = false;
            String currentUser = authenticationService.getCurrentUserName();

            NodeRef person = personService.getPerson(currentUser);
            bua = nodeService.hasAspect(person, DatabaseModel.ASPECT_BUA);

            switch (method) {
                case "delete":


                        if (!bua) {

                            System.out.println("sletter nu");

                            String cpr = json.getString("cpr");
                            String sagsnr = json.getString("caseNumber");

                            String query = "@rm\\:caseNumber:\"" + sagsnr + "\" AND ";
                            query = query + "@rm\\:cprNumber:\"" + cpr + "\"";

                            System.out.println("hvad er query");
                            System.out.println(query);

                            NodeRef declaration = entryBean.getEntry(query);

                            // udkommenteret. Lone og Kristina besluttede ikke at genbruge numre på statusmødet d. 16. maj. 2022
//                            int sagsnummer = (int) nodeService.getProperty(declaration, DatabaseModel.PROP_CASE_NUMBER);
//                            addCaseNumberToReuseList(sagsnummer);

                            lockService.unlock(declaration);
                            entryBean.deleteEntry(declaration);

                            result = JSONUtils.getSuccess();
                            JSONUtils.write(webScriptWriter, result);
                        }

                    break;

                case "confirm":

                    if (!bua) {
                        String cpr = json.getString("cpr");
                        String sagsnr = json.getString("caseNumber");

                        String query = "@rm\\:caseNumber:\"" + sagsnr + "\" AND ";
                        query = query + "@rm\\:cprNumber:\"" + cpr + "\"";

                        System.out.println("hvad er query");
                        System.out.println(query);

                        NodeRef declaration = entryBean.getEntry(query);

                        System.out.println("har du fundet en declaration");
                        System.out.println(declaration != null);

                        result = new JSONObject();
                        if (declaration != null) {

                            String navn = (String)nodeService.getProperty(declaration, DatabaseModel.PROP_FULL_NAME);

                            result.put("found", declaration != null);
                            result.put("navn", navn);
                            result.put("nodeRef", declaration);

                            JSONUtils.write(webScriptWriter, result);
                        }
                        else {

                            result.put("found", declaration != null);
                            JSONUtils.write(webScriptWriter, result);

                        }
                    }



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




        NodeRef docLibRef = siteService.getContainer("retspsyk", SiteService.DOCUMENT_LIBRARY);

        System.out.println("hvad er nodeService.getProperty(docLibRef, DatabaseModel.PROP_FREE_CASENUMBERS");
        System.out.println(nodeService.getProperty(docLibRef, DatabaseModel.PROP_FREE_CASENUMBERS));
        System.out.println(nodeService.getProperty(docLibRef, DatabaseModel.PROP_FREE_CASENUMBERS) != null);

        if (nodeService.getProperty(docLibRef, DatabaseModel.PROP_FREE_CASENUMBERS) != null) {
            String caseNumbers = (String) nodeService.getProperty(docLibRef, DatabaseModel.PROP_FREE_CASENUMBERS);

            System.out.println("hvad er indlæst caseNumbers");
            System.out.println(caseNumbers);

            List<String> list = null;
            if (!caseNumbers.equals("")) {
                list = new ArrayList<String>(Arrays.asList(caseNumbers.split(",")));
            }
            else {
                list = new ArrayList();
            }

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
