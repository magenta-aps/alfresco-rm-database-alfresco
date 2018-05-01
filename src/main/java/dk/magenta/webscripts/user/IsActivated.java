package dk.magenta.webscripts.user;

import dk.magenta.beans.PropertyValuesBean;
import dk.magenta.utils.JSONUtils;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
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

public class IsActivated extends AbstractWebScript {

    private PropertyValuesBean propertyValuesBean;

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public void setAuthenticationService(MutableAuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }


    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    private AuthorityService authorityService;

    private boolean isMember(String user) throws JSONException {
        Set<String> auths = authorityService.getAuthoritiesForUser(user);

        Iterator<String> authIt = auths.iterator();
        while (authIt.hasNext()){
            String group = authIt.next();
            if (group.equals("GROUP_site_retspsyk")) {
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

        Map<String, String> params = JSONUtils.parseParameters(webScriptRequest.getURL());
        String userName = params.get("userName");


                webScriptResponse.setContentEncoding("UTF-8");
                Writer webScriptWriter = webScriptResponse.getWriter();
                JSONObject result = new JSONObject();

                try {
                    result.put("member", isMember(userName));
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                 AuthenticationUtil.clearCurrentSecurityContext();

                 JSONUtils.write(webScriptWriter, result);
            }

}




// F.eks. curl -i -u admin:admin -X GET 'http://localhost:8080/alfresco/s/propertyValues?siteShortName=retspsyk'

//curl -i -u admin:admin -X  -F 'img_avatar=@/home/petehouston/hello.txt' http://localhost/upload'