

package dk.magenta.webscripts.contents;

import dk.magenta.beans.MailBean;
import dk.magenta.model.DatabaseModel;
import dk.magenta.utils.JSONUtils;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import javax.swing.text.html.parser.ContentModel;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AddPermission extends AbstractWebScript {


    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    private SiteService siteService;

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    private NodeService nodeService;

    private PermissionService permissionService;

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    private SearchService searchService;



    private void rekur(NodeRef child) {

        List<ChildAssociationRef> children = nodeService.getChildAssocs(child);

        if (children.size() == 0) {

            if ((nodeService.getType(child).equals(org.alfresco.model.ContentModel.TYPE_CONTENT))) {

                permissionService.setPermission(child, DatabaseModel.GROUP_ALLOWEDTODELETE, PermissionService.DELETE_NODE, true);
            }

        }
        else {
            for (int i = 0; i <= children.size()-1;i++) {
                NodeRef subChild = children.get(i).getChildRef();

                // need a check here, as the child might be a thumbnail
                if ((nodeService.getType(child).equals(org.alfresco.model.ContentModel.TYPE_CONTENT))) {
                    permissionService.setPermission(child, DatabaseModel.GROUP_ALLOWEDTODELETE, PermissionService.DELETE_NODE, true);
                }

                rekur(subChild);
            }
        }
    }

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


                SiteInfo siteInfo = siteService.getSite("retspsyk");

                // Get the documentLibrary of the site.
                NodeRef docLib = siteService.getContainer(siteInfo.getShortName(), "documentlibrary");


                this.rekur(docLib);






                // hovednode for erklæringer

                // rekursiv gennemløb og tilføj permission -

                // print antal dokumenter som manglede den, alternativt antal i alt


                ResultSet resultSet = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "lucene", " PATH:\"/app:company_home/st:sites/cm:retspsyk/cm:documentLibrary//*\" AND TYPE:\"cm:content\" ");

                List<NodeRef> nodeRefs = resultSet.getNodeRefs();

                Iterator i = nodeRefs.iterator();

                while (i.hasNext()) {

                    NodeRef node = (NodeRef)i.next();

                    permissionService.setPermission(node, DatabaseModel.GROUP_ALLOWEDTODELETE, PermissionService.DELETE_NODE, true);
                }
            }
            else {

                NodeRef n = new NodeRef(( String)json.get("nodeRef"));
                permissionService.setPermission(n, DatabaseModel.GROUP_ALLOWEDTODELETE, PermissionService.DELETE_NODE, true);
            }

            result = JSONUtils.getSuccess();
            JSONUtils.write(webScriptWriter, result);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
