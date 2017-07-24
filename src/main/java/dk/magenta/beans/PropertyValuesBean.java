package dk.magenta.beans;


import dk.magenta.model.DatabaseModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.*;

public class PropertyValuesBean {

    private Repository repository;
    private FileFolderService fileFolderService;
    private ContentService contentService;

    private JSONObject propertyValues;
    private NodeRef rootFolderRef;


    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    public void setFileFolderService(FileFolderService fileFolderService) {
        this.fileFolderService = fileFolderService;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public JSONObject getPropertyValues () {
        return propertyValues;
    }

    public void loadPropertiesFolder() throws FileNotFoundException {
        NodeRef companyHome = repository.getCompanyHome();
        List<String> pathElements = DatabaseModel.PROP_VALUES_PATH;

        rootFolderRef = fileFolderService.resolveNamePath(companyHome, pathElements).getNodeRef();
    }

    public void loadPropertyValues () throws JSONException, FileNotFoundException, IOException {

        List<FileInfo> fileInfos = fileFolderService.listFiles(rootFolderRef);

        JSONObject result = new JSONObject();

        for (FileInfo fileInfo : fileInfos) {
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
            result.put(propertyName, values);
        }
        propertyValues = result;
    }

    public void updatePropertyValues (String property, JSONArray values) throws JSONException, FileNotFoundException {
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
    }
}

