package dk.magenta.beans;


import dk.magenta.model.DatabaseModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.io.IOUtils;
import org.codehaus.groovy.transform.SourceURIASTTransformation;
import org.odftoolkit.simple.TextDocument;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static dk.magenta.model.DatabaseModel.ASPECT_EXPIRYUSER;
import static dk.magenta.model.DatabaseModel.PROP_EXPIRYDATE;

public class UserBean {


    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    SiteService siteService;

    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    AuthorityService authorityService;

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    PersonService personService;

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    NodeService nodeService;

    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    private TransactionService transactionService;

    public boolean deactivateExpUsers() throws ParseException {

        return transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

            NodeRef container = personService.getPeopleContainer();

            List<ChildAssociationRef> people = nodeService.getChildAssocs(container);

            Iterator i = people.iterator();
            Date now = new Date();



            while (i.hasNext()) {
                NodeRef user = ((ChildAssociationRef)i.next()).getChildRef();
    //            System.out.println(user);

                if (nodeService.hasAspect(user, ASPECT_EXPIRYUSER)) {

                    String d = (String) nodeService.getProperty(user, PROP_EXPIRYDATE);

                    if ( d != null) {
                        Date expires = new SimpleDateFormat("yyyy-MM-dd").parse(d);

                        if (now.after(expires)) {
                            System.out.println("you need to be deactivated");
                            this.deactivateUser((String)nodeService.getProperty(user,ContentModel.PROP_USERNAME));
                            nodeService.removeAspect(user, ASPECT_EXPIRYUSER);
                        }
                    }
                }
            }
            return true;
        });
    }

    public void deactivateUser (String userName) {

        AuthenticationUtil.setRunAsUserSystem();

        siteService.removeMembership("retspsyk", userName);
        authorityService.removeAuthority(DatabaseModel.GROUP_ALLOWEDTODELETE, userName);
        authorityService.removeAuthority(DatabaseModel.GROUP_TEMPLATEFOLDERVALUEMANAGER, userName);
        authorityService.removeAuthority(DatabaseModel.GROUP_SITEENTRYLOCKMANAGER, userName);
        authorityService.removeAuthority(DatabaseModel.GROUP_SITEPROPERTYVALUEMANAGER, userName);
        authorityService.removeAuthority(DatabaseModel.GROUP_SITEROLEMANAGER, userName);

        AuthenticationUtil.clearCurrentSecurityContext();

    }

}


// todo - teste at det kan køre i et alf-cron job - at det ikke skal pakkes ind i en transaktion.
// test at du stadig kan deaktivere en bruger, efter refaktoring af koden til dette
