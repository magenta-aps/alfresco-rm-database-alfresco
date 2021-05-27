package dk.magenta.scheduledjob;

import dk.magenta.beans.ReportWaitingTimeBean;
import org.alfresco.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WaitReportScheduledJobExecuter {
    private static final Logger LOG = LoggerFactory.getLogger(WaitReportScheduledJobExecuter.class);

    /**
     * Public API access
     */
    private ServiceRegistry serviceRegistry;


    public void setReportWaitingTimeBean(ReportWaitingTimeBean reportWaitingTimeBean) {
        this.reportWaitingTimeBean = reportWaitingTimeBean;
    }

    private ReportWaitingTimeBean reportWaitingTimeBean;

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    /**
     * Executer implementation
     */
    public void execute() {
        LOG.info("Running the scheduled job");

        System.out.println("godow fra waitreport");

        try {
            reportWaitingTimeBean.sendMail();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
