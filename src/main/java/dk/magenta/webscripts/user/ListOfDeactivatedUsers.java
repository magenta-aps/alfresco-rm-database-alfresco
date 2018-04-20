package dk.magenta.webscripts.user;

import dk.magenta.beans.PropertyValuesBean;
import dk.magenta.utils.JSONUtils;

import org.alfresco.query.PagingResults;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import org.alfresco.model.ContentModel;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ListOfDeactivatedUsers extends AbstractWebScript {

    private PropertyValuesBean propertyValuesBean;

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }



    private PersonService personService;

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    private NodeService nodeService;

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    SiteService siteService;


    @Override
    public void execute(WebScriptRequest webScriptRequest, WebScriptResponse webScriptResponse) throws IOException {

        webScriptResponse.setContentEncoding("UTF-8");
        Writer webScriptWriter = webScriptResponse.getWriter();
        JSONObject result;

        Map<String, String> params = JSONUtils.parseParameters(webScriptRequest.getURL());


        NodeRef peopleContainer = personService.getPeopleContainer();


        List<ChildAssociationRef> allPeople = nodeService.getChildAssocs(peopleContainer);

        Iterator i = allPeople.iterator();
        JSONArray array = new JSONArray();


        while (i.hasNext()) {

            NodeRef n = ((ChildAssociationRef)i.next()).getChildRef();

            String name = (String)nodeService.getProperty(n, ContentModel.PROP_USERNAME);

            boolean member =  siteService.isMember("retspsyk", (String)nodeService.getProperty(n, ContentModel.PROP_USERNAME));

            JSONObject o = new JSONObject();
            try {
                o.put("nodeRef", n);
                o.put("userName", name);
                o.put("firstName", (String)nodeService.getProperty(n, ContentModel.PROP_FIRSTNAME));
                o.put("lastName", (String)nodeService.getProperty(n, ContentModel.PROP_LASTNAME));
                o.put("active", member);
                array.put(o);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        JSONUtils.write(webScriptWriter, array);
    }
}




// F.eks. curl -i -u admin:admin -X GET 'http://localhost:8080/alfresco/s/propertyValues?siteShortName=retspsyk'

//curl -i -u admin:admin -X  -F 'img_avatar=@/home/petehouston/hello.txt' http://localhost/upload'