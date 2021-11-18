package dk.magenta.beans;


import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PersonService;
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
import org.odftoolkit.simple.TextDocument;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class UserBean {


    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    PersonService personService;

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    NodeService nodeService;

    public void deactivateExpUsers() {

        NodeRef container = personService.getPeopleContainer();

        List<ChildAssociationRef> people = nodeService.getChildAssocs(container);

        Iterator i = people.iterator();

        while (i.hasNext()) {
            NodeRef user = ((ChildAssociationRef)i.next()).getChildRef();
            System.out.println(user);
        }


    }

}
