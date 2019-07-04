package dk.magenta.webscripts.contents;

import dk.magenta.beans.ContentsBean;
import dk.magenta.beans.EntryBean;
import dk.magenta.beans.MailBean;
import dk.magenta.model.DatabaseModel;
import dk.magenta.utils.JSONUtils;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class MoveContent extends AbstractWebScript {

    public void setContentsBean(ContentsBean contentsBean) {
        this.contentsBean = contentsBean;
    }

    private ContentsBean contentsBean;


    @Override
    public void execute(WebScriptRequest webScriptRequest, WebScriptResponse webScriptResponse) throws IOException {

        webScriptResponse.setContentEncoding("UTF-8");
        Content c = webScriptRequest.getContent();
        Writer webScriptWriter = webScriptResponse.getWriter();
        JSONObject result;


        JSONObject json = null;
        try {

            System.out.println("the json" + c.getContent());
            json = new JSONObject(c.getContent());

            JSONArray jsonNodeRefs = JSONUtils.getArray(json, "nodeRefs");
            NodeRef[] nodeRefs = new NodeRef[jsonNodeRefs.length()];
            for (int i=0; i<jsonNodeRefs.length(); i++) {
                String nodeRefStr = jsonNodeRefs.getString(i);
                NodeRef nodeRef = new NodeRef(nodeRefStr);
                nodeRefs[i] = nodeRef;
            }

            String destNode = (String)json.get("destNode");
            System.out.println("destNode: " + destNode);


            try {
                contentsBean.moveContent(nodeRefs, new NodeRef(destNode));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            result = JSONUtils.getSuccess();
            JSONUtils.write(webScriptWriter, result);


        } catch (JSONException e) {
            e.printStackTrace();
            result = JSONUtils.getError(e);
            webScriptResponse.setStatus(400);
            JSONUtils.write(webScriptWriter, result);
        }
    }
}
