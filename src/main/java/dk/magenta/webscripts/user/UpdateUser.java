package dk.magenta.webscripts.user;

import dk.magenta.beans.PropertyValuesBean;
import dk.magenta.model.DatabaseModel;
import dk.magenta.utils.JSONUtils;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.Content;
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

        System.out.println("webScriptRequest.getParameter(");
        System.out.println("webScriptRequest.getParameter(");
        System.out.println("webScriptRequest.getParameter(");
        System.out.println(webScriptRequest.getParameter("signature"));;





        Map<String, String> params = JSONUtils.parseParameters(webScriptRequest.getURL());

        String signature2 = params.get("signature");
        System.out.println("hvad er signature: linebreak?");
        System.out.println(signature2);


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
                    p = personService.getPerson(userName);

                    String signature = webScriptRequest.getParameter("signature");
                    System.out.println("hvad er signature: linebreak?");
                    System.out.println(signature);

                    // den bliver ikke gemt med korrekt encoding - mellemrum forsvinder

                    NodeRef templateLibrary = siteService.getContainer("retspsyk", DatabaseModel.PROP_SIGNATURE_LIBRARY);
                    if (signature != null) {

                        NodeRef nodeRef = nodeService.getChildByName(templateLibrary, ContentModel.ASSOC_CONTAINS, userName);

                        if (nodeRef != null) {
                            nodeService.setProperty(nodeRef, DatabaseModel.PROP_SIGNATURE, signature);
                        }
                        else {
                            Map<QName, Serializable> properties = new HashMap<>();
                            properties.put(ContentModel.PROP_NAME, userName);
                            properties.put(DatabaseModel.PROP_SIGNATURE, signature);
                            QName qName = QName.createQName(DatabaseModel.CONTENT_MODEL_URI, userName);
                            ChildAssociationRef childAssociationRef = nodeService.createNode(templateLibrary, ContentModel.ASSOC_CONTAINS, qName, DatabaseModel.TYPE_SIGNATURE, properties);
                            nodeService.setProperty(childAssociationRef.getChildRef(), DatabaseModel.PROP_SIGNATURE, signature);
                            nodeService.addAspect(childAssociationRef.getChildRef(), ContentModel.ASPECT_VERSIONABLE, null);
                        }
                    }


                    boolean bua = new Boolean(params.get("bua"));



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
