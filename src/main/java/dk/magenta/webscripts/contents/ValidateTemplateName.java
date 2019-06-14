package dk.magenta.webscripts.contents;

import dk.magenta.model.DatabaseModel;
import dk.magenta.utils.JSONUtils;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PermissionService;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import javax.swing.text.html.parser.ContentModel;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

public class ValidateTemplateName extends AbstractWebScript {


    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    private PermissionService permissionService;

    public void setFileFolderService(FileFolderService fileFolderService) {
        this.fileFolderService = fileFolderService;
    }

    private FileFolderService fileFolderService;

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    private NodeService nodeService;

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

            NodeRef n = new NodeRef((String) json.get("nodeRef"));
            String templateType = (String) json.get("templateType");

            System.out.println("hvad er json");
            System.out.println(json);


            FileInfo newNode = fileFolderService.create(nodeService.getPrimaryParent(n).getParentRef(), "tmp", org.alfresco.model.ContentModel.TYPE_CONTENT);


            if (templateType.equals(DatabaseModel.PROP_TEMPLATE_DOC_SAMTYKKE)) {

                FileInfo node = fileFolderService.copy(n, nodeService.getPrimaryParent(n).getParentRef(),  DatabaseModel.PROP_TEMPLATE_DOC_SAMTYKKE_FILENAME);
                fileFolderService.delete(n);
                fileFolderService.delete(newNode.getNodeRef());

                permissionService.setPermission(node.getNodeRef(), DatabaseModel.GROUP_ALLOWEDTODELETE, PermissionService.DELETE_NODE, true);


            }
            else {

                FileInfo node = fileFolderService.copy(n, nodeService.getPrimaryParent(n).getParentRef(),  DatabaseModel.PROP_TEMPLATE_DOC_KENDELSE_FILENAME);
                fileFolderService.delete(n);
                fileFolderService.delete(newNode.getNodeRef());

                permissionService.setPermission(node.getNodeRef(), DatabaseModel.GROUP_ALLOWEDTODELETE, PermissionService.DELETE_NODE, true);

            }

            result = JSONUtils.getSuccess();
            JSONUtils.write(webScriptWriter, result);

        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        catch (org.alfresco.service.cmr.model.FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
