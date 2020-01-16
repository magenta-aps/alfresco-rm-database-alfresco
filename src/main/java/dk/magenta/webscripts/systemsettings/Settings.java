package dk.magenta.webscripts.systemsettings;

import dk.magenta.beans.SettingsBean;
import dk.magenta.utils.JSONUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.util.Map;

public class Settings extends AbstractWebScript {

    final Logger logger = LoggerFactory.getLogger(Settings.class);

    public void setSettingsBean(SettingsBean settingstBean) {
        this.settingsBean = settingstBean;
    }

    private SettingsBean settingsBean;

    @Override
    public void execute(WebScriptRequest webScriptRequest, WebScriptResponse webScriptResponse) throws IOException   {

        JSONObject result = new JSONObject();

        try {

            String defaultText = settingsBean.getDefaultMailText();

            result.put("text", defaultText);



        } catch (Exception e) {
            e.printStackTrace();
            result = JSONUtils.getError(e);
            webScriptResponse.setStatus(400);
        }

        JSONUtils.write(webScriptResponse.getWriter(), result);



    }
}
