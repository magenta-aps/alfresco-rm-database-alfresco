package dk.magenta.webscripts.contents;

import dk.magenta.beans.MailBean;
import dk.magenta.model.DatabaseModel;
import dk.magenta.utils.JSONUtils;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AddPermission extends AbstractWebScript {


    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    private PermissionService permissionService;

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    private SearchService searchService;

    @Override
    public void execute(WebScriptRequest webScriptRequest, WebScriptResponse webScriptResponse) throws IOException {

        webScriptResponse.setContentEncoding("UTF-8");
        Content c = webScriptRequest.getContent();
        Writer webScriptWriter = webScriptResponse.getWriter();
        JSONObject result;

        JSONObject json = null;

        try {
            json = new JSONObject(c.getContent());



            if (json.has("update")) {

                ResultSet resultSet = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "lucene", " PATH:\"/app:company_home/st:sites/cm:retspsyk/cm:documentLibrary//*\" AND TYPE:\"cm:content\" ");

                List<NodeRef> nodeRefs = resultSet.getNodeRefs();

                Iterator i = nodeRefs.iterator();

                while (i.hasNext()) {
                    NodeRef node = (NodeRef)i.next();
                    System.out.println(node);
                    permissionService.setPermission(node, DatabaseModel.GROUP_ALLOWEDTODELETE, PermissionService.DELETE_NODE, true);
                }
            }
            else {
                NodeRef n = new NodeRef(( String)json.get("nodeRef"));
//                permissionService.setPermission(n, DatabaseModel.GROUP_ALLOWEDTODELETE, PermissionService.DELETE_NODE, true);
            }

            result = JSONUtils.getSuccess();
            JSONUtils.write(webScriptWriter, result);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
