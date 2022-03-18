package dk.magenta.webscripts.entry;

import dk.magenta.beans.EntryBean;
import dk.magenta.beans.PsycBean;
import dk.magenta.beans.PsycValuesBean;
import dk.magenta.model.DatabaseModel;
import dk.magenta.utils.JSONUtils;
import org.alfresco.jlan.util.StringList;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.*;

import static dk.magenta.model.DatabaseModel.ASPECT_PSYCDATA;

public class Psyc extends AbstractWebScript {

    public void setPsycBean(PsycBean psycBean) {
        this.psycBean = psycBean;
    }
    private PsycBean psycBean;

    public void setPsycValuesBean(PsycValuesBean psycValuesBean) {
        this.psycValuesBean = psycValuesBean;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    private NodeService nodeService;
    private PsycValuesBean psycValuesBean;

    public void setEntryBean(EntryBean entryBean) {
        this.entryBean = entryBean;
    }

    private EntryBean entryBean;

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();

        res.setContentEncoding("UTF-8");
        Content c = req.getContent();
        JSONObject result = new JSONObject();

        Writer webScriptWriter = res.getWriter();


        JSONObject jsonProperties = null;

        JSONObject json = null;
        try {

            json = new JSONObject(c.getContent());
            jsonProperties = JSONUtils.getObject(json, "properties");
            String method = jsonProperties.getString("method");

            System.out.println("hvad er method");
            System.out.println(method);

            System.out.println("hvad er properties");
            System.out.println(jsonProperties);

            switch (method) {

                case "test":
                    Map<QName, Serializable> properties = new HashMap<>();
                    properties.put(DatabaseModel.PROPQNAME_PSYCDATA_PSYCH_TYPE, "[\"0\",\"2\",\"3\"]");

                    NodeRef n = new NodeRef("workspace://SpacesStore/125074f7-3869-4e72-ab05-94dc0824db19");
                    nodeService.removeAspect(n, ASPECT_PSYCDATA);
                    nodeService.addAspect(n, ASPECT_PSYCDATA, properties);

                    psycValuesBean.loadPropertyValues();


                    ArrayList ids = new ArrayList();
                    ids.add("1");
                    ids.add("4");
                    ids.add("5");

                    psycValuesBean.formatIdsForFrontend(ids,"psykologisk_undersoegelsestype");

//                    String query = "";
//
//                    String base = "@rmpsy\\:psykologisk_undersoegelsestype: 1";
//
//                    for (int i=2; i<=1000;i++) {
//                        base = base + " OR " + "@rmpsy\\:psykologisk_undersoegelsestype: " + i;
//                    }
//
//
//
//                    query = base;
//                    System.out.println("query");
//                    System.out.println(query);
//
//                    List<NodeRef> list = entryBean.getEntriesbyQuery(query);
//                    System.out.println("list");
//                    System.out.println(list);

//

                    // indlæs alt
                    // knyt det nye aspekt på - set hvordan det ser ud i nodebrowser
                    // sæt værdier ind for et tilfældigt fil.



//                     psycBean.createAllData();


//                    psycValuesBean.pingMap();

                    JSONArray list = psycValuesBean.getPropertyValues();

                    result.put("result", list);


                    System.out.println("output fra test");

                    JSONUtils.write(webScriptWriter, result);

                    break;

                case "getInstruments":

                    // get the aspect for the observand

                    String caseid = jsonProperties.getString("caseid");
                    String query = "@rm\\:caseNumber:\"" + caseid + "\"";

                    NodeRef observand = entryBean.getEntry(query);

                    if (nodeService.hasAspect(observand, ASPECT_PSYCDATA)) {

                        ArrayList r = (ArrayList) nodeService.getProperty(observand, DatabaseModel.PROPQNAME_PSYCDATA_PSYCH_TYPE);


                        // Map the id's to the real name - put this in the propertyvaluesBean - a method that given a group of id's maps them to two object, one with
//                        the list of id's and one with the list of names

                        System.out.println("r");
                        System.out.println(r);

                        result.put(DatabaseModel.PROP_PSYC_LIBRARY_PSYCH_TYPE, r);


                        System.out.println("output fra test");

                        JSONUtils.write(webScriptWriter, result);




//                        StringList psyc = (StringList) nodeService.getProperty(observand, DatabaseModel.PROPQNAME_PSYCDATA_PSYCH_TYPE);

                    }
                    else {
                        // add the aspect and return empty
                    }


                    break;
                case "total":
                    break;
            }
        } catch (JSONException e) {
            System.out.println("json exception");
            e.printStackTrace();
        }
        catch (Exception e) {
            System.out.println("this exception");
            System.out.println(e.toString());
            e.printStackTrace();
        }
    }
}
