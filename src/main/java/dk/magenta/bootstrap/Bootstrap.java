package dk.magenta.bootstrap;

import dk.magenta.beans.DatabaseBean;
import dk.magenta.beans.EntryBean;
import dk.magenta.beans.PropertyValuesBean;
import dk.magenta.model.DatabaseModel;
import dk.magenta.utils.JSONUtils;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.*;

public class Bootstrap extends AbstractLifecycleBean {

    private PropertyValuesBean propertyValuesBean;

    public void setPropertyValuesBean(PropertyValuesBean propertyValuesBean) {
        this.propertyValuesBean = propertyValuesBean;
    }

    private EntryBean entryBean;
    public void setEntryBean(EntryBean entryBean) {
        this.entryBean = entryBean;
    }




    private SiteService siteService;

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    private PermissionService permissionService;

    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    protected void onBootstrap(ApplicationEvent applicationEvent) {

        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

//        //Setup templateFolder
//        NodeRef templateLibrary = siteService.getContainer("retspsyk", DatabaseModel.PROP_TEMPLATE_LIBRARY);
////        System.out.println("template noderef" + templateLibrary);
//        String templateFolderManager = "GROUP_site_" + "retspsyk" + "_TemplateFolderValueManager";
//        permissionService.setInheritParentPermissions(templateLibrary, false);
//        permissionService.setPermission(templateLibrary, templateFolderManager, DatabaseModel.Permission_SiteTemplateManager, true);


/*
        NodeRef retspsyk = siteService.getSite("retspsyk").getNodeRef();

        String baseAuth = "GROUP_site_retspsyk_";

        String roleManager = "SiteRoleManager";
        String entryLockManager = "SiteEntryLockManager";
        String propertyValueManager = "SitePropertyValueManager";

        permissionService.setPermission(retspsyk, baseAuth + roleManager, roleManager, true);
        permissionService.setPermission(retspsyk, baseAuth + entryLockManager, entryLockManager, true);
        permissionService.setPermission(retspsyk, baseAuth + propertyValueManager, propertyValueManager, true);
*/
        // Load property values

        System.out.println("starting bootstrap");

        try {
            List<SiteInfo> siteInfos = siteService.findSites("", 0);
            for(SiteInfo s: siteInfos)
                propertyValuesBean.loadPropertyValues(s.getShortName());
        } catch (JSONException | FileNotFoundException | IOException e) {
            e.printStackTrace();
        }

         //this.createDeclarations(1000);
    }

    @Override
    protected void onShutdown(ApplicationEvent applicationEvent) {
        // do nothing
    }

    private void createDeclarations(int number) {

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
                jsonProperties.put("doctor", "Dr. No");
                jsonProperties.put("psychologist", "Dr. Yes");
                jsonProperties.put("mainDiagnosis", diagnosis.get(r.nextInt(1000)));
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


}