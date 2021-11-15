package dk.magenta.scheduledjob;

import dk.magenta.beans.ReportWaitingTimeBean;
import dk.magenta.beans.WeeklyStatBean;
import org.alfresco.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WaitReportScheduledJobExecuter {
    private static final Logger LOG = LoggerFactory.getLogger(WaitReportScheduledJobExecuter.class);

    /**
     * Public API access
     */
    private ServiceRegistry serviceRegistry;


    public void setWeeklyStatBean(WeeklyStatBean weeklyStatBean) {
        this.weeklyStatBean = weeklyStatBean;
    }

    private WeeklyStatBean weeklyStatBean;



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
            // #43768 change the report to be send to the akk. report

            weeklyStatBean.sendMailCurrentYear();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
