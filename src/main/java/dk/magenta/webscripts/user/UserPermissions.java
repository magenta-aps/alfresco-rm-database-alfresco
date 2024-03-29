package dk.magenta.webscripts.user;

import com.google.gdata.data.Content;
import com.google.gdata.data.Person;
import dk.magenta.beans.PropertyValuesBean;
import dk.magenta.model.DatabaseModel;
import dk.magenta.utils.JSONUtils;

import org.alfresco.query.PagingResults;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import org.alfresco.model.ContentModel;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static dk.magenta.model.DatabaseModel.ASPECT_SIGNATUREADDEDTOUSER;


public class UserPermissions extends AbstractWebScript {

    private PropertyValuesBean propertyValuesBean;

    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    private AuthorityService authorityService;


    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }



    private PersonService personService;

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    private NodeService nodeService;

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    SiteService siteService;


    private JSONObject isMember(String user) throws JSONException {

        JSONObject o = new JSONObject();
        o.put("GROUP_site_retspsyk_SiteEntryLockManager",false);
        o.put("GROUP_site_retspsyk_SiteRoleManager",false);
        o.put("GROUP_site_retspsyk_SitePropertyValueManager",false);
        o.put("GROUP_site_retspsyk_TemplateFolderValueManager",false);
        o.put("GROUP_site_retspsyk_SiteConsumer",false);


        Set<String> auths = authorityService.getAuthoritiesForUser(user);
        Iterator<String> authIt = auths.iterator();
        while (authIt.hasNext()){
            o.put(authIt.next(), true);
        }

        return o;

    }


    @Override
    public void execute(WebScriptRequest webScriptRequest, WebScriptResponse webScriptResponse) throws IOException {

        webScriptResponse.setContentEncoding("UTF-8");
        Writer webScriptWriter = webScriptResponse.getWriter();
        JSONObject result;

        Map<String, String> params = JSONUtils.parseParameters(webScriptRequest.getURL());




        String user = params.get("user");



        String onlyActivate = params.get("onlyActivate");



        NodeRef peopleContainer = personService.getPeopleContainer();

        List<ChildAssociationRef> allPeople = nodeService.getChildAssocs(peopleContainer);

        Iterator i = allPeople.iterator();
        JSONArray array = new JSONArray();

        AuthenticationUtil.setRunAsUserSystem();
        while (i.hasNext()) {

            NodeRef n = ((ChildAssociationRef)i.next()).getChildRef();

            String name = (String)nodeService.getProperty(n, ContentModel.PROP_USERNAME);

            boolean member =  siteService.isMember("retspsyk", (String)nodeService.getProperty(n, ContentModel.PROP_USERNAME));

            JSONObject o = new JSONObject();

            PersonService.PersonInfo authorityName = personService.getPerson(n);

            try {

                boolean go = false;

                if (user.equals(DatabaseModel.USER_ALL)) {
                    go = true;
                }
                else if (user.equals(DatabaseModel.USER_ONLY_BUA)) {
                    go = nodeService.hasAspect(n, DatabaseModel.ASPECT_BUA_USER);
                }
                else if (user.equals(DatabaseModel.USER_ONLY_PS)) {
                    go = !(nodeService.hasAspect(n, DatabaseModel.ASPECT_BUA_USER));
                }

                if (go) {

                    o.put("nodeRef", n);
                    o.put("userName", name);
                    o.put("firstName", (String) nodeService.getProperty(n, ContentModel.PROP_FIRSTNAME));
                    o.put("lastName", (String) nodeService.getProperty(n, ContentModel.PROP_LASTNAME));
                    o.put("active", member);
                    o.put("signatureAdded", nodeService.hasAspect(n, ASPECT_SIGNATUREADDEDTOUSER) ? "(Signatur)" : "");



                    if (nodeService.hasAspect(n, DatabaseModel.ASPECT_BUA_USER)) {
                        o.put("bua", "(BUA)");
                    }
                    else {
                        o.put("bua", "");
                    }

                    NodeRef homefolder =  (NodeRef)nodeService.getProperty(n, ContentModel.PROP_HOMEFOLDER);
                    if (homefolder != null) {
                        o.put("oprettet", nodeService.getProperty(homefolder, ContentModel.PROP_CREATED));
                    }

                    JSONObject membership = isMember(authorityName.getUserName());

                    o.put("GROUP_site_retspsyk_SiteEntryLockManager", membership.getBoolean("GROUP_site_retspsyk_SiteEntryLockManager"));
                    o.put("GROUP_site_retspsyk_SitePropertyValueManager", membership.getBoolean("GROUP_site_retspsyk_SitePropertyValueManager"));
                    o.put("GROUP_site_retspsyk_SiteRoleManager", membership.getBoolean("GROUP_site_retspsyk_SiteRoleManager"));
                    o.put("GROUP_site_retspsyk_TemplateFolderValueManager", membership.getBoolean("GROUP_site_retspsyk_TemplateFolderValueManager"));
                    o.put("GROUP_site_retspsyk_SiteConsumer", membership.getBoolean("GROUP_site_retspsyk_SiteConsumer"));

                    // filter the active/nonactive
                    if (onlyActivate.equals("true")) {
                        if (o.get("active").equals(true)) {
                            array.put(o);
                        }
                    }
                    else {
                        array.put(o);
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        AuthenticationUtil.clearCurrentSecurityContext();
        JSONUtils.write(webScriptWriter, array);
    }
}




// F.eks. curl -i -u admin:admin -X GET 'http://localhost:8080/alfresco/s/propertyValues?siteShortName=retspsyk'

//curl -i -u admin:admin -X  -F 'img_avatar=@/home/petehouston/hello.txt' http://localhost/upload'
