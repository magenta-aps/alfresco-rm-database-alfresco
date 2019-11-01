package dk.magenta.webscripts.flowchart;

import dk.magenta.beans.EntryBean;
import dk.magenta.beans.PropertyValuesBean;
import dk.magenta.model.DatabaseModel;
import dk.magenta.utils.JSONUtils;
import org.alfresco.model.ContentModel;
import org.alfresco.rad.test.AbstractAlfrescoIT;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
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

/**
 * Created by flemmingheidepedersen on 18/05/2018.
 */

public  class DataProducerHelper extends AbstractAlfrescoIT {

    private static Logger log = Logger.getLogger(DataProducerHelper.class);

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

    private org.json.JSONArray ethnicity;
    private org.json.JSONArray referingAgency;
    private org.json.JSONArray mainCharge;
    private org.json.JSONArray placement;
    private org.json.JSONArray sanctionProposal;
    private org.json.JSONArray diagnosis;
    private org.json.JSONArray finalVerdict;
    private org.json.JSONArray status;
    private org.json.JSONArray noDeclarationReason;

    public DataProducerHelper() throws JSONException {
        super();

        femaleNames.add("Mathilde");
        femaleNames.add("Clara");
        femaleNames.add("Mona");
        femaleNames.add("Kirstine");
        femaleNames.add("Ulla");
        femaleNames.add("Emma");
        femaleNames.add("Ingrid");
        femaleNames.add("Lykke");
        femaleNames.add("Majbrit");


        maleNames.add("Jens");
        maleNames.add("Kurt");
        maleNames.add("Mathias");
        maleNames.add("Dennis");
        maleNames.add("Dan");
        maleNames.add("Mogens");
        maleNames.add("Hans");
        maleNames.add("Niels");
        maleNames.add("Jan");


        firstNames.put(true, maleNames);
        firstNames.put(false, femaleNames);


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


        result = propertyValuesBean.getPropertyValues(DatabaseModel.TYPE_PSYC_SITENAME);



        ethnicity = result.getJSONArray("ethnicity");
        referingAgency = result.getJSONArray("referingAgency");
        mainCharge = result.getJSONArray("mainCharge");
        placement = result.getJSONArray("placement");
        sanctionProposal = result.getJSONArray("sanctionProposal");
        diagnosis = result.getJSONArray("diagnosis");
        finalVerdict = result.getJSONArray("finalVerdict");
        status = result.getJSONArray("status");
        noDeclarationReason = result.getJSONArray("noDeclarationReason");


    }


    public void unlock(NodeRef child) {
        List<ChildAssociationRef> children = nodeService.getChildAssocs(child);

        if (children.size() == 0) {

            if (lockService.isLocked(child)) {
                lockService.unlock(child);
            }
        }
        else {
            for (int i = 0; i <= children.size()-1;i++) {
                NodeRef subChild = children.get(i).getChildRef();
                unlock(subChild);
            }
        }
    }


    public List<NodeRef> createDeclarations(int number, String state, String doctor, String psychologist, String socialworker ) {
        List<NodeRef> returnlist = new ArrayList<>();

        JSONObject jsonProperties = new JSONObject();

        Random r = new Random();

        for (int i = 1; i <= number; i++) {

            try {

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


                Map<QName, Serializable> properties = JSONUtils.getMap(jsonProperties);
                NodeRef nodeRef = entryBean.addEntry(DatabaseModel.TYPE_PSYC_SITENAME, DatabaseModel.TYPE_PSYC_DEC, properties, false);

                returnlist.add(nodeRef);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return returnlist;
    }

    public String getRandomCPRString(boolean isMale) {
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

    public String[] getRandomDateStrings(int count) {
        String[] randomDates = new String[count];
        randomDates[0] = getRandomDateString(2005);
        for(int i=1; i<count; i++) {
            String[] split = randomDates[i - 1].split("-");
            randomDates[i] = getRandomDateString(Integer.parseInt(split[0]) + 1);
        }
        return randomDates;
    }

    public String getRandomDateString(Integer minimumYear) {
        Random r = new Random();
        String year = toTwoDigit(r.nextInt(3) + minimumYear);
        String month = toTwoDigit(r.nextInt(11) + 1);
        String day = toTwoDigit(r.nextInt(27) + 1);
        return year + "-" + month + "-" + day;
    }

    public String toTwoDigit(Integer i) {
        if(i < 10)
            return "0" + i;
        return i.toString();
    }


    public void wipeAllCases()  {

        NodeRef docLibRef = siteService.getContainer("retspsyk", SiteService.DOCUMENT_LIBRARY);

        // reset counter
        nodeService.setProperty(docLibRef, ContentModel.PROP_COUNTER, 0);

        this.unlock(docLibRef);

        List<ChildAssociationRef> children = nodeService.getChildAssocs(docLibRef);

        for (int i = 0; i <= children.size()-1;i++) {
            NodeRef subChild = children.get(i).getChildRef();
            nodeService.deleteNode(subChild);
        }
    }


}
