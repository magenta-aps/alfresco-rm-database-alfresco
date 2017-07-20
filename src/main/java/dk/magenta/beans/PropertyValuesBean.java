package dk.magenta.beans;


import dk.magenta.model.DatabaseModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class PropertyValuesBean {

    private Repository repository;
    private FileFolderService fileFolderService;
    private ContentService contentService;


    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    public void setFileFolderService(FileFolderService fileFolderService) {
        this.fileFolderService = fileFolderService;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public JSONObject getPropertyValues () throws JSONException, FileNotFoundException, IOException {

        NodeRef companyHome = repository.getCompanyHome();
        List<String> pathElements = DatabaseModel.PROP_VALUES_PATH;

        NodeRef folderRef = fileFolderService.resolveNamePath(companyHome, pathElements).getNodeRef();
        List<FileInfo> fileInfos = fileFolderService.listFiles(folderRef);

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

            String propertyName = fileInfo.getName().replace(".txt", "");
            result.put(propertyName, values);
        }
        return result;
    }

}

