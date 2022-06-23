package dk.magenta.beans;


import dk.magenta.model.DatabaseModel;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.site.SiteService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.*;

public class PsycValuesBean {

    private FileFolderService fileFolderService;
    private ContentService contentService;
    private SiteService siteService;

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    private NodeService nodeService;

    public void setPsycBean(PsycBean psycBean) {
        this.psycBean = psycBean;
    }

    private PsycBean psycBean;

    private JSONArray propertyValues = new JSONArray();

    // ex map[instrumentType][id] -> så får du valuestreng

    private Map<String, Map<String, String>> mapped = new HashMap<>();

    public void setFileFolderService(FileFolderService fileFolderService) {
        this.fileFolderService = fileFolderService;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

//    public JSONObject getPropertyValues () {
//        return propertyValuesMap.get();
//    }

    private void setupMapping() throws JSONException {
        for (int i = 0; i<=this.propertyValues.length()-1; i++) {

            JSONObject instrument = this.propertyValues.getJSONObject(i);

            String instrumentName = (String) instrument.get("instrumentname");
            JSONArray jsonArray = (JSONArray) instrument.get("values");

            Map<String, String> instruments = new HashMap<>();

            for (int k=0; k <= jsonArray.length()-1;k++) {
                JSONObject val = jsonArray.getJSONObject(k);
                instruments.put(val.getString("id"), val.getString("name"));
            }
            this.mapped.put(instrumentName, instruments);
        }
    }

    public int getLengthOfInstrumentList(String instrumentName) {
        return mapped.get(instrumentName).size();
    }

    public void loadPropertyValues () throws JSONException, FileNotFoundException, IOException {

        propertyValues = new JSONArray();

        NodeRef rootFolderRef = siteService.getContainer("retspsyk", DatabaseModel.PROP_PSYC_LIBRARY);


        if(rootFolderRef != null) {
            List<FileInfo> folders = fileFolderService.listFolders(rootFolderRef);

            JSONObject result = new JSONObject();

            for (FileInfo fileInfo : folders) {


                JSONArray jsonArray = new JSONArray();

                // each node wil be a folder
                NodeRef instrumentRootFolder = fileInfo.getNodeRef();

                String instrumentName = (String)nodeService.getProperty(instrumentRootFolder, ContentModel.PROP_NAME);

                // read the children
                List<ChildAssociationRef> children = nodeService.getChildAssocs(instrumentRootFolder);

                Iterator i = children.iterator();

                while (i.hasNext()) {
                    ChildAssociationRef child = (ChildAssociationRef)i.next();

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("id" , (String)nodeService.getProperty(child.getChildRef(), DatabaseModel.PROP_ANVENDTUNDERSOEGELSESINST_ID));
                    jsonObject.put("name" , (String)nodeService.getProperty(child.getChildRef(), DatabaseModel.PROP_ANVENDTUNDERSOEGELSESINST_NAME));

                    jsonArray.put(jsonObject);
                }

                JSONObject o = new JSONObject();
                o.put("instrumentname", instrumentName);
                o.put("values", jsonArray);

                propertyValues.put(o);
            }
        }
        this.setupMapping();
    }

    public void pingMap() {
    }

    public JSONArray getPropertyValues() {
        return propertyValues;
    }

    public String mapIdToLabel(String id, String instrumentName) {
        Map<String, String> instrumentValues = this.mapped.get((instrumentName));

        return instrumentValues.get(id);
    }


    public JSONObject formatIdsForFrontend(ArrayList ids, String instrumentName) throws JSONException {

        // get the ids and names for the instrument

        Map<String, String> instrumentValues = this.mapped.get((instrumentName));


        JSONObject o = new JSONObject();
        ArrayList nameList = new ArrayList();
        ArrayList idList = new ArrayList();

        for (int i=0; i<=ids.size()-1;i++) {
            String id = (String)ids.get(i);
            idList.add(id);
            nameList.add(instrumentValues.get(id));
        }

        o.put("idList", idList);
        o.put("nameList", nameList);

        return o;
    }

    public JSONObject getValuesForInstrument(String inst) throws JSONException {

        int i = 0;

        while (i<=this.propertyValues.length()-1) {

            JSONObject instrument = this.propertyValues.getJSONObject(i);

            if (instrument.get("instrumentname").equals(inst)) {
                return instrument;
            }
            else {
                i++;
            }
        }
        return null;
    }
}

