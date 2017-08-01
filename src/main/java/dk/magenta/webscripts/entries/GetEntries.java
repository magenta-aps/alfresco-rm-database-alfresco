package dk.magenta.webscripts.entries;

import dk.magenta.beans.EntryBean;
import dk.magenta.utils.JSONUtils;
import dk.magenta.utils.QueryUtils;
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
    public void execute(WebScriptRequest webScriptRequest, WebScriptResponse webScriptResponse) throws IOException {
        Map<String, String> params = JSONUtils.parseParameters(webScriptRequest.getURL());

        webScriptResponse.setContentEncoding("UTF-8");
        Writer webScriptWriter = webScriptResponse.getWriter();
        JSONArray result = new JSONArray();

        try {
            String type = params.get("type");
            String entryValue = params.get("entryValue");

            String query = QueryUtils.getEntryQuery(type, entryValue);
            Set<NodeRef> nodeRefs = entryBean.getEntries(query);
            Iterator<NodeRef> iterator = nodeRefs.iterator();

            if (!iterator.hasNext())
                throw new Exception("Entry with the type (" + type + ")" +
                        " and the entry value (" + entryValue + ") does not exist.");

            while (iterator.hasNext()) {
                NodeRef nodeRef = iterator.next();
                result.put(entryBean.toJSON(nodeRef));
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.put(JSONUtils.getError(e));
            webScriptResponse.setStatus(400);
        }
        JSONUtils.write(webScriptWriter, result);
    }
}

// F.eks. curl -i -u admin:admin -X GET 'http://localhost:8080/alfresco/s/entry?type=forensicPsychiatryDeclaration&entryValue=658'