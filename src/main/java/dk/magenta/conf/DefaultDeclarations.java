package dk.magenta.conf;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by flemmingheidepedersen on 14/07/2017.
 */
public class DefaultDeclarations {

    private List<String> users_to_bootstrap;

    public DefaultDeclarations() throws JSONException {
        users_to_bootstrap = new ArrayList<String>();

        String entities = "ff";

        users_to_bootstrap.add("Estrid");
        JSONObject ethnicity = new JSONObject();
        ethnicity.put("name", "ethnicity");
        ethnicity.put("entities", entities);
    }

    public List<String> getUsersForBootstrapping() {
        return users_to_bootstrap;
    }
}