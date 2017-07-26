package dk.magenta.bootstrap;

import dk.magenta.beans.PropertyValuesBean;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.json.JSONException;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

import java.io.IOException;

public class Bootstrap extends AbstractLifecycleBean {

    private PropertyValuesBean propertyValuesBean;

    public void setPropertyValuesBean(PropertyValuesBean propertyValuesBean) {
        this.propertyValuesBean = propertyValuesBean;
    }

    protected void onBootstrap(ApplicationEvent applicationEvent) {

        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

        // load properties folder

        try {
            propertyValuesBean.loadPropertiesFolder();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // Load property values

        try {
            propertyValuesBean.loadPropertyValues();
        } catch (JSONException | FileNotFoundException | IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onShutdown(ApplicationEvent applicationEvent) {
        // do nothing
    }


}