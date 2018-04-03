package dk.magenta.webscripts.user;

import dk.magenta.beans.PropertyValuesBean;
import dk.magenta.utils.JSONUtils;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;

import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;

public class CreateUser extends AbstractWebScript {

    private PropertyValuesBean propertyValuesBean;

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public void setAuthenticationService(MutableAuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    MutableAuthenticationService authenticationService;
    private PersonService personService;


    @Override
    public void execute(WebScriptRequest webScriptRequest, WebScriptResponse webScriptResponse) throws IOException {


        String currentUser = authenticationService.getCurrentUserName();

        webScriptResponse.setContentEncoding("UTF-8");
        Writer webScriptWriter = webScriptResponse.getWriter();
        JSONObject result;

        if (currentUser.equals("BSK_001")) {

            Map<String, String> params = JSONUtils.parseParameters(webScriptRequest.getURL());




            String userName = params.get("userName");
            String password = params.get("password");
            String firstName = params.get("firstName");
            String lastName = params.get("password");
            String email = params.get("email");


            AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

            authenticationService.createAuthentication(userName,
                    password.toCharArray());
            authenticationService.setAuthenticationEnabled(userName, true);

            Map<QName, Serializable> user = new HashMap<>();
            user.put(ContentModel.PROP_USERNAME, userName);
            user.put(ContentModel.PROP_FIRSTNAME, firstName);
            user.put(ContentModel.PROP_LASTNAME, lastName);
            user.put(ContentModel.PROP_EMAIL, email);

            NodeRef person = personService.createPerson(user);
            System.out.println("person " + person.toString());

            try {
                result = JSONUtils.getSuccess();

            } catch (Exception e) {
                e.printStackTrace();
                result = JSONUtils.getError(e);
                webScriptResponse.setStatus(400);
            }
                JSONUtils.write(webScriptWriter, result);
        }
        else {
             result = JSONUtils.getError(new Exception("run as wrong user"));
             JSONUtils.write(webScriptWriter, result);
        }

    }
}




// F.eks. curl -i -u admin:admin -X GET 'http://localhost:8080/alfresco/s/propertyValues?siteShortName=retspsyk'

//curl -i -u admin:admin -X  -F 'img_avatar=@/home/petehouston/hello.txt' http://localhost/upload'