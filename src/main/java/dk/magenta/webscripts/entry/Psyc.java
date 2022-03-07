package dk.magenta.webscripts.entry;

import dk.magenta.beans.PsycBean;
import dk.magenta.utils.JSONUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

public class Psyc extends AbstractWebScript {

    public void setPsycBean(PsycBean psycBean) {
        this.psycBean = psycBean;
    }
    private PsycBean psycBean;

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();

        res.setContentEncoding("UTF-8");
        Content c = req.getContent();
        JSONObject result = new JSONObject();


        JSONObject jsonProperties = null;

        JSONObject json = null;
        try {

            json = new JSONObject(c.getContent());
            jsonProperties = JSONUtils.getObject(json, "properties");
            String method = jsonProperties.getString("method");

            System.out.println("hvad er method");
            System.out.println(method);

            System.out.println("hvad er properties");
            System.out.println(jsonProperties);

            switch (method) {

                case "test":
//                    Map<QName, Serializable> properties = new HashMap<>();
//                    properties.put(DatabaseModel.PROP_PSYCDATA_INTERVIEWRATINGSCALES, "[\"solgaard\",\"lasssi\",\"gavl\"]");
//
//                    NodeRef n = new NodeRef("workspace://SpacesStore/5c11e08c-8064-494a-9074-3ff6d07ed81e");
//                    nodeService.addAspect(n, ASPECT_PSYCDATA, properties);
//
//                    psycBean.createDataForInterviewRating();

                    psycBean.createDataForInterviewRating();

                    System.out.println("output fra test");

                    break;

                case "total":
                    break;
            }
        } catch (JSONException e) {
            System.out.println("json exception");
            e.printStackTrace();
        }
        catch (Exception e) {
            System.out.println("this exception");
            System.out.println(e.toString());
            e.printStackTrace();
        }

        Writer webScriptWriter = res.getWriter();

        JSONUtils.write(webScriptWriter, result);

    }
}
