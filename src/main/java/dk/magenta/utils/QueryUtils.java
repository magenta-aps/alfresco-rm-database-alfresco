package dk.magenta.utils;

import dk.magenta.model.DatabaseModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.JSONArray;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class QueryUtils {

    public static String getTypeQuery (String type) {
        return  "TYPE:\"" + DatabaseModel.RM_MODEL_PREFIX +":" + type + "\"";
    }

    public static String getParameterQuery (String paramKey, String paramValue) {
        return "@" + DatabaseModel.RM_MODEL_PREFIX + "\\:" + paramKey + ":\"" + paramValue + "\"";
    }

    public static String getEntryQuery (String type, String entryKey, String entryValue) {

        System.out.println("hvad er entrykey");
        System.out.println(entryKey);

        if (entryKey == null) {
            return getTypeQuery(type);
        } else {

            return getTypeQuery(type) + " AND " + getParameterQuery(entryKey, entryValue);

        }
    }

    public static JSONArray getJSONError (Exception e) {
        Map<String, Serializable> map = new HashMap<>();
        map.put("error", e.getStackTrace()[0].toString());
        return getJSONReturnArray(map);
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
}
