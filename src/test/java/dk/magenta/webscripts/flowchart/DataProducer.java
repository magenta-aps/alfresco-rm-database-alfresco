package dk.magenta.webscripts.flowchart;

import dk.magenta.Const;
import dk.magenta.beans.EntryBean;
import dk.magenta.beans.PropertyValuesBean;
import dk.magenta.model.DatabaseModel;
import dk.magenta.utils.JSONUtils;
import dk.magenta.webscripts.users.UserHelperTest;
import org.alfresco.model.ContentModel;
import org.alfresco.rad.test.AbstractAlfrescoIT;
import org.alfresco.rad.test.AlfrescoTestRunner;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;

import javax.xml.soap.Node;

import java.io.Serializable;
import java.util.*;

import static org.alfresco.util.ApplicationContextHelper.getApplicationContext;

/**
 * Created by flemmingheidepedersen on 18/05/2018.
 */

public class DataProducer extends AbstractAlfrescoIT {

    private static Logger log = Logger.getLogger(DataProducer.class);

    public ApplicationContext appContext = getApplicationContext();

    private EntryBean entryBean = (EntryBean) appContext.getBean("entryBean");
    private SiteService siteService = (SiteService) appContext.getBean("siteService");
    private NodeService nodeService = (NodeService) appContext.getBean("nodeService");
    private PropertyValuesBean propertyValuesBean = (PropertyValuesBean) appContext.getBean("propertyValuesBean");

    public DataProducer() {
        super();



    }


    private void removeAspect(NodeRef child) {

        List<ChildAssociationRef> children = nodeService.getChildAssocs(child);

        System.out.println("hvad er children");
        System.out.println(children);

        if (children.size() == 0) {
            nodeService.removeAspect(child, ContentModel.ASPECT_LOCKABLE);
        }
        else {
            for (int i = 0; i <= children.size()-1;i++) {
                NodeRef subChild = children.get(i).getChildRef();
                removeAspect(subChild);
            }
        }
    }


