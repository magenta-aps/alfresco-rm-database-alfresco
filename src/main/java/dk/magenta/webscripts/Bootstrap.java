package dk.magenta.webscripts;



import dk.magenta.conf.DefaultRoles;
import dk.magenta.conf.DefaultUsers;
import dk.magenta.conf.DropDownTestContentsConf;
import dk.magenta.model.DatabaseModel;
import org.activiti.engine.impl.bpmn.data.Data;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.security.*;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.util.PropertyMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationEvent;

import org.springframework.extensions.surf.util.AbstractLifecycleBean;
import dk.magenta.conf.DropDownConf;
import sun.text.resources.th.BreakIteratorInfo_th;

import java.util.Iterator;
import java.util.List;

public class Bootstrap extends AbstractLifecycleBean {

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    private SiteService siteService;
    private AuthorityService authorityService;
    private MutableAuthenticationService authenticationService;



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



    private DropDownConf dropDownConf = new DropDownConf();




    protected void onBootstrap(ApplicationEvent applicationEvent) {

        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();


        DropDownTestContentsConf dropDownTestContentsConf = new DropDownTestContentsConf();



        // load sites

        SiteInfo psycDec = siteService.getSite(DatabaseModel.TYPE_PSYC_DEC_SITE);

        if (psycDec == null) {

            SiteInfo site = siteService.createSite("site-dashboard", "retspsyk", "retspsyk", "container for retspsyk cases", SiteVisibility.PUBLIC);
            siteService.createContainer("retspsyk", DatabaseModel.DOC_LIBRARY, ContentModel.TYPE_FOLDER, null);

            System.out.println("created site: ");
            System.out.println(site);
        }



        // load dropdown groups

        Iterator i = dropDownConf.groups_to_bootstrap.iterator();

        while (i.hasNext()) {
            DropDownConf.Dropdown dropdown = (DropDownConf.Dropdown)i.next();
            String groupName = dropdown.getName();

            if (!authorityService.authorityExists("GROUP_" + groupName)) {
                authorityService.createAuthority(AuthorityType.GROUP, groupName, groupName, null);
                System.out.println("Bootstrapped group: " + groupName);
            }
        }


        // populate dropdown groups

        JSONArray testdata = dropDownTestContentsConf.getTestdata_to_bootstrap();

        try {
            for (int j=0;j<= testdata.length()-1;j++) {
                JSONObject group = testdata.getJSONObject(j);

                String groupName = group.getString("name");



                JSONArray entites = group.getJSONArray("entities");

                for (int k=0; k<=entites.length()-1;k++) {

                    String entity = entites.getString(k);
                    if (!authorityService.authorityExists("GROUP_" + entity)) {
                        String authName = authorityService.createAuthority(AuthorityType.GROUP, entity, entity, null);
                        authorityService.addAuthority("GROUP_" + groupName, authName);
                    }
                    else {
                        try {
                            authorityService.addAuthority("GROUP_" + groupName, "GROUP_" + entity);
                        }
                        catch (DuplicateChildNodeNameException e) {
                           // do nothing, caught as we reuse subgroups in different parentgroups
                        }
                    }
                }
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        // create default groups for roles
        List<String> roles_to_bootstrap = new DefaultRoles().getRolesForBootstrapping();
        Iterator j = roles_to_bootstrap.iterator();

        while (j.hasNext()) {

            String role = (String)j.next();
            if (!authorityService.authorityExists("GROUP_" + role)) {
                String auth = authorityService.createAuthority(AuthorityType.GROUP, role, role, null);
                System.out.println("bootstrapped role: " + auth);
            }
        }




        // bootstrap default users

        List<String> users_to_bootstrap = new DefaultUsers().getUsersForBootstrapping();
        Iterator u = users_to_bootstrap.iterator();

        while (u.hasNext()) {

            String name = (String)u.next();

            if (this.authenticationService.authenticationExists(name) == false) {

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
    }

    @Override
    protected void onShutdown(ApplicationEvent applicationEvent) {
        // do nothing
    }


}