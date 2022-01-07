package dk.magenta.webscripts.entry;

import dk.magenta.beans.EntryBean;
import dk.magenta.utils.JSONUtils;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class UndoCloseCaseEntry extends AbstractWebScript {


    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    private AuthenticationService authenticationService;

    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    private AuthorityService authorityService;

    private EntryBean entryBean;

    public void setEntryBean(EntryBean entryBean) {
        this.entryBean = entryBean;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        System.out.println("hej fra undoclosecaseentry");

        Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
        Map<String, String> params = JSONUtils.parseParameters(req.getURL());

        String mode = params.get("mode");

        res.setContentEncoding("UTF-8");
        Writer webScriptWriter = res.getWriter();
        JSONObject result;

        try {
            String uuid = templateArgs.get("uuid");
            NodeRef nodeRef = entryBean.getNodeRef(uuid);

            // todo: check for permission - need to be able to unlock

            if (this.isAuthorized()) {
                entryBean.undoCloseCase(nodeRef);
                result = entryBean.toJSON(nodeRef);
            } else {
                result = JSONUtils.getError("permisson denied");
            }


        } catch (Exception e) {
            e.printStackTrace();
            result = JSONUtils.getError(e);
            res.setStatus(400);
        }
        JSONUtils.write(webScriptWriter, result);
    }

    private boolean isAuthorized() {

        String currentUser = authenticationService.getCurrentUserName();
        System.out.println(currentUser);

        AtomicBoolean access = new AtomicBoolean(false);

        AuthenticationUtil.runAs(() -> {

            Set<String> auths = authorityService.getAuthoritiesForUser(currentUser);
            Iterator<String> authIt = auths.iterator();
            while (authIt.hasNext()) {
                String group = authIt.next();
                System.out.println(group);
                if (group.equals("GROUP_site_retspsyk_SiteEntryLockManager")) {
                    AuthenticationUtil.clearCurrentSecurityContext();
                    access.set(true);
                }
            }
            return null;
        }, AuthenticationUtil.getSystemUserName());

        return access.get();
     }
}

// F.eks. curl -i -u admin:admin -X PUT 'http://localhost:8080/alfresco/s/entry/445644-4545-4564-8848-1849155/unlock'
