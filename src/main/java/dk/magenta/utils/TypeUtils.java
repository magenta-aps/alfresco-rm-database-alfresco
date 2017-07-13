package dk.magenta.utils;

import dk.magenta.model.DatabaseModel;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.JSONArray;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TypeUtils {

    public static final Map<String, JSONObject> types = Collections.unmodifiableMap(
            new HashMap<String, JSONObject>() {
                {
                    put(DatabaseModel.TYPE_PSYC_DEC,
                            createType(
                                    DatabaseModel.TYPE_PSYC_DEC,
                                    DatabaseModel.TYPE_PSYC_DEC_KEY
                            )
                    );
                }});

    private static JSONObject createType(String name, String entryKey){
        JSONObject json = new JSONObject();
        try {
            json.put(DatabaseModel.NAME, name);
            json.put(DatabaseModel.ENTRY_KEY, entryKey);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public static String getEntryKey (String type) throws JSONException {
        return types.get(type).getString(DatabaseModel.ENTRY_KEY);
    }
}
