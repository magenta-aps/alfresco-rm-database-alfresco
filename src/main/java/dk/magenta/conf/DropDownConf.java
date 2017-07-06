package dk.magenta.conf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by flemmingheidepedersen on 03/07/2017.
 */



public class DropDownConf {

    public List<Dropdown> groups_to_bootstrap;

    public DropDownConf() {
        groups_to_bootstrap = new ArrayList<Dropdown>();

        groups_to_bootstrap.add(new Dropdown("ethnicity", ""));
        groups_to_bootstrap.add(new Dropdown("motherEthnicity", ""));
        groups_to_bootstrap.add(new Dropdown("fatherEthnicity", ""));
        groups_to_bootstrap.add(new Dropdown("referingAgency", ""));
        groups_to_bootstrap.add(new Dropdown("mainCharge", ""));
        groups_to_bootstrap.add(new Dropdown("placement", ""));
        groups_to_bootstrap.add(new Dropdown("sanctionProposal", ""));
        groups_to_bootstrap.add(new Dropdown("sentTo", ""));
        groups_to_bootstrap.add(new Dropdown("finalVerdict", ""));
        groups_to_bootstrap.add(new Dropdown("doctor1", ""));
        groups_to_bootstrap.add(new Dropdown("doctor2", ""));
        groups_to_bootstrap.add(new Dropdown("psychologist", ""));
        groups_to_bootstrap.add(new Dropdown("socialWorker", ""));
        groups_to_bootstrap.add(new Dropdown("secretary", ""));
        groups_to_bootstrap.add(new Dropdown("mainDiagnosis", ""));
        groups_to_bootstrap.add(new Dropdown("biDiagnoses", ""));
    }

    public List<String> getNames() {

        Iterator iterator = groups_to_bootstrap.iterator();
        List<String> result = new ArrayList<String>();

        while (iterator.hasNext()) {
            Dropdown dropdown = (Dropdown)iterator.next();
            result.add(dropdown.getName());
        }
        return result;
    }

    public class Dropdown {
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

        public Dropdown( String name, String source) {
            this.name = name;
            this.source = source;
        }
    }

}
