package dk.magenta;

import dk.magenta.utils.AuthorityUtils;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.transaction.TransactionService;

public class TestUtils {

    public static final String ADMIN = "admin";

    public static final String SITE_ONE = "reserved_for_test_site_one";

    public static SiteInfo createSite(SiteService siteService, String siteShortName){
        return createSite(siteService, siteShortName, SiteVisibility.PUBLIC);
    }

    private static SiteInfo createSite(SiteService siteService, String siteShortName, SiteVisibility siteVisibility) {
        SiteInfo s = siteService.createSite("site-dashboard", siteShortName, siteShortName, "desc", siteVisibility);
        siteService.createContainer(siteShortName, SiteService.DOCUMENT_LIBRARY, ContentModel.TYPE_FOLDER, null);
        return s;
    }

    public static Boolean deleteSite(SiteService siteService,
                                     AuthorityService authorityService, String siteShortName) {
            if(siteService.hasSite(siteShortName))
                siteService.deleteSite(siteShortName);

            String authority = AuthorityUtils.getAuthorityName(siteShortName, "");
            if (authorityService.authorityExists(authority))
                authorityService.deleteAuthority(authority, true);
            return true;
    }

}
