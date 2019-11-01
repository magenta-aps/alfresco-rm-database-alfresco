package dk.magenta.webscripts.flowchart;

import dk.magenta.beans.EntryBean;
import dk.magenta.beans.PropertyValuesBean;
import dk.magenta.model.DatabaseModel;
import dk.magenta.utils.JSONUtils;
import org.alfresco.rad.test.AbstractAlfrescoIT;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.alfresco.util.ApplicationContextHelper.getApplicationContext;

/**
 * Created by flemmingheidepedersen on 18/05/2018.
 */

public class DataProducer extends AbstractAlfrescoIT {

    private static Logger log = Logger.getLogger(DataProducer.class);

    public ApplicationContext appContext = getApplicationContext();

    private EntryBean entryBean = (EntryBean) appContext.getBean("entryBean");
    private SiteService siteService = (SiteService) appContext.getBean("siteService");
    private LockService lockService = (LockService) appContext.getBean("lockService");
    private NodeService nodeService = (NodeService) appContext.getBean("nodeService");
    private PropertyValuesBean propertyValuesBean = (PropertyValuesBean) appContext.getBean("propertyValuesBean");

    private List<String> femaleNames = new ArrayList<>();
    private List<String> maleNames = new ArrayList<>();
    private Map<Boolean, List<String>> firstNames = new HashMap<>();
    private List<String> lastname = new ArrayList<>();
    private JSONObject result;

    private DataProducerHelper dataProducerUtils = new DataProducerHelper();

    private org.json.JSONArray ethnicity;
    private org.json.JSONArray referingAgency;
    private org.json.JSONArray mainCharge;
    private org.json.JSONArray placement;
    private org.json.JSONArray sanctionProposal;
    private org.json.JSONArray diagnosis;
    private org.json.JSONArray finalVerdict;
    private org.json.JSONArray status;
    private org.json.JSONArray noDeclarationReason;

    public DataProducer() throws JSONException {
        super();
    }


    public void createDeclarationsForStateIndlagtObservationTest() throws JSONException, InterruptedException {

        // I området ”Indlagte” findes de sager som har Status Indlagt til observation

        // negative hits

        dataProducerUtils.createDeclarations(10, "Gr-afsoner", "Harees, Farahna\t- lægekonsulent, speciallæge i psykiatri", "", "");
        dataProducerUtils.createDeclarations(10, "Ambulant/surrogatanbragt", "", "Dam, Iris Billeskov", "");
        dataProducerUtils.createDeclarations(10, "Gr-afsoner", "", "", "Hansen, Anne Marie");

        dataProducerUtils.createDeclarations(10, "Ambulant/Arrestant", "", "Dam, Iris Billeskov", "");

        dataProducerUtils.createDeclarations(10, "Benådningssag", "Harees, Farahna\t- lægekonsulent, speciallæge i psykiatri", "", "");

        List<NodeRef> closed = dataProducerUtils.createDeclarations(5, "Indlagt til observation", "Harees, Farahna\t- lægekonsulent, speciallæge i psykiatri", "", "");


        JSONObject jsonObject = new JSONObject();
        jsonObject.put("closed","true");

        Map<QName, Serializable> properties = JSONUtils.getMap(jsonObject);

        Iterator i = closed.iterator();

        while (i.hasNext()) {
            NodeRef nodeRef = (NodeRef)i.next();
            entryBean.updateEntry(nodeRef,properties);
        }

        closed = dataProducerUtils.createDeclarations(5, "Ambulant/Arrestant", "Harees, Farahna\t- lægekonsulent, speciallæge i psykiatri", "", "");


        jsonObject = new JSONObject();
        jsonObject.put("closed","true");

        properties = JSONUtils.getMap(jsonObject);

        i = closed.iterator();

        while (i.hasNext()) {
            NodeRef nodeRef = (NodeRef)i.next();
            entryBean.updateEntry(nodeRef,properties);
        }




        // postive hits

        dataProducerUtils.createDeclarations(5, "Indlagt til observation", "", "", "Hansen, Anne Marie");
        dataProducerUtils.createDeclarations(8, "Indlagt til observation", "Harees, Farahna\t- lægekonsulent, speciallæge i psykiatri", "", "");


        System.out.println("sleep");
        TimeUnit.SECONDS.sleep(20); // make sure the solr indexed the new declaration
        System.out.println("wake up");

    }

