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
import java.util.Map;
import java.util.List;
import java.util.Random;

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

        this.createDeclaration();

    }

    @Override
    protected void onShutdown(ApplicationEvent applicationEvent) {
        // do nothing
    }

    private  void createDeclaration() {


//
        JSONObject jsonProperties = new JSONObject();


        Random r = new Random();
//





        JSONObject result;
        result = propertyValuesBean.getPropertyValues(DatabaseModel.TYPE_PSYC_DEC_SITE);
        System.out.println("crappowitch");
//
//
        for (int i = 1; i <= 1000; i++) {
//
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


                jsonProperties.put("cprNumber", ethnicity.get(r.nextInt(5)));
                jsonProperties.put("firstName", ethnicity.get(2));
                jsonProperties.put("lastName", ethnicity.get(2));
                jsonProperties.put("fullName", ethnicity.get(2));
                jsonProperties.put("address", ethnicity.get(2));
                jsonProperties.put("postbox", "8000");
                jsonProperties.put("city", ethnicity.get(2));
                jsonProperties.put("etnicity", ethnicity.get(r.nextInt(5)));
                jsonProperties.put("motherEthnicity", ethnicity.get(r.nextInt(5)));
                jsonProperties.put("fatherEthnicity", ethnicity.get(r.nextInt(5)));
                jsonProperties.put("referingAgency", referingAgency.get(5));
                jsonProperties.put("mainCharge", mainCharge.get(100));
                jsonProperties.put("placement", placement.get(2));
                jsonProperties.put("sanctionProposal", ethnicity.get(2));
                jsonProperties.put("observationDate", "2011-02-20T00:00:00.000Z");
                jsonProperties.put("declarationDate", "2011-07-20T00:00:00.000Z");
                jsonProperties.put("closedWithoutDeclarationSentTo", ethnicity.get(2));
                jsonProperties.put("forensicDoctorCouncil", ethnicity.get(2));
                jsonProperties.put("forensicDoctorCouncilText", ethnicity.get(2));
                jsonProperties.put("finalVerdict", ethnicity.get(2));
                jsonProperties.put("doctor", ethnicity.get(2));
                jsonProperties.put("psychologist", ethnicity.get(2));
                jsonProperties.put("mainDiagnosis", ethnicity.get(2));
                jsonProperties.put("biDiagnoses", ethnicity.get(2));
                System.out.println(jsonProperties);

                Map<QName, Serializable> properties = JSONUtils.getMap(jsonProperties);
                NodeRef nodeRef = entryBean.addEntry(DatabaseModel.TYPE_PSYC_DEC_SITE, DatabaseModel.TYPE_PSYC_DEC, properties);


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


    }


}