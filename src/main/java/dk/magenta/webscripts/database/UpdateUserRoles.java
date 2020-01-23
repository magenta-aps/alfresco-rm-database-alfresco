package dk.magenta.webscripts.database;

import dk.magenta.beans.DatabaseBean;
import dk.magenta.model.DatabaseModel;
import dk.magenta.utils.JSONUtils;
import org.alfresco.service.cmr.security.AuthorityService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import javax.faces.model.DataModel;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class UpdateUserRoles extends AbstractWebScript {

    private DatabaseBean databaseBean;

    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    private AuthorityService authorityService;


    public void setDatabaseBean(DatabaseBean databaseBean) {
        this.databaseBean = databaseBean;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();

        res.setContentEncoding("UTF-8");
        Content c = req.getContent();
        Writer webScriptWriter = res.getWriter();
        JSONObject result;

        try {
            JSONObject json = new JSONObject(c.getContent());
            String siteShortName = templateArgs.get("siteShortName");
            String username = templateArgs.get("username");

            JSONArray addGroups = JSONUtils.getArray(json, "addGroups");
            JSONArray removeGroups = JSONUtils.getArray(json, "removeGroups");
            System.out.println("addGroups");

            if (addGroups != null) {
                for (int i = 0; i <= addGroups.length()-1; i++) {

                    String o = addGroups.getString(i);
                    System.out.println("hvad er o");
                    System.out.println(o.equals("GROUP_site_retspsyk_SiteConsumer"));

                    // if added to GROUP_site_retspsyk_SiteConsumer (only readaccess)- stripe the delete rights for all documents and remove from user from the colaborator group
                    if (o.equals("GROUP_site_retspsyk_SiteConsumer")) {
                        authorityService.removeAuthority("GROUP_site_retspsyk_SiteCollaborator", username);
                        authorityService.removeAuthority(DatabaseModel.GROUP_ALLOWEDTODELETE, username);
                    }
                }
            }

            if (removeGroups != null) {
                for (int i = 0; i <= removeGroups.length()-1; i++) {

                    String o = removeGroups.getString(i);

                    // if removed from the siteConsumer group (only readaccess), make the user able to delete all documents again and add user to the colaborator group
                    if (o.equals("GROUP_site_retspsyk_SiteConsumer")) {

                        // added check, #32745 - as existing membership breaks the update

                        boolean member_allowedToDelete = false;
                        boolean member_site_retspsyk_SiteCollaborator = false;

                        authorityService.getAuthoritiesForUser(username);

                        Set<String> auths = authorityService.getAuthoritiesForUser(username);
                        Iterator<String> authIt = auths.iterator();
                        while (authIt.hasNext()){

                            String auth = authIt.next();

                            if (auth.equals(DatabaseModel.GROUP_ALLOWEDTODELETE)) {
                                member_allowedToDelete = true;
                            }
                            else if (auth.equals("GROUP_site_retspsyk_SiteCollaborator")) {
                                member_site_retspsyk_SiteCollaborator = true;
                            }
                        }

                        if (!member_allowedToDelete) {
                            authorityService.addAuthority(DatabaseModel.GROUP_ALLOWEDTODELETE, username);
                        }

                        if (!member_site_retspsyk_SiteCollaborator) {
                            authorityService.addAuthority("GROUP_site_retspsyk_SiteCollaborator", username);
                        }



                    }
                }
            }
            System.out.println("removeGroups");
            System.out.println(removeGroups);


            result = databaseBean.updateUserRoles(siteShortName, username, addGroups, removeGroups);

        } catch (Exception e) {
            e.printStackTrace();
            result = JSONUtils.getError(e);
            res.setStatus(400);
        }
        JSONUtils.write(webScriptWriter, result);
    }
}

// F.eks. curl -i -u admin:admin -X GET 'http://localhost:8080/alfresco/s/database/retspsyk/role'