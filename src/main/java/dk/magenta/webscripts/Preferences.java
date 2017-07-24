package dk.magenta.webscripts;

import dk.magenta.utils.JSONUtils;
import org.alfresco.service.cmr.preference.PreferenceService;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.extensions.webscripts.*;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public class Preferences extends AbstractWebScript {

    final Logger logger = LoggerFactory.getLogger(Preferences.class);

    public void setPreferenceService(PreferenceService preferenceService) {
        this.preferenceService = preferenceService;
    }

    private PreferenceService preferenceService;

    @Override
    public void execute(WebScriptRequest webScriptRequest, WebScriptResponse webScriptResponse) throws IOException {

        JSONArray result;
        try {
            Map<String, String> params = JSONUtils.parseParameters(webScriptRequest.getURL());

            String username = params.get("username");
            String pf = params.get("pf");
            Map<String, Serializable> preferences = JSONUtils.getPreferences(preferenceService, username, pf);
            result = JSONUtils.getJSONReturnArray(preferences);

        } catch (Exception e) {
            e.printStackTrace();
            result = JSONUtils.getJSONError(e);
            webScriptResponse.setStatus(400);
        }

        JSONUtils.write(webScriptResponse.getWriter(), result);

    }
}
