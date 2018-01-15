package dk.magenta.utils;

import dk.magenta.model.DatabaseModel;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;

public class QueryUtils {

    public static String getSiteQuery (String siteShortName) {
        return  "PATH:\"/app:company_home/st:sites/cm:" + siteShortName + "/cm:documentLibrary/*/*/*/*\"";
    }

    public static String getTypeQuery (String type) {
        return  "TYPE:\"" + DatabaseModel.RM_MODEL_PREFIX +":" + type + "\"";
    }

    public static String getParameterQuery (String paramKey, String paramValue) {
        return "@" + DatabaseModel.RM_MODEL_PREFIX + "\\:" + paramKey + ":\"" + paramValue + "\"";
    }

    public static String getEntryQuery (String siteShortName, String type, String entryValue) throws JSONException {
        String entryKey = TypeUtils.types.get(type).getString(DatabaseModel.ENTRY_KEY);
        String query = getSiteQuery(siteShortName) + " AND " + getTypeQuery(type);

        if(entryValue != null)
            query += " AND " + getParameterQuery(entryKey, entryValue);

        return query;
    }

    public static String getKeyValueQuery (String siteShortName, String type, JSONArray keyValues) throws JSONException {

        String query = getSiteQuery(siteShortName) + " AND " + getTypeQuery(type);


        for (int i = 0 ; i < keyValues.length(); i++) {
            JSONObject obj = keyValues.getJSONObject(i);
            String key = obj.getString("key");
            String value = obj.getString("value");
            System.out.println(key + ":" + value);
            query += " AND " + getParameterQuery(key, value);
        }

//        if(entryValue != null)
//            query += " AND " + getParameterQuery(entryKey, entryValue);

        return query;
    }
}
