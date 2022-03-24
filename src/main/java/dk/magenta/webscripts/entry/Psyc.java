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
import static dk.magenta.model.DatabaseModel.RMPSY_MODEL_URI;

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
                    properties.put(DatabaseModel.PROPQNAME_PSYCDATA_PSYCH_TYPE, "3,5,6");
//                    properties.put(DatabaseModel.PROPQNAME_PSYCDATA_PSYCH_TYPE, "0:f,1:f,2:f,3:t,4:f,5:t,6:f");
//                    properties.put(DatabaseModel.PROPQNAME_PSYCDATA_PSYCH_TYPE, "[\"0\",\"2\",\"3\"]"); dur ikke

                    NodeRef n = new NodeRef("workspace://SpacesStore/125074f7-3869-4e72-ab05-94dc0824db19");
                    nodeService.removeAspect(n, ASPECT_PSYCDATA);
                    nodeService.addAspect(n, ASPECT_PSYCDATA, properties);

                    psycValuesBean.loadPropertyValues();

                    // psycBean.createAllData();

                    JSONArray list = psycValuesBean.getPropertyValues();

                    result.put("result", list);


                    System.out.println("output fra test");

                    JSONUtils.write(webScriptWriter, result);

                    break;

                case "saveInstrumentsForDetailview":

                    String caseid = jsonProperties.getString("caseid");

                    String instrument = jsonProperties.getString("instrument");
                    System.out.println("instrument");
                    System.out.println(instrument);

                    String selected = jsonProperties.getString("selected");
                    System.out.println("hvad er selected: ");
                    System.out.println(selected);

                    String query = "@rm\\:caseNumber:\"" + caseid + "\"";

                    System.out.println("instrument: ");
                    System.out.println(instrument);

                    NodeRef observand = entryBean.getEntry(query);
                    QName instrumentQname = QName.createQName(RMPSY_MODEL_URI, instrument);

                    nodeService.setProperty(observand,instrumentQname,selected);

//                    ArrayList idPsycData = (ArrayList) nodeService.getProperty(observand, QName.createQName(RMPSY_MODEL_URI, instrument));


                    break;


                case "getInstrumentsForDetailview":

                    // get the aspect for the observand

                    caseid = jsonProperties.getString("caseid");
                    instrument = jsonProperties.getString("instrument");
                    query = "@rm\\:caseNumber:\"" + caseid + "\"";

                    System.out.println("instrument: ");
                    System.out.println("instrument: ");
                    System.out.println("instrument: ");
                    System.out.println(instrument);

                    observand = entryBean.getEntry(query);
                    instrumentQname = QName.createQName(RMPSY_MODEL_URI, instrument);


                    if (nodeService.hasAspect(observand, ASPECT_PSYCDATA) && (nodeService.getProperty(observand, instrumentQname) != null)) {

                        ArrayList idPsycData = (ArrayList) nodeService.getProperty(observand, QName.createQName(RMPSY_MODEL_URI, instrument));
                        System.out.println("idPsycData");
                        System.out.println(idPsycData);
                        System.out.println("System.out.println(idPsycData.contains(3));");
                        System.out.println(idPsycData.contains(3));

                        ArrayList formattedList = new ArrayList<String>(Arrays.asList(((String)idPsycData.get(0)).split(",")));
                        System.out.println("param");
                        System.out.println(formattedList);

                        ArrayList mappedValues = new ArrayList();

                        // add the label for each id


                        // setup the totallist of id: xx, label: xx, val: xx

                        System.out.println("antal for instrument:" + instrument);
                        System.out.println("antal for instrument:" + instrument);
                        System.out.println("antal for instrument:" + instrument);
                        System.out.println(psycValuesBean.getLengthOfInstrumentList(instrument));




                        for (int k=0; k<=psycValuesBean.getLengthOfInstrumentList(instrument)-1;k++) {

                            System.out.println("formattedList.contains(String.valueOf(k));");
                            System.out.println(formattedList.contains(String.valueOf(k)));

                            JSONObject entry = new JSONObject();
                            entry.put("id",k);
                            entry.put("label",psycValuesBean.mapIdToLabel(String.valueOf(k), instrument));
                            entry.put("val", formattedList.contains(String.valueOf(k)) ? true : false);

                            mappedValues.add(entry);
                        }



//                        for (int i=0; i<= formattedList.size()-1;i++) {
//                        String inst = (String)formattedList.get(i);
//
//                        JSONObject instO = new JSONObject();
//                        instO.put("id",inst.split(":")[0]);
//                        instO.put("label",psycValuesBean.mapIdToLabel(inst.split(":")[0], DatabaseModel.PROP_PSYC_LIBRARY_PSYCH_TYPE));
//                        instO.put("val",inst.split(":")[1].equals("t") ? true : false);
//
//
//                        }




                           // get the length of the instrument category

                           // make the list

                          // set the selected values in the return list


                        result.put("data", mappedValues);

//                        result.put(DatabaseModel.PROP_PSYC_LIBRARY_PSYCH_TYPE, formattedList);
                        JSONUtils.write(webScriptWriter, result);
                    }
                    else {
                        // add the aspect and and make an empty default for that instrument



                        // nodeService.addAspect(observand, ASPECT_PSYCDATA, null);

                        System.out.println("getValuesForInstrument");
                        System.out.println(instrument);
                        JSONObject values = psycValuesBean.getValuesForInstrument(instrument);
                        System.out.println("values");
                        System.out.println(values.get("values"));

                        ArrayList mappedValues = new ArrayList();
                        JSONArray valuesArray = (JSONArray) values.get("values");

                        for (int i=0; i<= valuesArray.length()-1;i++) {

                            JSONObject val = (JSONObject) valuesArray.get(i);

                            JSONObject instO = new JSONObject();
                            instO.put("id",val.getString("id"));
                            instO.put("label", val.getString("name"));
                            instO.put("val",false);

                            mappedValues.add(instO);
                        }

                        result.put("data", mappedValues);
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

//                case "createPsycPropertyValues":


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
