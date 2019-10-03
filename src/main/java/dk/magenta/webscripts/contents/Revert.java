

package dk.magenta.webscripts.contents;

import dk.magenta.model.DatabaseModel;
import dk.magenta.utils.JSONUtils;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.*;

public class Revert extends AbstractWebScript {


    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    public void setVersionService(VersionService versionService) {
        this.versionService = versionService;
    }

    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    private AuthorityService authorityService;

    private VersionService versionService;

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    private SiteService siteService;

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    private NodeService nodeService;

    private PermissionService permissionService;

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    private SearchService searchService;


    private boolean isMember() {

        String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();

        Set<String> auths = authorityService.getAuthoritiesForUser(currentUser);
        Iterator<String> authIt = auths.iterator();
        while (authIt.hasNext()){
            String group = authIt.next();
            if (group.equals("GROUP_site_retspsyk_SiteEntryLockManager")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void execute(WebScriptRequest webScriptRequest, WebScriptResponse webScriptResponse) throws IOException {

        webScriptResponse.setContentEncoding("UTF-8");
        Content c = webScriptRequest.getContent();
        Writer webScriptWriter = webScriptResponse.getWriter();
        JSONObject result;

        JSONObject json = null;

         try {

                if (isMember()) {

                    json = new JSONObject(c.getContent());
                    System.out.println(json);

                    NodeRef n = new NodeRef((String) json.get("nodeRef"));
                    String version = (String) json.get("version");
                    VersionHistory versionHistory = versionService.getVersionHistory(n);

                    Version versionObject = versionHistory.getVersion(version);

//                    Map<String, Serializable> versionProperties = new HashMap<>();
//                    versionProperties.put(Version.PROP_DESCRIPTION, "reverted back to version: " + versionObject.getVersionLabel());

                    AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

                    versionService.revert(n, versionObject);

                    result = JSONUtils.getSuccess();
                    JSONUtils.write(webScriptWriter, result);
                }
                else {
                    result = JSONUtils.getObject("result","user not allowed to revert");
                    JSONUtils.write(webScriptWriter, result);
                }



            } catch (JSONException e) {
                e.printStackTrace();
            }
            catch (Exception e) {
                System.out.println("fail");
                e.printStackTrace();
            }

    }
}



