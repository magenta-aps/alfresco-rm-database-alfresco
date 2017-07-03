package dk.magenta.webscripts;

import java.awt.dnd.DropTargetDragEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by flemmingheidepedersen on 03/07/2017.
 */



class DropDownConf {

    public static final Dropdown CONSTANT_STRING="CONSTANT_STRING";

    public List<Dropdown> groups_to_bootstrap;

    public DropDownConf() {
        groups_to_bootstrap = new ArrayList<Dropdown>();

        groups_to_bootstrap.add(new Dropdown("ethnicity", ""));
    }


    class Dropdown {
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        private String name;
        private String source;

        public Dropdown(String source, String name) {
            this.source = source;
            this.name = name;
        }
    }

}
