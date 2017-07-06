package dk.magenta.utils;

import dk.magenta.model.DatabaseModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONException;

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
}
