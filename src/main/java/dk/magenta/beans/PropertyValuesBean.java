package dk.magenta.beans;


import dk.magenta.model.DatabaseModel;
import dk.magenta.utils.JSONUtils;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.*;

public class PropertyValuesBean {

    private FileFolderService fileFolderService;
    private ContentService contentService;
    private SiteService siteService;

    private Map<String, JSONObject> propertyValuesMap = new HashMap<>();

    public void setFileFolderService(FileFolderService fileFolderService) {
        this.fileFolderService = fileFolderService;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public JSONObject getPropertyValues (String siteShortName) {
        return propertyValuesMap.get(siteShortName);
    }

    public void loadPropertyValues (String siteShortName) throws JSONException, FileNotFoundException, IOException {

        NodeRef rootFolderRef = siteService.getContainer(siteShortName, DatabaseModel.PROP_VALUES);
        if(rootFolderRef != null) {
            List<FileInfo> fileInfos = fileFolderService.listFiles(rootFolderRef);

            JSONObject result = new JSONObject();

            for (FileInfo fileInfo : fileInfos) {
                if (fileInfo.getName().equals("referingAgency.txt")) {
                    System.out.println("found referingAgency");

                    JSONArray values = new JSONArray();
                    NodeRef nodeRef = fileInfo.getNodeRef();
                    ContentReader contentReader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
                    InputStream s = contentReader.getContentInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(s));

                    String line;
                    System.out.println("hvad er line");
                    while ((line = br.readLine()) != null) {
                        System.out.println(line);
                        values.put(line);
                    }

                    s.close();
                    br.close();

                    String propertyName = fileInfo.getName().replace(".txt", "");

                    // cleanup if socialworker, secretary, doctor or psycologist -

                    // TODO code to cleanup

                    result.put(propertyName, values);
                }
                else {

                    JSONArray values = new JSONArray();
                    NodeRef nodeRef = fileInfo.getNodeRef();
                    ContentReader contentReader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
                    InputStream s = contentReader.getContentInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(s));

                    String line;
                    while ((line = br.readLine()) != null)
                        values.put(line);

                    s.close();
                    br.close();

                    String propertyName = fileInfo.getName().replace(".txt", "");

                    // cleanup if socialworker, secretary, doctor or psycologist -

                    // TODO code to cleanup

                    result.put(propertyName, values);
                }
            }
            propertyValuesMap.put(siteShortName, result);
        }
    }

    public void updatePropertyValues (String siteShortName, String property, JSONArray values) throws JSONException, FileNotFoundException {

        NodeRef rootFolderRef = siteService.getContainer(siteShortName, DatabaseModel.PROP_VALUES);

        JSONObject propertyValues = propertyValuesMap.get(siteShortName);
        propertyValues.put(property, values);

        List<String> path = new ArrayList<>(Collections.singletonList(property + ".txt"));
        FileInfo fileInfo = fileFolderService.resolveNamePath(rootFolderRef, path);
        NodeRef nodeRef = fileInfo.getNodeRef();

        StringBuilder output = new StringBuilder();
        int length = values.length();
        for(int i=0; i < length; i++) {
            String value = values.getString(i);
            output.append(value);

            if(i + 1 != length)
                output.append("\n");
        }

        ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        writer.putContent(output.toString());

        // reload the properties if the property was referingAgency

        if (property.equals("referingAgency")) {
            try {
                this.loadPropertyValues("retspsyk");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public String getReferingAgentByKey( String key) throws JSONException{

        JSONObject values = propertyValuesMap.get("retspsyk");

        JSONArray agents = values.getJSONArray("referingAgency");

        for (int i=0; i<= agents.length()-1; i++) {

            String s = agents.getString(i);

            JSONObject o = new JSONObject(s);

            if (key.equals(o.get("title"))) {
                return "" + o.get("title") + "\n" + o.get("adresse") + "\n" + o.get("postnr")  + " " + o.get("by") + "";
            };
        }
        return "";
    };

    public String getUserByUserName (String userName) throws JSONException {

        // secretaries

        JSONObject values = propertyValuesMap.get("retspsyk");



        JSONArray secretaries = values.getJSONArray("secretary");
        for (int i=0; i<= secretaries.length()-1; i++) {

            String s = secretaries.getString(i);

            if (s.contains(userName)) {
                s = s.replace("(" + userName +")","").trim();
                return s;
            }
        }

        JSONArray socialworker = values.getJSONArray("socialworker");
        for (int i=0; i<= socialworker.length()-1; i++) {

            String s = socialworker.getString(i);

            if (s.contains(userName)) {
                s = s.replace("(" + userName +")","").trim();
                return s;
            }
        }

        JSONArray psychologist = values.getJSONArray("psychologist");
        for (int i=0; i<= psychologist.length()-1; i++) {

            String s = psychologist.getString(i);

            if (s.contains(userName)) {
                s = s.replace("(" + userName +")","").trim();
                return s;
            }
        }

        JSONArray doctor = values.getJSONArray("doctor");
        for (int i=0; i<= doctor.length()-1; i++) {

            String s = doctor.getString(i);

            if (s.contains(userName)) {
                s = s.replace("(" + userName +")","").trim();
                return s;
            }
        }

        return null;
    }

    public String getUserNameByUser (String userName) throws JSONException {



        // secretaries

        JSONObject values = propertyValuesMap.get("retspsyk");
        JSONArray secretaries = values.getJSONArray("secretary");

        for (int i=0; i<= secretaries.length()-1; i++) {

            String s = secretaries.getString(i);

            if (s.contains(userName)) {
                s = s.replace(userName,"").trim();
                s= s.replace("(","").trim();
                s= s.replace(")","").trim();
                return s;
            }
        }

        JSONArray socialworker = values.getJSONArray("socialworker");
        for (int i=0; i<= socialworker.length()-1; i++) {

            String s = socialworker.getString(i);

            if (s.contains(userName)) {
                s = s.replace(userName,"").trim();
                s= s.replace("(","").trim();
                s= s.replace(")","").trim();
                return s;
            }
        }

        JSONArray psychologist = values.getJSONArray("psychologist");
        for (int i=0; i<= psychologist.length()-1; i++) {

            String s = psychologist.getString(i);

            if (s.contains(userName)) {
                s = s.replace(userName,"").trim();
                s= s.replace("(","").trim();
                s= s.replace(")","").trim();
                return s;
            }
        }

        JSONArray doctor = values.getJSONArray("doctor");

        for (int i=0; i<= doctor.length()-1; i++) {

            String s = doctor.getString(i);

            if (s.contains(userName)) {
                s = s.replace(userName,"").trim();
                s= s.replace("(","").trim();
                s= s.replace(")","").trim();
                return s;
            }
        }

        return null;
    }

}