    public void createDeclarationsForStateArrestanterTest() throws JSONException, InterruptedException {

        // I området ”Arrestanter” findes de sager som har Status enten Ambulant/Arrestant eller Ambulant/Surrogatbehandling

        // negative hits

        dataProducerUtils.createDeclarations(10, "Gr-afsoner", "Harees, Farahna\t- lægekonsulent, speciallæge i psykiatri", "", "");
        dataProducerUtils.createDeclarations(10, "Gr-afsoner", "", "Dam, Iris Billeskov", "");
        dataProducerUtils.createDeclarations(10, "Gr-afsoner", "", "", "Hansen, Anne Marie");

        dataProducerUtils.createDeclarations(10, "Indlagt til observation", "", "Dam, Iris Billeskov", "");

        dataProducerUtils.createDeclarations(10, "Benådningssag", "Harees, Farahna\t- lægekonsulent, speciallæge i psykiatri", "", "");

        List<NodeRef> closed = dataProducerUtils.createDeclarations(15, "Ambulant/Arrestant", "Harees, Farahna\t- lægekonsulent, speciallæge i psykiatri", "", "");


        JSONObject jsonObject = new JSONObject();
        jsonObject.put("closed","true");

        Map<QName, Serializable> properties = JSONUtils.getMap(jsonObject);

        Iterator i = closed.iterator();

        while (i.hasNext()) {
            NodeRef nodeRef = (NodeRef)i.next();
            entryBean.updateEntry(nodeRef,properties);
        }



        // postive hits

        dataProducerUtils.createDeclarations(10, "Ambulant/Arrestant", "", "", "Hansen, Anne Marie");
        dataProducerUtils.createDeclarations(10, "Ambulant/surrogatanbragt", "Harees, Farahna\t- lægekonsulent, speciallæge i psykiatri", "", "");


        System.out.println("sleep");
        TimeUnit.SECONDS.sleep(20); // make sure the solr indexed the new declaration
        System.out.println("wake up");

    }

    public void createDeclarationsForOngoingTest() throws JSONException {

        // I området ”Igangværende” findes de sager som har fået tildelt enten en psykolog, læge eller socialrådgiver, og som ikke har Status enten Ambulant/Arrestant,
        // Ambulant/Surrogatbehandling, Indlagt til observation, eller Status der begynder med ”GR-”

        // negative hits

        dataProducerUtils.createDeclarations(10, "Gr-afsoner", "Harees, Farahna\t- lægekonsulent, speciallæge i psykiatri", "", "");
        dataProducerUtils.createDeclarations(10, "Gr-afsoner", "", "Dam, Iris Billeskov", "");
        dataProducerUtils.createDeclarations(10, "Gr-afsoner", "", "", "Hansen, Anne Marie");

        dataProducerUtils.createDeclarations(10, "Ambulant/surrogatanbragt", "Harees, Farahna\t- lægekonsulent, speciallæge i psykiatri", "", "");
        dataProducerUtils.createDeclarations(10, "Indlagt til observation", "", "Dam, Iris Billeskov", "");
        dataProducerUtils.createDeclarations(10, "Ambulant/Arrestant", "", "", "Hansen, Anne Marie");

        dataProducerUtils.createDeclarations(10, "Ambulant/surrogatanbragt", "Harees, Farahna\t- lægekonsulent, speciallæge i psykiatri", "Dam, Iris Billeskov", "");
        dataProducerUtils.createDeclarations(10, "Indlagt til observation", "Harees, Farahna\t- lægekonsulent, speciallæge i psykiatri", "Dam, Iris Billeskov", "");
        dataProducerUtils.createDeclarations(10, "Ambulant/arrestant", "Harees, Farahna\t- lægekonsulent, speciallæge i psykiatri", "Dam, Iris Billeskov", "Hansen, Anne Marie");

        List<NodeRef> closed = dataProducerUtils.createDeclarations(5, "Ambulant", "Harees, Farahna\t- lægekonsulent, speciallæge i psykiatri", "", "");


        JSONObject jsonObject = new JSONObject();
        jsonObject.put("closed","true");

        Map<QName, Serializable> properties = JSONUtils.getMap(jsonObject);

        Iterator i = closed.iterator();

        while (i.hasNext()) {
            NodeRef nodeRef = (NodeRef)i.next();
            entryBean.updateEntry(nodeRef,properties);
        }


        // positive hits

        dataProducerUtils.createDeclarations(10, "Ambulant", "Harees, Farahna\t- lægekonsulent, speciallæge i psykiatri", "", "");
        dataProducerUtils.createDeclarations(10, "Afsoner", "", "Dam, Iris Billeskov", "");
        dataProducerUtils.createDeclarations(10, "Torturundersøgelse", "", "", "Hansen, Anne Marie");

        dataProducerUtils.createDeclarations(10, "Benådningssag", "Harees, Farahna\t- lægekonsulent, speciallæge i psykiatri", "", "");
        dataProducerUtils.createDeclarations(10, "Ambulant", "", "Dam, Iris Billeskov", "");
        dataProducerUtils.createDeclarations(10, "Torturundersøgelse", "", "Dam, Iris Billeskov", "Hansen, Anne Marie");




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





}
