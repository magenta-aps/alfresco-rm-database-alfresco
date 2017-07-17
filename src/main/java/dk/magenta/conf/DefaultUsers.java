package dk.magenta.conf;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by flemmingheidepedersen on 14/07/2017.
 */
public class DefaultUsers {

    private List<String> users_to_bootstrap;

    public DefaultUsers() {
        users_to_bootstrap = new ArrayList<String>();

        users_to_bootstrap.add("Bo");
        users_to_bootstrap.add("Erik");
        users_to_bootstrap.add("Birger");
        users_to_bootstrap.add("Frode");
        users_to_bootstrap.add("Gisles");
        users_to_bootstrap.add("Gorm");
        users_to_bootstrap.add("Harald");
        users_to_bootstrap.add("Halfdan");
        users_to_bootstrap.add("Knud");
        users_to_bootstrap.add("Njal");

        users_to_bootstrap.add("Astrid");
        users_to_bootstrap.add("Bodil");
        users_to_bootstrap.add("Frida");
        users_to_bootstrap.add("Gertrud");
        users_to_bootstrap.add("Gro");
        users_to_bootstrap.add("Estrid");
        users_to_bootstrap.add("Hilda");
        users_to_bootstrap.add("Gunhild");
        users_to_bootstrap.add("Liv");
        users_to_bootstrap.add("Signe");
    }

    public List<String> getUsersForBootstrapping() {
        return users_to_bootstrap;
    }
}