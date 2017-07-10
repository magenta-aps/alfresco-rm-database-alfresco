package dk.magenta.conf;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by flemmingheidepedersen on 03/07/2017.
 */



public class DropDownTestContentsConf {

    private JSONArray testdata_to_bootstrap;


    public JSONArray getTestdata_to_bootstrap() {
        return testdata_to_bootstrap;
    }

    public DropDownTestContentsConf()  {


        try {

            testdata_to_bootstrap = new JSONArray();

            JSONArray entities = new JSONArray();
            try(BufferedReader br = new BufferedReader(new FileReader(System.getProperty("user.dir")  + "/src/main/java/dk/magenta/webscripts/etnicitet.txt"))) {
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();

                while (line != null) {
                    sb.append(line);
                    sb.append(System.lineSeparator());
                    line = br.readLine();

                    if (line != null) {
                        entities.put(line);
                    }
                }

                JSONObject ethnicity = new JSONObject();
                ethnicity.put("name", "ethnicity");
                ethnicity.put("entities", entities);

                testdata_to_bootstrap.put(ethnicity);
            }








            //new
            JSONObject motherEthnicity = new JSONObject();
            motherEthnicity.put("name", "motherEthnicity");
            motherEthnicity.put("entities", entities);

            testdata_to_bootstrap.put(motherEthnicity);


            //new
            JSONObject fatherEthnicity = new JSONObject();
            fatherEthnicity.put("name", "fatherEthnicity");
            fatherEthnicity.put("entities", entities);

            testdata_to_bootstrap.put(fatherEthnicity);

            //new

            entities = new JSONArray();
            try(BufferedReader br = new BufferedReader(new FileReader(System.getProperty("user.dir")  + "/src/main/java/dk/magenta/webscripts/henvisende_instans.txt/"))) {
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();

                while (line != null) {
                    sb.append(line);
                    sb.append(System.lineSeparator());
                    line = br.readLine();

                    if (line != null) {
                        System.out.println("what is the line" + line);
                        entities.put(line);
                    }
                }

                JSONObject referingAgency = new JSONObject();
                referingAgency.put("name", "referingAgency");
                referingAgency.put("entities", entities);

                testdata_to_bootstrap.put(referingAgency);
            }


            //new

            entities = new JSONArray();
            try(BufferedReader br = new BufferedReader(new FileReader(System.getProperty("user.dir")  + "/src/main/java/dk/magenta/webscripts/hovedsigtelser.txt/"))) {
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();

                while (line != null) {
                    sb.append(line);
                    sb.append(System.lineSeparator());
                    line = br.readLine();

                    if (line != null) {
                        System.out.println("what is the line" + line);
                        entities.put(line);
                    }
                }

                JSONObject mainCharge = new JSONObject();
                mainCharge.put("name", "mainCharge");
                mainCharge.put("entities", entities);

                testdata_to_bootstrap.put(mainCharge);
            }
            //new

            entities = new JSONArray();
            try(BufferedReader br = new BufferedReader(new FileReader(System.getProperty("user.dir")  + "/src/main/java/dk/magenta/webscripts/placering.txt/"))) {
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();

                while (line != null) {
                    sb.append(line);
                    sb.append(System.lineSeparator());
                    line = br.readLine();

                    if (line != null) {
                        System.out.println("what is the line" + line);
                        entities.put(line);
                    }
                }

                JSONObject placement = new JSONObject();
                placement.put("name", "placement");
                placement.put("entities", entities);

                testdata_to_bootstrap.put(placement);
            }

            //new

            entities = new JSONArray();
            try(BufferedReader br = new BufferedReader(new FileReader(System.getProperty("user.dir")  + "/src/main/java/dk/magenta/webscripts/sanktionsforslag.txt/"))) {
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();

                while (line != null) {
                    sb.append(line);
                    sb.append(System.lineSeparator());
                    line = br.readLine();

                    if (line != null) {
                        System.out.println("what is the line" + line);
                        entities.put(line);
                    }
                }

                JSONObject sanctionProposal = new JSONObject();
                sanctionProposal.put("name", "sanctionProposal");
                sanctionProposal.put("entities", entities);

                testdata_to_bootstrap.put(sanctionProposal);
            }

            //new
            String sentTo1 = "sentTo1";
            String sentTo2 = "sentTo2";
            entities = new JSONArray();

            entities.put(sentTo1);
            entities.put(sentTo2);

            JSONObject sentTo = new JSONObject();
            sentTo.put("name", "sentTo");
            sentTo.put("entities", entities);

            testdata_to_bootstrap.put(sentTo);

            //new
            String finalVerdict1 = "finalVerdict1";
            String finalVerdict2 = "finalVerdict2";
            entities = new JSONArray();

            entities.put(finalVerdict1);
            entities.put(finalVerdict2);

            JSONObject finalVerdict = new JSONObject();
            finalVerdict.put("name", "finalVerdict");
            finalVerdict.put("entities", entities);

            testdata_to_bootstrap.put(finalVerdict);

            //new
            String doctor1 = "doctor1";
            String doctor2 = "doctor2";
            entities = new JSONArray();

            entities.put(doctor1);
            entities.put(doctor2);

            JSONObject doctor_1 = new JSONObject();
            doctor_1.put("name", "doctor1");
            doctor_1.put("entities", entities);

            testdata_to_bootstrap.put(doctor_1);

            //new
            String doctor2_1 = "doctor2_1";
            String doctor2_2 = "doctor2_2";
            entities = new JSONArray();

            entities.put(doctor2_1);
            entities.put(doctor2_2);

            JSONObject doctor_2 = new JSONObject();
            doctor_2.put("name", "doctor2");
            doctor_2.put("entities", entities);

            testdata_to_bootstrap.put(doctor_2);

            //new
            String socialWorker1 = "socialWorker1";
            String socialWorker2 = "socialWorker2";
            entities = new JSONArray();

            entities.put(socialWorker1);
            entities.put(socialWorker2);

            JSONObject socialWorker = new JSONObject();
            socialWorker.put("name", "socialWorker");
            socialWorker.put("entities", entities);

            testdata_to_bootstrap.put(socialWorker);

            //new
            String psychologist1 = "psychologist1";
            String psychologist2 = "psychologist2";
            entities = new JSONArray();

            entities.put(psychologist1);
            entities.put(psychologist2);

            JSONObject psychologist = new JSONObject();
            psychologist.put("name", "psychologist");
            psychologist.put("entities", entities);

            testdata_to_bootstrap.put(psychologist);

            //new
            String secretary1 = "secretary1";
            String secretary2 = "secretary2";
            entities = new JSONArray();

            entities.put(secretary1);
            entities.put(secretary2);

            JSONObject secretary = new JSONObject();
            secretary.put("name", "secretary");
            secretary.put("entities", entities);

            testdata_to_bootstrap.put(psychologist);

            System.out.println(System.getProperty("user.dir"));

            entities = new JSONArray();


            try(BufferedReader br = new BufferedReader(new FileReader(System.getProperty("user.dir")  + "/src/main/java/dk/magenta/webscripts/diagnoser.txt/"))) {
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();

                while (line != null) {
                    sb.append(line);
                    sb.append(System.lineSeparator());
                    line = br.readLine();

                    if (line != null) {
                        entities.put(line);
                    }
                }

                JSONObject mainDiagnosis = new JSONObject();
                mainDiagnosis.put("name", "mainDiagnosis");
                mainDiagnosis.put("entities", entities);

                testdata_to_bootstrap.put(mainDiagnosis);
            }




            //new
            String biDiagnoses1 = "biDiagnoses1";
            String biDiagnoses2 = "biDiagnoses2";
            entities = new JSONArray();

            entities.put(biDiagnoses1);
            entities.put(biDiagnoses2);

            JSONObject biDiagnoses = new JSONObject();
            biDiagnoses.put("name", "biDiagnoses");
            biDiagnoses.put("entities", entities);

            testdata_to_bootstrap.put(biDiagnoses);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String [ ] args) {


        DropDownTestContentsConf d = new DropDownTestContentsConf();
    }

}
