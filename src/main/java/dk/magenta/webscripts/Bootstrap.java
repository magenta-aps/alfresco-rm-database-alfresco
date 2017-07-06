package dk.magenta.webscripts;



import dk.magenta.conf.DropDownTestContentsConf;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationEvent;

import org.springframework.extensions.surf.util.AbstractLifecycleBean;
import dk.magenta.conf.DropDownConf;

import java.util.Iterator;

public class Bootstrap extends AbstractLifecycleBean {

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    private SiteService siteService;
    private AuthorityService authorityService;


    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }



    private DropDownConf dropDownConf = new DropDownConf();




    protected void onBootstrap(ApplicationEvent applicationEvent) {

        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();


        DropDownTestContentsConf dropDownTestContentsConf = new DropDownTestContentsConf();



        // load sites

        SiteInfo retspsyk = siteService.getSite("retspsyk");

        if (retspsyk == null) {

            SiteInfo site = siteService.createSite("site-dashboard", "retspsyk", "retspsyk", "container for retspsyk cases", SiteVisibility.PUBLIC);
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

        System.out.println(testdata);

        try {
            for (int j=0;j<= testdata.length();j++) {
                JSONObject group = testdata.getJSONObject(j);

                String groupName = group.getString("name");
                System.out.println("groupName");
                System.out.println(groupName);

                JSONArray entites = group.getJSONArray("entities");
                System.out.println("entities");
                System.out.println(entites);

                for (int k=0; k<=entites.length()-1;k++) {

                    String entity = entites.getString(k);
                    if (!authorityService.authorityExists("GROUP_" + entity)) {

                        String authName = authorityService.createAuthority(AuthorityType.GROUP, entity, entity, null);
                        System.out.println("authname");
                        System.out.println(authName);

                        System.out.println("groupName");
                        System.out.println(groupName);

                        authorityService.addAuthority("GROUP_" + groupName, authName);
                    }
                }
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onShutdown(ApplicationEvent applicationEvent) {
        // do nothing
    }


}
