

package dk.magenta.webscripts.contents;

import dk.magenta.beans.ContentsBean;
import dk.magenta.beans.TransformBean;
import dk.magenta.model.DatabaseModel;
import dk.magenta.utils.JSONUtils;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteService;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.io.Writer;

import static dk.magenta.model.DatabaseModel.ASPECT_TMP;

public class TransformODT extends AbstractWebScript {

    public void setTransformBean(TransformBean transformBean) {
        this.transformBean = transformBean;
    }

    private TransformBean transformBean;

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    private SiteService siteService;

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    private NodeService nodeService;


    @Override
    public void execute(WebScriptRequest webScriptRequest, WebScriptResponse webScriptResponse) throws IOException {

        webScriptResponse.setContentEncoding("UTF-8");
        Content c = webScriptRequest.getContent();
        Writer webScriptWriter = webScriptResponse.getWriter();
        JSONObject result = new JSONObject();

        JSONObject json = null;

        try {
            json = new JSONObject(c.getContent());
            System.out.println("hvad er json");
            System.out.println(json);

            NodeRef n = new NodeRef("workspace://SpacesStore/" + ( String)json.get("nodeRef"));
            NodeRef tmpFolder = siteService.getContainer("retspsyk", DatabaseModel.PROP_TMP);

            NodeRef tmpNode = transformBean.transformODTtoPDF(n, tmpFolder);
            nodeService.addAspect(tmpNode,ASPECT_TMP,null);

            result.put("item", tmpNode.getId());
            JSONUtils.write(webScriptWriter, result);

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
