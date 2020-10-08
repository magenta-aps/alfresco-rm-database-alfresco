package dk.magenta.beans;

import dk.magenta.model.DatabaseModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.transform.ContentTransformer;
import org.alfresco.service.cmr.repository.*;

import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.*;

import static dk.magenta.model.DatabaseModel.GROUP_ALLOWEDTODELETE;

public class ScriptBean {

    private NodeService nodeService;

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    private SiteService siteService;

    private AuthorityService authorityService;

    private String siteShortName = "Retspsyk";

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    private PersonService personService;

    public void setPermissonService(PermissionService permissonService) {
        this.permissonService = permissonService;
    }

    private PermissionService permissonService;

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    private ContentService contentService;

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public List<NodeRef> getList() {
        return list;
    }

    private List<NodeRef> list = new ArrayList<>();


    public void traverse(NodeRef root) {

        list = new ArrayList<>();

        List<ChildAssociationRef> children = nodeService.getChildAssocs(root);

        if (children.size() == 0) {
            // handle the node, if it meets the criteria
            if (nodeService.getType(root).equals(ContentModel.TYPE_CONTENT) || nodeService.getType(root).equals(ContentModel.TYPE_FOLDER)) {

                Set<AccessPermission> permissions = permissonService.getAllSetPermissions(root);
                Iterator i = permissions.iterator();
                boolean found = false;

                while (i.hasNext()) {
                    AccessPermission accessPermission = (AccessPermission) i.next();
                    if (accessPermission.getPermission().equals("DeleteNode") && (accessPermission.getAuthority().equals(GROUP_ALLOWEDTODELETE))) {
                        found = true;
                    }
                }

                // we need to fix the missing permisson
                if (!found) {
                    list.add(root);
                }
            }
        }
        else {
            for (int i = 0; i <= children.size() - 1; i++) {
                ChildAssociationRef c = children.get(i);
                this.traverse(c.getChildRef());
            }
        }

    }
}
