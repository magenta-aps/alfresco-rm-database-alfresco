package dk.magenta.utils;

import dk.magenta.model.DatabaseModel;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.Date;

public class QueryUtils {

    public static String getSiteQuery (String siteShortName) {
        return  "PATH:\"/app:company_home/st:sites/cm:" + siteShortName + "/cm:documentLibrary/*/*/*/*\"";
    }

    public static String getTypeQuery (String type) {
        return  "TYPE:\"" + DatabaseModel.RM_MODEL_PREFIX +":" + type + "\"";
    }

    public static String getParametersQueryNullValue (String paramKey, boolean not) {

        if (not) {
            return "!@" + DatabaseModel.RM_MODEL_PREFIX + "\\:" + paramKey + ":\"" + "null" + "\"";
        }
        else {
            return "@" + DatabaseModel.RM_MODEL_PREFIX + "\\:" + paramKey + ":\"" + "null" + "\"";
        }
    }

    public static String getParameterQuery (String paramKey, String paramValue, boolean not) {

        if (not) {
            return "!@" + DatabaseModel.RM_MODEL_PREFIX + "\\:" + paramKey + ":\"" + paramValue + "\"";
        }
        else {
            return "@" + DatabaseModel.RM_MODEL_PREFIX + "\\:" + paramKey + ":\"" + paramValue + "\"";
        }
    }

    public static String getParametersQuery (String paramKey, String paramValue, boolean not) {

        if (not) {
            if (paramValue.contains("[")) {
                return "!@" + DatabaseModel.RM_MODEL_PREFIX + "\\:" + paramKey + ":" + paramValue;
            }
            else {
                return "!@" + DatabaseModel.RM_MODEL_PREFIX + "\\:" + paramKey + ":\"" + paramValue + "\"";
            }
        }
        else {
            if (paramValue.contains("[")) {
                return "@" + DatabaseModel.RM_MODEL_PREFIX + "\\:" + paramKey + ":" + paramValue;
            }
            else {
                return "@" + DatabaseModel.RM_MODEL_PREFIX + "\\:" + paramKey + ":\"" + paramValue + "\"";
            }

        }
    }




    public static String getEntryQuery (String siteShortName, String type, String entryValue) throws JSONException {
        String entryKey = TypeUtils.types.get(type).getString(DatabaseModel.ENTRY_KEY);
        String query = getSiteQuery(siteShortName) + " AND " + getTypeQuery(type);

        if(entryValue != null)
            query += " AND " + getParameterQuery(entryKey, entryValue, false);

        return query;
    }


    public static String mapWaitingType(String type) {

        if (type.equals("active")) {
            return "waiting_active";
        }
        else if (type.equals("passive")) {
            return "waiting_passive";
        }
        else  {
            return "waiting_total";
        }
    }

    public static String dateRangeQuery(String from, String to) {
        return "[" + "\"" + from + "\"" + " TO "+ "\"" + to + "\"" + "]";
    }

    public static String waitingQuery(int days, String operator) {

        if (operator.equals("over")) {
            return "[" + days + " TO MAX]";
        }
        else if (operator.equals("under")) {
            return "[MIN TO " + days + " ]";
        }
        else {
            return String.valueOf(days);
        }
    }

    public static String getKeyValueQuery (String siteShortName, String type, JSONArray keyValues) throws JSONException {

        String query = getSiteQuery(siteShortName) + " AND " + getTypeQuery(type);

        for (int i = 0 ; i < keyValues.length(); i++) {
            JSONObject obj = keyValues.getJSONObject(i);
            String key = obj.getString("key");
            String value = obj.getString("value");
            String include = obj.getString("include");

            if (include.equals("false")) {
                query += " AND " + getParametersQuery(key, value, true);

            } else {
//                System.out.println("hvad er tilføjet: ");
//                System.out.println(getParametersQuery(key, value, false));
                query += " AND " + getParametersQuery(key, value, false);
            }

//            System.out.println(key + ":" + value);
        }
        return query;
    }

    public static String getKeyValueORQuery (String siteShortName, String type, JSONArray keyValues) throws JSONException {

        String query = getSiteQuery(siteShortName) + " AND " + getTypeQuery(type) + " AND ";

        for (int i = 0 ; i < keyValues.length(); i++) {
            JSONObject obj = keyValues.getJSONObject(i);
            String key = obj.getString("key");
            String value = obj.getString("value");
            String include = obj.getString("include");

            query += " OR " + getParametersQuery(key, value, true);

            System.out.println(key + ":" + value);
        }
        return query;
    }
}

//http://localhost:8080/alfresco/service/database/retspsyk/page_entries?skip=0&maxItems=10&keyValue=[{%22key%22%20:%20%22cprNummer%22,%20%22value%22%20:%20%221%22}]