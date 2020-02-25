

package dk.magenta.webscripts.contents;

import dk.magenta.beans.ContentsBean;
import dk.magenta.model.DatabaseModel;
import dk.magenta.utils.JSONUtils;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.NamespaceService;
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

    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    private AuthenticationService authenticationService;

    private VersionService versionService;

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    private SiteService siteService;

    public void setContentsBean(ContentsBean contentsBean) {
        this.contentsBean = contentsBean;
    }

    private ContentsBean contentsBean;


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


                    NodeRef n = new NodeRef((String) json.get("nodeRef"));
                    String version = (String) json.get("version");
                    VersionHistory versionHistory = versionService.getVersionHistory(n);

                    Version versionObject = versionHistory.getVersion(version);

//                    Map<String, Serializable> versionProperties = new HashMap<>();
//                    versionProperties.put(Version.PROP_DESCRIPTION, "reverted back to version: " + versionObject.getVersionLabel());

                    String currentUser = authenticationService.getCurrentUserName();

                    AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

                    versionService.revert(n, versionObject);

                    nodeService.setProperty(n, ContentModel.PROP_MODIFIER, currentUser);


                    // update the versionHistory of the current version

                    String fileName = (String)nodeService.getProperty(n, ContentModel.PROP_NAME);

                    // Create new version node of earlier version
                    Map<String,  Serializable> properties = new HashMap<>();
                    properties.put(ContentModel.PROP_NAME.toString(), fileName);
                    Serializable content = nodeService.getProperty(n, ContentModel.PROP_CONTENT);
                    properties.put(ContentModel.PROP_CONTENT.toString(), content);
                    properties.put(ContentModel.PROP_MODIFIER.toString(), currentUser);
                    properties.put("modifier", currentUser);


                    versionService.createVersion(n,properties);












                    VersionHistory h = versionService.getVersionHistory(n);




                    contentsBean.getThumbnail(n.getId(), h.getHeadVersion().getFrozenStateNodeRef().getId(), true);


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



