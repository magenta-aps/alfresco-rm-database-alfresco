

package dk.magenta.webscripts.contents;

import dk.magenta.beans.ContentsBean;
import dk.magenta.utils.JSONUtils;
import org.alfresco.service.cmr.repository.NodeRef;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.io.Writer;

public class TransformPDFtoJPG extends AbstractWebScript {

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
            json = new JSONObject(c.getContent());


            NodeRef n = new NodeRef(( String)json.get("nodeRef"));
            System.out.println("hvad er n");
            System.out.println(n);


//            The code is something like this:
//
//            ContentReader reader = contentService.getReader(node, ContentModel.PROP_CONTENT);
//
//            ContentWriter tiffWriter = contentService.getWriter(nodeTiff, ContentModel.PROP_CONTENT, true);
//
//            ContentTransformer transform = contentService.getTransformer(MimetypeMap.MIMETYPE_PDF, MimetypeMap.MIMETYPE_IMAGE_TIFF);
//
//            transform.transform(reader, tiffWriter);


            contentsBean.transformPDFtoJPG(n);



            result = JSONUtils.getSuccess();
            JSONUtils.write(webScriptWriter, result);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
