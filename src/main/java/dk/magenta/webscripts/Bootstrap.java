package dk.magenta.webscripts;



import dk.magenta.beans.PropertyValuesBean;
import dk.magenta.conf.DefaultRoles;
import dk.magenta.conf.DefaultUsers;
import dk.magenta.model.DatabaseModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.security.*;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.util.PropertyMap;
import org.json.JSONException;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

import java.io.IOException;
import java.util.List;

public class Bootstrap extends AbstractLifecycleBean {

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    private SiteService siteService;
    private AuthorityService authorityService;
    private MutableAuthenticationService authenticationService;
    private PropertyValuesBean propertyValuesBean;

    public void setAuthenticationService(AuthenticationService mutableAuthenticationService) {
        this.authenticationService = (MutableAuthenticationService)mutableAuthenticationService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    private PersonService personService;


    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    public void setPropertyValuesBean(PropertyValuesBean propertyValuesBean) {
        this.propertyValuesBean = propertyValuesBean;
    }


    protected void onBootstrap(ApplicationEvent applicationEvent) {

        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();


        // load sites

        SiteInfo psycDec = siteService.getSite(DatabaseModel.TYPE_PSYC_DEC_SITE);

        if (psycDec == null) {

            SiteInfo site = siteService.createSite("site-dashboard", "retspsyk", "retspsyk", "container for retspsyk cases", SiteVisibility.PUBLIC);
            siteService.createContainer("retspsyk", DatabaseModel.DOC_LIBRARY, ContentModel.TYPE_FOLDER, null);

            System.out.println("created site: ");
            System.out.println(site);
        }


        // create default groups for roles
        List<String> roles_to_bootstrap = new DefaultRoles().getRolesForBootstrapping();

        for (String role : roles_to_bootstrap) {

            if (!authorityService.authorityExists("GROUP_" + role)) {
                String auth = authorityService.createAuthority(AuthorityType.GROUP, role, role, null);
                System.out.println("bootstrapped role: " + auth);
            }
        }

        // bootstrap default users

        List<String> users_to_bootstrap = new DefaultUsers().getUsersForBootstrapping();

        for (String name : users_to_bootstrap) {

            if (!this.authenticationService.authenticationExists(name)) {

                this.authenticationService.createAuthentication(name, name.toCharArray());


                PropertyMap ppOne = new PropertyMap(4);
                ppOne.put(ContentModel.PROP_USERNAME, name);
                ppOne.put(ContentModel.PROP_FIRSTNAME, name);
                ppOne.put(ContentModel.PROP_LASTNAME, "Thomsen");
                ppOne.put(ContentModel.PROP_EMAIL, "email@email.com");
                ppOne.put(ContentModel.PROP_JOBTITLE, "jobTitle");

                personService.createPerson(ppOne);

                authorityService.addAuthority("GROUP_" + "site_retspsyk_SiteCollaborator", name);

                System.out.println("bootstrapped user: " + name);
            }
        }

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