    private void createDeclarations(int number, String state, String doctor, String psychologist, String socialworker  ) {

        JSONObject jsonProperties = new JSONObject();

        Random r = new Random();

        List<String> femaleNames = new ArrayList<>();
        femaleNames.add("Mathilde");
        femaleNames.add("Clara");
        femaleNames.add("Mona");
        femaleNames.add("Kirstine");
        femaleNames.add("Ulla");
        femaleNames.add("Emma");
        femaleNames.add("Ingrid");
        femaleNames.add("Lykke");
        femaleNames.add("Majbrit");

        List<String> maleNames = new ArrayList<>();
        maleNames.add("Jens");
        maleNames.add("Kurt");
        maleNames.add("Mathias");
        maleNames.add("Dennis");
        maleNames.add("Dan");
        maleNames.add("Mogens");
        maleNames.add("Hans");
        maleNames.add("Niels");
        maleNames.add("Jan");

        Map<Boolean, List<String>> firstNames = new HashMap<>();
        firstNames.put(true, maleNames);
        firstNames.put(false, femaleNames);

        List<String> lastname = new ArrayList<>();
        lastname.add("Eskildsen");
        lastname.add("Fischer");
        lastname.add("Vestergaard");
        lastname.add("Frost");
        lastname.add("Hansen");
        lastname.add("Hedegård");
        lastname.add("Eskildsen");
        lastname.add("Hermansen");
        lastname.add("Nielsen");
        lastname.add("Madsen");
        lastname.add("Østergaard");
        lastname.add("Holm");

        JSONObject result;
        result = propertyValuesBean.getPropertyValues(DatabaseModel.TYPE_PSYC_SITENAME);


        for (int i = 1; i <= number; i++) {

            try {

                org.json.JSONArray ethnicity = result.getJSONArray("ethnicity");
                org.json.JSONArray referingAgency = result.getJSONArray("referingAgency");
                org.json.JSONArray mainCharge = result.getJSONArray("mainCharge");
                org.json.JSONArray placement = result.getJSONArray("placement");
                org.json.JSONArray sanctionProposal = result.getJSONArray("sanctionProposal");
                org.json.JSONArray diagnosis = result.getJSONArray("diagnosis");
                org.json.JSONArray finalVerdict = result.getJSONArray("finalVerdict");
                org.json.JSONArray status = result.getJSONArray("status");
                org.json.JSONArray noDeclarationReason = result.getJSONArray("noDeclarationReason");


                boolean isMale = r.nextBoolean();
                jsonProperties.put("cprNumber", getRandomCPRString(isMale));
                jsonProperties.put("firstName", firstNames.get(isMale).get(r.nextInt(8)));
                jsonProperties.put("status", status.get(r.nextInt(11)));
                jsonProperties.put("lastName", lastname.get(r.nextInt(11)));
                jsonProperties.put("fullName", jsonProperties.get("firstName") + " " + jsonProperties.get("lastName"));
                jsonProperties.put("address", "Singularisvej 12");
                jsonProperties.put("postbox", "2700");
                jsonProperties.put("city", "Assens");
                jsonProperties.put("ethnicity", ethnicity.get(r.nextInt(5)));
                jsonProperties.put("motherEthnicity", ethnicity.get(r.nextInt(5)));
                jsonProperties.put("fatherEthnicity", ethnicity.get(r.nextInt(5)));
                jsonProperties.put("referingAgency", referingAgency.get(r.nextInt(5)));
                jsonProperties.put("mainCharge", mainCharge.get(r.nextInt(65)));
                jsonProperties.put("placement", placement.get(r.nextInt(13)));
                jsonProperties.put("sanctionProposal", sanctionProposal.get(r.nextInt(15)));
                String[] randomDateStrings = getRandomDateStrings(3);
                jsonProperties.put("creationDate", randomDateStrings[0] + "T00:00:00.000Z");
                jsonProperties.put("observationDate", randomDateStrings[1] + "T00:00:00.000Z");
                jsonProperties.put("declarationDate", randomDateStrings[2] + "T00:00:00.000Z");
                jsonProperties.put("forensicDoctorCouncil", "");
                jsonProperties.put("forensicDoctorCouncilText", "");
                jsonProperties.put("finalVerdict", finalVerdict.get(r.nextInt(15)));
                jsonProperties.put("doctor", doctor);
                jsonProperties.put("psychologist", psychologist);
                jsonProperties.put("socialworker", socialworker);
                jsonProperties.put("status", state);
                jsonProperties.put("mainDiagnosis", diagnosis.get(r.nextInt(1000)));
                jsonProperties.put("biDiagnoses", "[\"" + diagnosis.get(r.nextInt(1000)) + "\"]");
                jsonProperties.put("biDiagnoses", "[\"" + diagnosis.get(r.nextInt(1000)) + "\"]");
                System.out.println(jsonProperties);

                Map<QName, Serializable> properties = JSONUtils.getMap(jsonProperties);
                NodeRef nodeRef = entryBean.addEntry(DatabaseModel.TYPE_PSYC_SITENAME, DatabaseModel.TYPE_PSYC_DEC, properties, false);


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


    }

    private String getRandomCPRString(boolean isMale) {
        Random r = new Random();
        String year = toTwoDigit(r.nextInt(50) + 40);
        String month = toTwoDigit(r.nextInt(11) + 1);
        String day = toTwoDigit(r.nextInt(27) + 1);
        String lastFour1 = toTwoDigit(r.nextInt(98) + 1);
        Integer lastFour2Temp = r.nextInt(97) + 1;

        if(isMale)
            lastFour2Temp += lastFour2Temp % 2 == 0 ? 1 : 0;
        else
            lastFour2Temp += lastFour2Temp % 2;

        String lastFour2 = toTwoDigit(lastFour2Temp);

        return day + month + year + lastFour1 + lastFour2;
    }

    private String[] getRandomDateStrings(int count) {
        String[] randomDates = new String[count];
        randomDates[0] = getRandomDateString(2005);
        for(int i=1; i<count; i++) {
            String[] split = randomDates[i - 1].split("-");
            randomDates[i] = getRandomDateString(Integer.parseInt(split[0]) + 1);
        }
        return randomDates;
    }

    private String getRandomDateString(Integer minimumYear) {
        Random r = new Random();
        String year = toTwoDigit(r.nextInt(3) + minimumYear);
        String month = toTwoDigit(r.nextInt(11) + 1);
        String day = toTwoDigit(r.nextInt(27) + 1);
        return year + "-" + month + "-" + day;
    }

    private String toTwoDigit(Integer i) {
        if(i < 10)
            return "0" + i;
        return i.toString();
    }



    public void createDeclarationsForOngoingTest() {

        // I området ”Igangværende” findes de sager som har fået tildelt enten en psykolog, læge eller socialrådgiver, og som ikke har Status enten Ambulant/Arrestant,
        // Ambulant/Surrogatbehandling, Indlagt til observation, eller Status der begynder med ”GR-”


        // negative hits

        this.createDeclarations(10, "Gr-afsoner", "Harees, Farahna\t- lægekonsulent, speciallæge i psykiatri", "", "");
        this.createDeclarations(10, "Gr-afsoner", "", "Dam, Iris Billeskov", "");
        this.createDeclarations(10, "Gr-afsoner", "", "", "Hansen, Anne Marie");

        this.createDeclarations(10, "Ambulant/Surrogatbehandling", "Harees, Farahna\t- lægekonsulent, speciallæge i psykiatri", "", "");
        this.createDeclarations(10, "Indlagt til observation", "", "Dam, Iris Billeskov", "");
        this.createDeclarations(10, "Ambulant/Arrestant", "", "", "Hansen, Anne Marie");

        this.createDeclarations(10, "Ambulant/Surrogatbehandling", "Harees, Farahna\t- lægekonsulent, speciallæge i psykiatri", "Dam, Iris Billeskov", "");
        this.createDeclarations(10, "Indlagt til observation", "Harees, Farahna\t- lægekonsulent, speciallæge i psykiatri", "Dam, Iris Billeskov", "");
        this.createDeclarations(10, "Ambulant/Arrestant", "Harees, Farahna\t- lægekonsulent, speciallæge i psykiatri", "Dam, Iris Billeskov", "Hansen, Anne Marie");


        // positive hits



//        0: "Ambulant"
//        1: "Ambulant/arrestant"
//        2: "Ambulant/surrogatanbragt"
//        3: "Indlagt til observation"
//        4: "Afsoner"
//        5: "Gr-Ambulant"
//        6: "Gr-Ambulant/arrestant"
//        7: "Gr-Ambulant/surrogatanbragt"
//        8: "Gr-Indlagt til observation"
//        9: "Gr-afsoner"
//        10: "Torturundersøgelse"
//        11: "Benådningssag"



//        String[] status = new String[4];
//        status[0] = "Ambulant/arrestant";
//        status[1] = "Ambulant/surrogatanbragt";
//        status[2] = "Indlagt til observation";
//        status[3] = "Gr-*";


        // læger

//        String Arngrim, Trine - ledende overlæge, speciallæge i psykiatri"
//        1: "Nitschke, Kirsten - overlæge, speciallæge i psykiatri"
//        2: "Harees, Farahna	- lægekonsulent, speciallæge i psykiatri"
//        3: "Kapilin, Ulla - lægekonsulent, speciallæge i psykiatri"
//        4: "Tøttrup, Jette - overlæge, speciallæge i psykiatri"
//        5: "Nielsen, Niels - overlæge, speciallæge i psykiatri"
//        6: "Thomsen, Anders Frøkjær - lægekonsulent, speciallæge i psykiatri"
//        7: "Lund, Jens - lægekonsulent, speciallæge i psykiatri"


        // psykologer

//        "Brinck, Mette"
//        1: "Wiwe, Louise Brückner"
//        2: "Olsen, Olav"
//        3: "Werchmeister, Claus"
//        4: "Larsen, Ulla"
//        5: "Dam, Iris Billeskov"
//        6: "Pedersen, Lars Bjerggaard"
//        7: "Mollerup, Rose-Marie"
//        8: "Hjortebjerg, Pia Lykke"
//        9: "Henriksen, Anne"

        // socialrådgiver

//        0: "Jensen, Lis"
//        1: "Hansen, Anne Marie"
//        2: "Sand, Mette"



    }

    public void createDeclarations () {






        String[] status = new String[4];
        status[0] = "Ambulant/arrestant";
        status[1] = "Ambulant/surrogatanbragt";
        status[2] = "Indlagt til observation";
        status[3] = "Gr-*";


        // læger

//        String Arngrim, Trine - ledende overlæge, speciallæge i psykiatri"
//        1: "Nitschke, Kirsten - overlæge, speciallæge i psykiatri"
//        2: "Harees, Farahna	- lægekonsulent, speciallæge i psykiatri"
//        3: "Kapilin, Ulla - lægekonsulent, speciallæge i psykiatri"
//        4: "Tøttrup, Jette - overlæge, speciallæge i psykiatri"
//        5: "Nielsen, Niels - overlæge, speciallæge i psykiatri"
//        6: "Thomsen, Anders Frøkjær - lægekonsulent, speciallæge i psykiatri"
//        7: "Lund, Jens - lægekonsulent, speciallæge i psykiatri"


        // psykologer

//        "Brinck, Mette"
//        1: "Wiwe, Louise Brückner"
//        2: "Olsen, Olav"
//        3: "Werchmeister, Claus"
//        4: "Larsen, Ulla"
//        5: "Dam, Iris Billeskov"
//        6: "Pedersen, Lars Bjerggaard"
//        7: "Mollerup, Rose-Marie"
//        8: "Hjortebjerg, Pia Lykke"
//        9: "Henriksen, Anne"

        // socialrådgiver

//        0: "Jensen, Lis"
//        1: "Hansen, Anne Marie"
//        2: "Sand, Mette"

        this.createDeclarations(15, "Ambulant/arrestant", "Nitschke, Kirsten - overlæge, speciallæge i psykiatri", "", "");
    }

    public void wipeAllCases()  {

        System.out.println("entryBean");
        System.out.println(entryBean);


        NodeRef docLibRef = siteService.getContainer("retspsyk", SiteService.DOCUMENT_LIBRARY);

        // reset counter
        nodeService.setProperty(docLibRef, ContentModel.PROP_COUNTER, 0);

        this.removeAspect(docLibRef);

        List<ChildAssociationRef> children = nodeService.getChildAssocs(docLibRef);

        System.out.println("hvad er children");
        System.out.println(children);

        for (int i = 0; i <= children.size()-1;i++) {
            NodeRef subChild = children.get(i).getChildRef();
            nodeService.deleteNode(subChild);
        }



    }


}
