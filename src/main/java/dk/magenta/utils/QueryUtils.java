package dk.magenta.utils;

import dk.magenta.model.DatabaseModel;
import org.json.JSONException;

public class QueryUtils {

    public static String getTypeQuery (String type) {
        return  "TYPE:\"" + DatabaseModel.RM_MODEL_PREFIX +":" + type + "\"";
    }

    public static String getParameterQuery (String paramKey, String paramValue) {
        return "@" + DatabaseModel.RM_MODEL_PREFIX + "\\:" + paramKey + ":\"" + paramValue + "\"";
    }

    public static String getEntryQuery (String type, String entryValue) throws JSONException {
        String entryKey = TypeUtils.types.get(type).getString(DatabaseModel.ENTRY_KEY);
        return getTypeQuery(type) + " AND " + getParameterQuery(entryKey, entryValue);
    }
}
