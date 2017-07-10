package dk.magenta.utils;

import dk.magenta.model.DatabaseModel;
import org.activiti.engine.impl.bpmn.data.Data;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.preference.PreferenceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.JSONArray;

import java.io.Serializable;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JSONUtils {

    public static Map<String, String> parseParameters(String url) {
        // Do our own parsing to get the query string since java.net.URI can't
        // handle some URIs
        int queryStringStart = url.indexOf('?');
        String queryString = "";
        if (queryStringStart != -1) {
            queryString = url.substring(queryStringStart+1);
        }
        Map<String, String> parameters = URLEncodedUtils
                .parse(queryString, Charset.forName("UTF-8"))
                .stream()
                .collect(
                        Collectors.groupingBy(
                                NameValuePair::getName,
                                Collectors.collectingAndThen(Collectors.toList(), JSONUtils::paramValuesToString)));
        return parameters;
    }

    private static String paramValuesToString(List<NameValuePair> paramValues) {
        if (paramValues.size() == 1) {
            return paramValues.get(0).getValue();
        }
        List<String> values = paramValues.stream().map(NameValuePair::getValue).collect(Collectors.toList());
        return "[" + StringUtils.join(values, ",") + "]";
    }

    public static String getString(JSONObject json, String parameter) throws JSONException {
        if (!json.has(parameter) || json.getString(parameter).length() == 0)
        {
            return "";
        }
        return json.getString(parameter);
    }

    public static JSONObject getObject(JSONObject json, String parameter) throws JSONException {
        if (!json.has(parameter) || json.getJSONObject(parameter).length() == 0)
        {
            return null;
        }
        return json.getJSONObject(parameter);
    }

    public static JSONObject getObject(Map<QName, Serializable> map) {
        JSONObject result = new JSONObject();
        try {
            for (Map.Entry<QName, Serializable> pair : map.entrySet()) {
                String key = pair.getKey().getLocalName();
                Serializable value = pair.getValue();
                if (value != null && !"".equals(value))
                    result.put(key, pair.getValue().toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static JSONObject getObject (String key, String value) {
        JSONObject json = new JSONObject();
        try {
            json.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public static JSONObject getSuccess () {
        return getObject("status", "success");
    }

    public static JSONObject getSuccess (String caseNumber) throws JSONException {
        JSONObject result = getObject("status", "success");
        result.put("caseNumber", caseNumber);
        return result;
    }

    public static JSONObject getError (Exception e) {
        return getObject("error", e.getStackTrace()[0].toString());
    }

    public static Map<QName, Serializable> getMap(JSONObject json) throws JSONException {
        Map<QName, Serializable> map = new HashMap<>();
        Iterator keys = json.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            QName qName = QName.createQName(DatabaseModel.RM_MODEL_URI, key);
            map.put(qName, json.getString(key));
        }
        return map;
    }

    public static void write (Writer writer, JSONObject result) {
        try {
            result.write(writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void write (Writer writer, JSONArray result) {
        try {
            result.writeJSONString(writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static JSONArray getJSONArray(List<String> list) {
        JSONArray result = new JSONArray();

        Iterator i = list.iterator();

        while (i.hasNext()) {
            JSONObject o = new JSONObject();
            try {
                o.put("name", i.next());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            result.add(o);
        }

        return result;
    }

    public static Map<String, Serializable> getPreferences(PreferenceService preferenceService, String userName, String filter) {
        AuthenticationUtil.pushAuthentication();
        try {
            AuthenticationUtil.setRunAsUserSystem();
            // ...code to be run as Admin...
            return preferenceService.getPreferences(userName, filter);
        }
        finally {
            AuthenticationUtil.popAuthentication();
        }
    }

    public static JSONArray getJSONReturnArray(Map<String, Serializable> map) {
        JSONObject return_json = new JSONObject();
        JSONArray result = new JSONArray();
        try {
            for (Map.Entry<String, Serializable> pair : map.entrySet())
                return_json.put(pair.getKey(), pair.getValue().toString());
            result.add(return_json);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static JSONArray getJSONError (Exception e) {
        Map<String, Serializable> map = new HashMap<>();
        map.put("error", e.getStackTrace()[0].toString());
        return getJSONReturnArray(map);
    }
}
