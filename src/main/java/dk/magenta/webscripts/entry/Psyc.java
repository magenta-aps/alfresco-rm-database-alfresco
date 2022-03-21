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
                    properties.put(DatabaseModel.PROPQNAME_PSYCDATA_PSYCH_TYPE, "0:t,1:t,2:f,3:t,4:f,5:t,6:f");
//                    properties.put(DatabaseModel.PROPQNAME_PSYCDATA_PSYCH_TYPE, "[\"0\",\"2\",\"3\"]"); dur ikke

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

                case "getInstrumentsForDetailview":

                    // get the aspect for the observand

                    String caseid = jsonProperties.getString("caseid");
                    String instrument = jsonProperties.getString("instrument");
                    String query = "@rm\\:caseNumber:\"" + caseid + "\"";

                    NodeRef observand = entryBean.getEntry(query);

                    if (nodeService.hasAspect(observand, ASPECT_PSYCDATA)) {

                        // todo - make the 2nd param for getProperty variable
                        ArrayList idPsycData = (ArrayList) nodeService.getProperty(observand, DatabaseModel.PROPQNAME_PSYCDATA_PSYCH_TYPE);
                        System.out.println("idPsycData");
                        System.out.println(idPsycData);

                        ArrayList formattedList = new ArrayList<String>(Arrays.asList(((String)idPsycData.get(0)).split(",")));
                        System.out.println("param");
                        System.out.println(formattedList);

                        ArrayList mappedValues = new ArrayList();

                        // add the label for each id

                        for (int i=0; i<= formattedList.size()-1;i++) {
                        String inst = (String)formattedList.get(i);

                        JSONObject instO = new JSONObject();
                        instO.put("id",inst.split(":")[0]);
                        instO.put("label",psycValuesBean.mapIdToLabel(inst.split(":")[0], DatabaseModel.PROP_PSYC_LIBRARY_PSYCH_TYPE));
                        instO.put("val",inst.split(":")[1].equals("t") ? true : false);

                        mappedValues.add(instO);
                        }

                        result.put("data", mappedValues);

//                        result.put(DatabaseModel.PROP_PSYC_LIBRARY_PSYCH_TYPE, formattedList);
                        JSONUtils.write(webScriptWriter, result);
                    }
                    else {
                        // add the aspect and return empty
                        nodeService.addAspect(observand, ASPECT_PSYCDATA, null);
                        JSONUtils.write(webScriptWriter, result);
                    }


                    break;
                case "getInstrumentsForOverview":

                    caseid = jsonProperties.getString("caseid");

                    query = "@rm\\:caseNumber:\"" + caseid + "\"";
                    observand = entryBean.getEntry(query);
                    System.out.println("observand");
                    System.out.println(observand);

                    ArrayList idPsycData = (ArrayList) nodeService.getProperty(observand, DatabaseModel.PROPQNAME_PSYCDATA_PSYCH_TYPE);
                    System.out.println("idPsycData");
                    System.out.println(idPsycData);

                    ArrayList param = new ArrayList<String>(Arrays.asList(((String)idPsycData.get(0)).split(",")));
                    System.out.println("param");
                    System.out.println(param);

                    result.put(DatabaseModel.PROP_PSYC_LIBRARY_PSYCH_TYPE, psycValuesBean.formatIdsForFrontend(param, DatabaseModel.PROP_PSYC_LIBRARY_PSYCH_TYPE));
                    JSONUtils.write(webScriptWriter, result);


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
