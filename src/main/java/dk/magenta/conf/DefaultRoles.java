package dk.magenta.conf;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by flemmingheidepedersen on 14/07/2017.
 */
public class DefaultRoles {

    private List<String> roles_to_bootstrap;

    public DefaultRoles() {
        roles_to_bootstrap = new ArrayList<String>();

        roles_to_bootstrap.add("reopen_cases");
        roles_to_bootstrap.add("edit_lists");
        roles_to_bootstrap.add("assign_roles");
    }

    public List<String> getRolesForBootstrapping() {
        return roles_to_bootstrap;
    }
}
