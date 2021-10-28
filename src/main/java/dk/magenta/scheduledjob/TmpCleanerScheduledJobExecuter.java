package dk.magenta.scheduledjob;

import dk.magenta.beans.ReportWaitingTimeBean;
import dk.magenta.beans.TransformBean;
import dk.magenta.model.DatabaseModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.transaction.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

public class TmpCleanerScheduledJobExecuter {
    private static final Logger LOG = LoggerFactory.getLogger(TmpCleanerScheduledJobExecuter.class);

    /**
     * Public API access
     */
    private ServiceRegistry serviceRegistry;

    private TransformBean transformBean;


    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    /**
     * Executer implementation
     */
    public void execute() {
        LOG.info("Running the scheduled job");

        System.out.println("cleanup of the temp folder activated. Deleting nodes with the aspect rm:tmp");
        transformBean.cleanUpTMP();
    }

    public void setTransformBean(TransformBean transformBean) {
        this.transformBean = transformBean;
    }
}



