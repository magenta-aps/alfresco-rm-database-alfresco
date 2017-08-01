package dk.magenta.bootstrap;

import dk.magenta.beans.EntryBean;
import dk.magenta.beans.PropertyValuesBean;
import dk.magenta.model.DatabaseModel;
import dk.magenta.utils.JSONUtils;
import org.activiti.engine.impl.util.json.JSONArray;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;
import org.springframework.orm.jpa.vendor.Database;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.io.Serializable;
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

    protected void onBootstrap(ApplicationEvent applicationEvent) {

        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

        // Load property values

        try {
            List<SiteInfo> siteInfos = siteService.findSites("", 0);
            for(SiteInfo s: siteInfos)
                propertyValuesBean.loadPropertyValues(s.getShortName());
        } catch (JSONException | FileNotFoundException | IOException e) {
            e.printStackTrace();
        }

        // this.createDeclarations();

    }

    @Override
    protected void onShutdown(ApplicationEvent applicationEvent) {
        // do nothing
    }

    private  void createDeclarations() {


//
        JSONObject jsonProperties = new JSONObject();


        Random r = new Random();



        ArrayList fornavne = new ArrayList();
        fornavne.add("Hulda");
        fornavne.add("Wini");
        fornavne.add("Grehte");
        fornavne.add("Husissellda");
        fornavne.add("Ulla-pia");
        fornavne.add("Dolly");
        fornavne.add("Ingrid");
        fornavne.add("Lykke");
        fornavne.add("Majbrit");

        fornavne.add("Gunnar");
        fornavne.add("Kurt");
        fornavne.add("Finn");
        fornavne.add("Dennis");
        fornavne.add("Dan");
        fornavne.add("Tage");
        fornavne.add("Ronny");
        fornavne.add("Tonny");
        fornavne.add("Brian");

        ArrayList efternavne = new ArrayList();
        efternavne.add("Eskildsen");
        efternavne.add("Fischer");
        efternavne.add("Eskildsen");
        efternavne.add("Frost");
        efternavne.add("Hansen");
        efternavne.add("Hedegård");
        efternavne.add("Eskildsen");
        efternavne.add("Hermansen");
        efternavne.add("Hermansen");
        efternavne.add("Hermansen");
        efternavne.add("Hermansen");
        efternavne.add("Holm");





        JSONObject result;
        result = propertyValuesBean.getPropertyValues(DatabaseModel.TYPE_PSYC_SITENAME);
        System.out.println("crappowitch");

        for (int i = 1; i <= 100; i++) {

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


                jsonProperties.put("cprNumber", "200174" + (1000 + i));
                jsonProperties.put("firstName", fornavne.get(r.nextInt(17)));
                jsonProperties.put("lastName", efternavne.get(r.nextInt(8)));
                jsonProperties.put("fullName", jsonProperties.get("firstName") + " " + jsonProperties.get("lastName"));
                jsonProperties.put("address", "Singularisvej 12");
                jsonProperties.put("postbox", "2700");
                jsonProperties.put("city", "Assens");
                jsonProperties.put("etnicity", ethnicity.get(r.nextInt(5)));
                jsonProperties.put("motherEthnicity", ethnicity.get(r.nextInt(5)));
                jsonProperties.put("fatherEthnicity", ethnicity.get(r.nextInt(5)));
                jsonProperties.put("referingAgency", referingAgency.get(r.nextInt(5)));
                jsonProperties.put("mainCharge", mainCharge.get(r.nextInt(65)));
                jsonProperties.put("placement", placement.get(r.nextInt(13)));
                jsonProperties.put("sanctionProposal", sanctionProposal.get(r.nextInt(15)));
                jsonProperties.put("creationDate", "2011-05-20T00:00:00.000Z");
                jsonProperties.put("observationDate", "2011-02-20T00:00:00.000Z");
                jsonProperties.put("declarationDate", "2011-07-20T00:00:00.000Z");
                jsonProperties.put("forensicDoctorCouncil", "");
                jsonProperties.put("forensicDoctorCouncilText", "");
                jsonProperties.put("finalVerdict", finalVerdict.get(r.nextInt(15)));
                jsonProperties.put("doctor", "Dr. No");
                jsonProperties.put("psychologist", "Dr. Yes");
                jsonProperties.put("mainDiagnosis", diagnosis.get(r.nextInt(1000)));
                jsonProperties.put("biDiagnoses", "[\"" + diagnosis.get(r.nextInt(1000)) + "\"]");
                System.out.println(jsonProperties);

                Map<QName, Serializable> properties = JSONUtils.getMap(jsonProperties);
                NodeRef nodeRef = entryBean.addEntry(DatabaseModel.TYPE_PSYC_SITENAME, DatabaseModel.TYPE_PSYC_DEC, properties);


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


    }


}