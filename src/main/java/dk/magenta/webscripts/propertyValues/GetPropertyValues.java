package dk.magenta.webscripts.propertyValues;

import dk.magenta.beans.EntryBean;
import dk.magenta.beans.PropertyValuesBean;
import dk.magenta.utils.JSONUtils;
import dk.magenta.utils.QueryUtils;
import org.alfresco.service.cmr.repository.NodeRef;
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

public class GetPropertyValues extends AbstractWebScript {

    private PropertyValuesBean propertyValuesBean;
    public void setPropertyValuesBean(PropertyValuesBean propertyValuesBean) {
        this.propertyValuesBean = propertyValuesBean;
    }

    @Override
    public void execute(WebScriptRequest webScriptRequest, WebScriptResponse webScriptResponse) throws IOException {
        Map<String, String> params = JSONUtils.parseParameters(webScriptRequest.getURL());

        webScriptResponse.setContentEncoding("UTF-8");
        Writer webScriptWriter = webScriptResponse.getWriter();
        JSONObject result;

        try {
            String siteShortName = params.get("siteShortName");
            result = propertyValuesBean.getPropertyValues(siteShortName);

        } catch (Exception e) {
            e.printStackTrace();
            result = JSONUtils.getError(e);
            webScriptResponse.setStatus(400);
        }
        JSONUtils.write(webScriptWriter, result);
    }
}




// F.eks. curl -i -u admin:admin -X GET 'http://localhost:8080/alfresco/s/propertyValues?siteShortName=retspsyk'