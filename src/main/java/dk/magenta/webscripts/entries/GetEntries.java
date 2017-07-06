package dk.magenta.webscripts.entries;

import dk.magenta.beans.EntryBean;
import dk.magenta.utils.JSONUtils;
import dk.magenta.utils.QueryUtils;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.json.JSONObject;
import org.json.simple.JSONArray;
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
    public void execute(WebScriptRequest webScriptRequest, WebScriptResponse webScriptResponse) throws IOException {
        Map<String, String> params = JSONUtils.parseParameters(webScriptRequest.getURL());

        webScriptResponse.setContentEncoding("UTF-8");
        Writer webScriptWriter = webScriptResponse.getWriter();
        JSONArray result = new JSONArray();

        try {
            String type = params.get("type");
            String entryKey = params.get("entryKey");
            String entryValue = params.get("entryValue");

            String query = QueryUtils.getEntryQuery(type, entryKey, entryValue);
            System.out.println("hvad er query");
            System.out.println(query);
            Set<NodeRef> nodeRefs = entryBean.getEntries(query);
            Iterator<NodeRef> iterator = nodeRefs.iterator();

            if (!iterator.hasNext())
                result.add(JSONUtils.getObject("error", "Entry with the type (" + type + ")" +
                        " and the variable (" + entryKey + " = " + entryValue + ") does not exist."));

            while (iterator.hasNext()) {
                NodeRef nodeRef = iterator.next();
                result.add(entryBean.toJSON(nodeRef));
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.add(JSONUtils.getError(e));
            webScriptResponse.setStatus(400);
        }
        JSONUtils.write(webScriptWriter, result);
    }
}

// F.eks. curl -i -u admin:admin -X GET 'http://localhost:8080/alfresco/s/entry?type=forensicPsychiatryDeclaration&entryKey=caseNumber&entryValue=33'