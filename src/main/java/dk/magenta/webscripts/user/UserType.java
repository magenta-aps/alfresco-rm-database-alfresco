package dk.magenta.webscripts.user;

import dk.magenta.beans.PropertyValuesBean;
import dk.magenta.model.DatabaseModel;
import dk.magenta.utils.JSONUtils;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.*;
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

public class UserType extends AbstractWebScript {


    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    private PersonService personService;

    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    private AuthenticationService authenticationService;

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    private NodeService nodeService;



    @Override
    public void execute(WebScriptRequest webScriptRequest, WebScriptResponse webScriptResponse) throws IOException {

            webScriptResponse.setContentEncoding("UTF-8");
            Writer webScriptWriter = webScriptResponse.getWriter();
            JSONObject result = new JSONObject();

        Map<String, String> params = JSONUtils.parseParameters(webScriptRequest.getURL());



        System.out.println("user");
        System.out.println(authenticationService.getCurrentUserName());
        NodeRef p = personService.getPerson(authenticationService.getCurrentUserName());
        System.out.println(p);

        try {
            result.put("bua", nodeService.hasAspect(p,DatabaseModel.ASPECT_BUA_USER));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JSONUtils.write(webScriptWriter, result);
        }
}




// F.eks. curl -i -u admin:admin -X GET 'http://localhost:8080/alfresco/s/propertyValues?siteShortName=retspsyk'

//curl -i -u admin:admin -X  -F 'img_avatar=@/home/petehouston/hello.txt' http://localhost/upload'
