package dk.magenta.scheduledjob;

import dk.magenta.beans.TransformBean;
import dk.magenta.beans.UserBean;
import org.alfresco.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;

public class USRexpireScheduledJobExecuter {
    private static final Logger LOG = LoggerFactory.getLogger(USRexpireScheduledJobExecuter.class);

    /**
     * Public API access
     */
    private ServiceRegistry serviceRegistry;

    public void setUserBean(UserBean userBean) {
        this.userBean = userBean;
    }

    private UserBean userBean;


    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    /**
     * Executer implementation
     */
    public void execute() {
        LOG.info("Running the scheduled job");

        try {
            userBean.deactivateExpUsers();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        System.out.println("cleanup expired users");

    }


}



