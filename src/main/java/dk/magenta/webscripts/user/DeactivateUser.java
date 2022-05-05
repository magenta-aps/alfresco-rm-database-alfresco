package dk.magenta.webscripts.user;

import dk.magenta.beans.PropertyValuesBean;
import dk.magenta.beans.UserBean;
import dk.magenta.model.DatabaseModel;
import dk.magenta.utils.JSONUtils;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class DeactivateUser extends AbstractWebScript {

    private PropertyValuesBean propertyValuesBean;

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public void setAuthenticationService(MutableAuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }


    public void setUserBean(UserBean userBean) {
        this.userBean = userBean;
    }

    private UserBean userBean;

    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    private AuthorityService authorityService;

    private boolean isMember() {

        String currentUser = authenticationService.getCurrentUserName();
        System.out.println("currentUser");
        System.out.println(currentUser);

        System.out.println("authorityService");
        System.out.println(authorityService);

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

        AuthenticationUtil.setRunAsUserSystem();

        if (isMember()) {

            webScriptResponse.setContentEncoding("UTF-8");
            Writer webScriptWriter = webScriptResponse.getWriter();
            JSONObject result;

            Map<String, String> params = JSONUtils.parseParameters(webScriptRequest.getURL());

            String userName = params.get("userName");

//            siteService.removeMembership("retspsyk", userName);
//            authorityService.removeAuthority(DatabaseModel.GROUP_ALLOWEDTODELETE, userName);
//            authorityService.removeAuthority(DatabaseModel.GROUP_TEMPLATEFOLDERVALUEMANAGER, userName);
//            authorityService.removeAuthority(DatabaseModel.GROUP_SITEENTRYLOCKMANAGER, userName);
//            authorityService.removeAuthority(DatabaseModel.GROUP_SITEPROPERTYVALUEMANAGER, userName);
//            authorityService.removeAuthority(DatabaseModel.GROUP_SITEROLEMANAGER, userName);

            userBean.deactivateUser(userName);


            AuthenticationUtil.clearCurrentSecurityContext();

            result = JSONUtils.getSuccess();
            JSONUtils.write(webScriptWriter, result);
        }

    }
}




// F.eks. curl -i -u admin:admin -X GET 'http://localhost:8080/alfresco/s/propertyValues?siteShortName=retspsyk'

//curl -i -u admin:admin -X  -F 'img_avatar=@/home/petehouston/hello.txt' http://localhost/upload'
