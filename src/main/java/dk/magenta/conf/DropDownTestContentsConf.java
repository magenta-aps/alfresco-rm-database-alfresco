package dk.magenta.conf;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by flemmingheidepedersen on 03/07/2017.
 */



public class DropDownTestContentsConf {

    private JSONArray testdata_to_bootstrap;


    public JSONArray getTestdata_to_bootstrap() {
        return testdata_to_bootstrap;
    }

    public DropDownTestContentsConf()  {
        StringTokenizer st = null;
        testdata_to_bootstrap = new JSONArray();
        try {

            String etnicitet_input = "Vesten,Østeuropa,Iran,Mellemøsten,Nordafrika,Afrika - Øvrige,Fjernøsten,Resten af verden,Grønland";
            st = new StringTokenizer(etnicitet_input, ",");

            JSONArray entities = new JSONArray();
            while (st.hasMoreElements()) {
                entities.put(st.nextElement());
            }

            JSONObject ethnicity = new JSONObject();
            ethnicity.put("name", "ethnicity");
            ethnicity.put("entities", entities);

            testdata_to_bootstrap.put(ethnicity);


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


            String referingAgency_input = "Midt- og Vestjyllands politi,Sydøstjyllands politi,Syd- og Sønderjyllands politi,Nordjyllands politi,Fyns politi,Sydsjællands og Lolland-Falsters politi,Midt- og Vestsjællands politi,Nordsjællands politi,Københavns Vestegns politi,Københavns politi,Bornholms politi,Grønlands politi,Færøernes politi,Statsadvokaten for København og Bornholm (før 2006),Statsadvokaten for Nordsjælland og Kbh. Vestegn (før 2006) ,Statsadvokaten for Midt-, Vest- og Sydsj., L og F (før 2006),Statsadvokaten for Nord- og Østjylland (før 2006),Statsadvokaten for Midt-, Vest og Sydøstjylland (før 2006),Statsadvokaten for Fyn, Syd- og Sønderjylland (før 2006),Statsadvokaten for Særlig Økonomisk Kriminalitet (før 2006),Statsadvokaten for Særlige Int. Straffesager (før 2006),Rigsadvokaten (før 2006),Direktoratet for Kriminalforsorgen,Udlændingeservice/Udlændingestyrelsen,Andre,Flygtningenævnet,Statsadvokaten for Særlig Økonomisk og International Krimina,Statsadvokaten i Viborg,Statsadvokaten i København";

            st = new StringTokenizer(referingAgency_input, ",");

            entities = new JSONArray();
            while (st.hasMoreElements()) {
                entities.put(st.nextElement());
            }

            JSONObject referingAgency = new JSONObject();
            referingAgency.put("name", "referingAgency");
            referingAgency.put("entities", entities);

            testdata_to_bootstrap.put(referingAgency);



            //new


            String mainCharge_input = "Manddrab, forsøg §§ 237-241, jf. § 21,Vold § 244,Vold, forsøg § 244, jf. § 21,Kvalificeret vold m/skade på legeme eller helbred § 245,Kvalificeret vold m/skade på legeme eller helbred, forsøg § 245, jf. § 21,Kvalificeret vold m/grov skade § 246,Kvalificeret vold m/grov skade, forsøg § 246, jf. § 21,Vold og lign. mod nogen i offentlig tjeneste § 119,Vold og lign. mod nogen i offentlig tjeneste, forsøg § 119, jf. § 21,Anden personfarlig kriminalitet § 249-255,Anden personfarlig kriminalitet, forsøg § 249-255, jf. § 21,Trusler § 265-266,Trusler, forsøg § 265-266,Ulovlig tvang §§ 260-262,Ulovlig tvang, forsøg §§ 260-262,Brandstiftelse, kvalificeret § 180,Brandstiftelse, kvalificeret, forsøg § 180, jf. § 21,Brandstiftelse, forsætlig § 181,Brandstiftelse, forsætlig, forsøg § 181, jf. § 21,Voldtægt § 216,Voldtægt, forsøg § 216, jf. § 21,Anden sædelighed §§ 210-236 (- § 216),Anden sædelighed, forsøg §§ 210-236 (- § 216), jf. § 21,Narkotikakriminalitet § 191,Narkotikakriminalitet, forsøg § 191, jf. § 21,Lov om euforiserende stoffer,Lov om euforiserende stoffer, forsøg,Røveri § 288, stk. 1,Røveri, forsøg § 288, stk. 1, jf. § 21,Røveri, særligt farligt, § 288, stk. 2,Røveri, særligt farligt, forsøg § 288, stk. 2, jf. § 21,Tyveri § 276,Tyveri, forsøg § 276, jf. § 21,Bedrageri mv. §§ 278-280,Bedrageri mv., forsøg §§ 278-280, jf. § 21,Hærværk § 291,Hærværk, forsøg § 291, jf. § 21,Straffeloven, øvrige,Straffeloven, øvrige, forsøg,Særloven, øvrige,Særloven, øvrige, forsøg,Benådningsandragende,Asylansøger,andet,Gr-krænkelse af offentlig myndighed, Krl.§ 16,Gr-forbrydelser i familieforhold Krl.§ 49 - blodskam,Gr-forbrydelser i familieforhold, andre Krl. §§ 48-50 (-§49),Gr-forbrydelser mod kønssædeligheden – voldtægt, Krl. § 51,Gr-forbrydelser mod kønssædeligheden – voldtægtsforsøg, Krl.§ 51, jf. § 8,Gr-forbrydelser mod kønssædeligheden – barn under 15 år, Krl. § 53,Gr-forbrydelser mod kønssædeligheden – forførelse, Krl. § 54,Gr-forbrydelser mod kønssædeligheden – blufærdighedskrænkelse, Krl. § 56,Gr-forbrydelser mod kønssædeligheden, andre, Krl. § 51-56 (- § 51, 53, 54, 56),Gr-forbrydelser mod liv og legeme – manddrab og uagtsomt manddrab, Krl. § 57 og 58,Gr-forbrydelser mod liv og legeme – drabsforsøg, Krl.§ 57 og 58, jf. § 8,Gr-forbrydelser mod liv og legeme – vold, Krl. § 60,Gr-forbrydelser mod liv og legeme, andre, Krl. § 57-64 (-§ 57, 58 og 60),Gr-formueforbrydelser – tyveri, Krl. § 72,Gr-formueforbrydelser – hærværk, Krl. § 79,Gr-formueforbrydelser – brugstyveri, Krl. § 80,Gr-formueforbrydelser, andre, Krl. § 72-81 (-§§ 72,79, 80),Gr-kriminalloven i øvrigt,Gr-lov om euforiserende stoffer,Gr-særlove i øvrigt,NYGr-krænkelse af offentlig myndighed - NYKrl. § 37,NYGr-brandstiftelse - NYKrl. § 65,NYGr-forbrydelser i familieforhold - blodskam - NYKrl. § 74,NYGr-forbrydelser i familieforhold - andre - NYKrl. § 73 - 76 (- § 74),NYGr-sexualforbrydelser - voldtægt - NYKrl. § 77,NYGr-sexualforbrydelser - voldtægtsforsøg - NYKrl. § 77, jf. § 12,NYGr-sexualforbrydelser - samleje eller anden kønslig omgang med barn under 15 år - NYKrl. § 79,NYGr-sexualforbrydelser - forførelse - NYKrl. § 80,NYGr-sexualforbrydelser - blufærdighedskrænkelse - NYKrl. § 84,NYGr-sexualforbrydelser - andre - NYKrl. § 77 - 85 (- § 77, 79, 80, 84),NYGr-forbrydelser mod liv og legeme - manddrab og uagtsomt manddrab - NYKrl. § 86 og § 87,NYGr-forbrydelser mod liv og legeme - drabsforsøg - NYKrl. § 86 eller § 87, jf. § 12,NYGr-forbrydelser mod liv og legeme - vold - NYKrl. § 88,NYGr-forbrydelser mod liv og legeme - andre - NYKrl. § 86 - 90 (- § 86, 87, 88),NYGr-forbrydelser mod den personlige frihed - NYKrl. § 91 - 93,NYGr-freds- og æreskrænkelser - NYKrl. § 94 - 100,NYGr-formueforbrydelser - tyveri - NYKrl. § 102,NYGr-formueforbrydelser - røveri - NYKrl. § 112,NYGr-formueforbrydelser - tingsbeskadigelse - NYKrl. § 113,NYGr-formueforbrydelser - brugstyveri - NYKrl. § 114,NYGr-formueforbrydelser - andre - NYKrl. § 102 - 117,NYGr-kriminalloven i øvrigt,NYGr-lov om euforiserende stoffer,NYGr-særlove i øvrigt";


            st = new StringTokenizer(mainCharge_input, ",");

            entities = new JSONArray();
            while (st.hasMoreElements()) {
                entities.put(st.nextElement());
            }

            JSONObject mainCharge = new JSONObject();
            mainCharge.put("name", "mainCharge");
            mainCharge.put("entities", entities);

            testdata_to_bootstrap.put(mainCharge);


            //new


            String placement_input = "Manddrab, forsøg §§ 237-241, jf. § 21,Vold § 244,Vold, forsøg § 244, jf. § 21,Kvalificeret vold m/skade på legeme eller helbred § 245,Kvalificeret vold m/skade på legeme eller helbred, forsøg § 245, jf. § 21,Kvalificeret vold m/grov skade § 246,Kvalificeret vold m/grov skade, forsøg § 246, jf. § 21,Vold og lign. mod nogen i offentlig tjeneste § 119,Vold og lign. mod nogen i offentlig tjeneste, forsøg § 119, jf. § 21,Anden personfarlig kriminalitet § 249-255,Anden personfarlig kriminalitet, forsøg § 249-255, jf. § 21,Trusler § 265-266,Trusler, forsøg § 265-266,Ulovlig tvang §§ 260-262,Ulovlig tvang, forsøg §§ 260-262,Brandstiftelse, kvalificeret § 180,Brandstiftelse, kvalificeret, forsøg § 180, jf. § 21,Brandstiftelse, forsætlig § 181,Brandstiftelse, forsætlig, forsøg § 181, jf. § 21,Voldtægt § 216,Voldtægt, forsøg § 216, jf. § 21,Anden sædelighed §§ 210-236 (- § 216),Anden sædelighed, forsøg §§ 210-236 (- § 216), jf. § 21,Narkotikakriminalitet § 191,Narkotikakriminalitet, forsøg § 191, jf. § 21,Lov om euforiserende stoffer,Lov om euforiserende stoffer, forsøg,Røveri § 288, stk. 1,Røveri, forsøg § 288, stk. 1, jf. § 21,Røveri, særligt farligt, § 288, stk. 2,Røveri, særligt farligt, forsøg § 288, stk. 2, jf. § 21,Tyveri § 276,Tyveri, forsøg § 276, jf. § 21,Bedrageri mv. §§ 278-280,Bedrageri mv., forsøg §§ 278-280, jf. § 21,Hærværk § 291,Hærværk, forsøg § 291, jf. § 21,Straffeloven, øvrige,Straffeloven, øvrige, forsøg,Særloven, øvrige,Særloven, øvrige, forsøg,Benådningsandragende,Asylansøger,andet,Gr-krænkelse af offentlig myndighed, Krl.§ 16,Gr-forbrydelser i familieforhold Krl.§ 49 - blodskam,Gr-forbrydelser i familieforhold, andre Krl. §§ 48-50 (-§49),Gr-forbrydelser mod kønssædeligheden – voldtægt, Krl. § 51,Gr-forbrydelser mod kønssædeligheden – voldtægtsforsøg, Krl.§ 51, jf. § 8,Gr-forbrydelser mod kønssædeligheden – barn under 15 år, Krl. § 53,Gr-forbrydelser mod kønssædeligheden – forførelse, Krl. § 54,Gr-forbrydelser mod kønssædeligheden – blufærdighedskrænkelse, Krl. § 56,Gr-forbrydelser mod kønssædeligheden, andre, Krl. § 51-56 (- § 51, 53, 54, 56),Gr-forbrydelser mod liv og legeme – manddrab og uagtsomt manddrab, Krl. § 57 og 58,Gr-forbrydelser mod liv og legeme – drabsforsøg, Krl.§ 57 og 58, jf. § 8,Gr-forbrydelser mod liv og legeme – vold, Krl. § 60,Gr-forbrydelser mod liv og legeme, andre, Krl. § 57-64 (-§ 57, 58 og 60),Gr-formueforbrydelser – tyveri, Krl. § 72,Gr-formueforbrydelser – hærværk, Krl. § 79,Gr-formueforbrydelser – brugstyveri, Krl. § 80,Gr-formueforbrydelser, andre, Krl. § 72-81 (-§§ 72,79, 80),Gr-kriminalloven i øvrigt,Gr-lov om euforiserende stoffer,Gr-særlove i øvrigt,NYGr-krænkelse af offentlig myndighed - NYKrl. § 37,NYGr-brandstiftelse - NYKrl. § 65,NYGr-forbrydelser i familieforhold - blodskam - NYKrl. § 74,NYGr-forbrydelser i familieforhold - andre - NYKrl. § 73 - 76 (- § 74),NYGr-sexualforbrydelser - voldtægt - NYKrl. § 77,NYGr-sexualforbrydelser - voldtægtsforsøg - NYKrl. § 77, jf. § 12,NYGr-sexualforbrydelser - samleje eller anden kønslig omgang med barn under 15 år - NYKrl. § 79,NYGr-sexualforbrydelser - forførelse - NYKrl. § 80,NYGr-sexualforbrydelser - blufærdighedskrænkelse - NYKrl. § 84,NYGr-sexualforbrydelser - andre - NYKrl. § 77 - 85 (- § 77, 79, 80, 84),NYGr-forbrydelser mod liv og legeme - manddrab og uagtsomt manddrab - NYKrl. § 86 og § 87,NYGr-forbrydelser mod liv og legeme - drabsforsøg - NYKrl. § 86 eller § 87, jf. § 12,NYGr-forbrydelser mod liv og legeme - vold - NYKrl. § 88,NYGr-forbrydelser mod liv og legeme - andre - NYKrl. § 86 - 90 (- § 86, 87, 88),NYGr-forbrydelser mod den personlige frihed - NYKrl. § 91 - 93,NYGr-freds- og æreskrænkelser - NYKrl. § 94 - 100,NYGr-formueforbrydelser - tyveri - NYKrl. § 102,NYGr-formueforbrydelser - røveri - NYKrl. § 112,NYGr-formueforbrydelser - tingsbeskadigelse - NYKrl. § 113,NYGr-formueforbrydelser - brugstyveri - NYKrl. § 114,NYGr-formueforbrydelser - andre - NYKrl. § 102 - 117,NYGr-kriminalloven i øvrigt,NYGr-lov om euforiserende stoffer,NYGr-særlove i øvrigt";


            st = new StringTokenizer(placement_input, ",");

            entities = new JSONArray();
            while (st.hasMoreElements()) {
                entities.put(st.nextElement());
            }

            JSONObject placement = new JSONObject();
            placement.put("name", "placement");
            placement.put("entities", entities);

            testdata_to_bootstrap.put(placement);

            //new

            String sanctionProposal_input = "Benådning anbefales,Benådning anbefales ikke,Dom til ambulant psykiatrisk behandling (C.1),Dom til ambulant psykiatrisk behandling, tilsyn af kriminalforsorgen, indlæggelse efter beslutning af kriminalforsorgen og overlægen (C.4),Dom til ambulant psykiatrisk behandling, indlæggelser efter overlægens beslutning (C.3),Dom til ambulant psykiatrisk behandling, tilsyn af kriminalforsorgen (C.2),Dom til anbringelse (mentalt retarderede) (III),Dom til anbringelse, psykiatrisk afdeling (A.1),Dom til anbringelse, psykiatrisk afdeling, indtil straffen kan fuldbyrdes,Dom til anbringelse, Sikringsafdelingen (A.2),Dom til anbringelse, Sikringsafdelingen, indtil straffen kan fuldbyrdes,Dom til anbringelse, særligt sikret institution (mentalt retarderede) (I +II),Dom til at undergive sig bestemmelser i lov om social service (tidligere bistandsloven),Dom til psykiatrisk behandling (B1),Dom til psykiatrisk behandling, tilsyn af kriminalforsorgen (B2),Dom til psykiatrisk behandling, tilsyn af kriminalforsorgen, indtil straffen kan fuldbyrdes,Dom til tilsyn (mentalt retarderede) (V),Dom til tilsyn af kriminalforsorgen (som særforanstaltning),Dom til tilsyn med mulighed for anbringelse (mentalt retarderede) (IV),Dom til tilsyn med vilkår (mentalt retarderede) (V),Dom til ungdomssanktion (straffelovens § 74a),Forvaring,Ingen særforanstaltning,S-sag forelægges visitationsudvalget,Gr-anbringelse i Sikringsanstalten ,Gr-anbringelse i hospital for sindslidende i Danmark,Gr-behandlingsdom i Danmark eller under tilsyn heraf,Gr-behandlingsdom i Grønland, om fornødent Danmark,Gr-behandlingsdom i Grønland,Gr-anbringelse på institution for psykisk udviklingshæmmede,Gr-forsorg for psykisk udviklingshæmmede,Gr-tilsyn af Kriminalforsorgen med pålæg,Gr-anbringelse i Anstalten ved Herstedvester efter § 102, stk. 3 (NYGr § 161 stk. 4),Gr-ingen særforanstaltning,Gr-andet,Frifundet";

            st = new StringTokenizer(sanctionProposal_input, ",");

            entities = new JSONArray();
            while (st.hasMoreElements()) {
                entities.put(st.nextElement());
            }

            JSONObject sanctionProposal = new JSONObject();
            sanctionProposal.put("name", "sanctionProposal");
            sanctionProposal.put("entities", entities);

            testdata_to_bootstrap.put(sanctionProposal);


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
