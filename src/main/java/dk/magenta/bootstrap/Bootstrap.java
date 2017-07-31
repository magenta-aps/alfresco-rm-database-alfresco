package dk.magenta.bootstrap;

import dk.magenta.beans.PropertyValuesBean;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.json.JSONException;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

import java.io.IOException;
import java.util.List;

public class Bootstrap extends AbstractLifecycleBean {

    private PropertyValuesBean propertyValuesBean;

    public void setPropertyValuesBean(PropertyValuesBean propertyValuesBean) {
        this.propertyValuesBean = propertyValuesBean;
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

    }

    @Override
    protected void onShutdown(ApplicationEvent applicationEvent) {
        // do nothing
    }


}