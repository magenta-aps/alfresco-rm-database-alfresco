package dk.magenta.webscripts.propertyValues;

import dk.magenta.beans.PropertyValuesBean;
import dk.magenta.utils.JSONUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.io.Writer;

public class UpdatePropertyValues extends AbstractWebScript {

    private PropertyValuesBean propertyValuesBean;
    public void setPropertyValuesBean(PropertyValuesBean propertyValuesBean) {
        this.propertyValuesBean = propertyValuesBean;
    }

    @Override
    public void execute(WebScriptRequest webScriptRequest, WebScriptResponse webScriptResponse) throws IOException {
        webScriptResponse.setContentEncoding("UTF-8");
        Content c = webScriptRequest.getContent();
        Writer webScriptWriter = webScriptResponse.getWriter();
        JSONObject result;

        try {
            JSONObject json = new JSONObject(c.getContent());
            String siteShortName = JSONUtils.getString(json, "siteShortName");
            String property = JSONUtils.getString(json, "property");
            JSONArray values = JSONUtils.getArray(json, "values");

            propertyValuesBean.updatePropertyValues(siteShortName, property, values);
            result = JSONUtils.getSuccess();

        } catch (Exception e) {
            e.printStackTrace();
            result = JSONUtils.getError(e);
            webScriptResponse.setStatus(400);
        }
        JSONUtils.write(webScriptWriter, result);
    }
}

// F.eks. curl -i -u admin:admin -X PUT 'http://localhost:8080/alfresco/s/propertyValues'