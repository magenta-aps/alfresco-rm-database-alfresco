package dk.magenta.beans;


import dk.magenta.model.DatabaseModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.site.SiteService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

    private Map<String, JSONArray> propertyValuesMap = new HashMap<>();

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

    public void loadPropertyValues () throws JSONException, FileNotFoundException, IOException {

        NodeRef rootFolderRef = siteService.getContainer("retspsyk", DatabaseModel.PROP_PSYC_LIBRARY);

        System.out.println("hvad er rootFolderRef");
        System.out.println(rootFolderRef);

        if(rootFolderRef != null) {
            List<FileInfo> folders = fileFolderService.listFolders(rootFolderRef);

            System.out.println("fileInfos");
            System.out.println(folders.size());

            JSONObject result = new JSONObject();

            for (FileInfo fileInfo : folders) {


                JSONArray jsonArray = new JSONArray();

                // each node wil be a folder
                NodeRef instrumentRootFolder = fileInfo.getNodeRef();
                System.out.println("instrumentRootFolder");
                System.out.println(instrumentRootFolder);

                System.out.println(nodeService.getProperty(instrumentRootFolder, ContentModel.PROP_NAME));

                String instrumentName = (String)nodeService.getProperty(instrumentRootFolder, ContentModel.PROP_NAME);

                // read the children
                List<ChildAssociationRef> children = nodeService.getChildAssocs(instrumentRootFolder);

                Iterator i = children.iterator();

                while (i.hasNext()) {
                    ChildAssociationRef child = (ChildAssociationRef)i.next();
//                    System.out.println("hvad er child");
//                    System.out.println(child.getChildRef());

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("id" , (String)nodeService.getProperty(child.getChildRef(), DatabaseModel.PROP_ANVENDTUNDERSOEGELSESINST_ID));
                    jsonObject.put("name" , (String)nodeService.getProperty(child.getChildRef(), DatabaseModel.PROP_ANVENDTUNDERSOEGELSESINST_NAME));

                    jsonArray.put(jsonObject);
                }
                propertyValuesMap.put(instrumentName, jsonArray);
            }

        }
    }

    public void pingMap() {
        System.out.println("er der noget i dit map?");
        System.out.println(this.propertyValuesMap.size());
        System.out.println(this.propertyValuesMap.get("Psykiatriske_interviews_og_ratingscales"));

    }

    public void updatePropertyValues (String siteShortName, String property, JSONArray values) throws JSONException, FileNotFoundException {

        NodeRef rootFolderRef = siteService.getContainer(siteShortName, DatabaseModel.PROP_VALUES);

        JSONArray propertyValues = propertyValuesMap.get(siteShortName);
        propertyValues.put(Integer.parseInt(property), values);

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
                this.loadPropertyValues();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }







}

