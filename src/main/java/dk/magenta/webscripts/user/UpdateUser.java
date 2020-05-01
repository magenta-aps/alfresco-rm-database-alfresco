package dk.magenta.webscripts.user;

import dk.magenta.beans.PropertyValuesBean;
import dk.magenta.model.DatabaseModel;
import dk.magenta.utils.JSONUtils;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteService;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class UpdateUser extends AbstractWebScript {



    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public void setAuthenticationService(MutableAuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    PermissionService permissionService;

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    NodeService nodeService;


    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    private AuthorityService authorityService;

    private boolean isMember() {

        String currentUser = authenticationService.getCurrentUserName();

        Set<String> auths = authorityService.getAuthoritiesForUser(currentUser);
        Iterator<String> authIt = auths.iterator();
        while (authIt.hasNext()){
            String group = authIt.next();
            if (group.equals("GROUP_site_retspsyk_SiteRoleManager")) {
                return true;
            }
        }
        return false;
    }

    MutableAuthenticationService authenticationService;
    private PersonService personService;

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    SiteService siteService;


    @Override
    public void execute(WebScriptRequest webScriptRequest, WebScriptResponse webScriptResponse) throws IOException {

        String currentUserName = authenticationService.getCurrentUserName();

        AuthenticationUtil.setRunAsUserSystem();

        webScriptResponse.setContentEncoding("UTF-8");
        Writer webScriptWriter = webScriptResponse.getWriter();
        JSONObject result = new JSONObject();

        Map<String, String> params = JSONUtils.parseParameters(webScriptRequest.getURL());

        String method = params.get("method");

        switch (method) {
            case "getUserType":

                String userName = params.get("userName");

                if (userName.equals(DatabaseModel.USER_CURRENT)) {
                    userName = currentUserName;
                }

                NodeRef p = personService.getPerson(userName);

                try {
                    result.put("result", nodeService.hasAspect(p, DatabaseModel.ASPECT_BUA_USER));
                    JSONUtils.write(webScriptWriter, result);
                }
                catch (JSONException j) {
                    j.printStackTrace();
                    System.out.println(j.getMessage());
                }
                break;

            case "update":

                if (isMember() || currentUserName.equals("admin")) {

                    userName = params.get("userName");

                    boolean bua = new Boolean(params.get("bua"));

                    p = personService.getPerson(userName);

                    if (bua) {
                        nodeService.addAspect(p, DatabaseModel.ASPECT_BUA_USER, null);
                    }
                    else {
                        nodeService.removeAspect(p, DatabaseModel.ASPECT_BUA_USER);
                    }

                    AuthenticationUtil.clearCurrentSecurityContext();

                    result = JSONUtils.getSuccess();
                    JSONUtils.write(webScriptWriter, result);
                }
        }
    }


}
