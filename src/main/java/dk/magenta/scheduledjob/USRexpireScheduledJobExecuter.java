package dk.magenta.scheduledjob;

import dk.magenta.beans.TransformBean;
import org.alfresco.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class USRexpireScheduledJobExecuter {
    private static final Logger LOG = LoggerFactory.getLogger(USRexpireScheduledJobExecuter.class);

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



