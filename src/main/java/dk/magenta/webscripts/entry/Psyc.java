package dk.magenta.webscripts.entry;

import dk.magenta.beans.EntryBean;
import dk.magenta.beans.PsycBean;
import dk.magenta.beans.PsycValuesBean;
import dk.magenta.model.DatabaseModel;
import dk.magenta.utils.JSONUtils;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
//                    Map<QName, Serializable> properties = new HashMap<>();
//                    properties.put(DatabaseModel.PROPQNAME_PSYCDATA_PSYCH_TYPE, "[\"21\",\"36\",\"1\"]");
//
//                    NodeRef n = new NodeRef("workspace://SpacesStore/5c11e08c-8064-494a-9074-3ff6d07ed81e");
//                    nodeService.addAspect(n, ASPECT_PSYCDATA, properties);


                    String query = "";

                    String base = "@rmpsy\\:psykologisk_undersoegelsestype: 1";

                    for (int i=2; i<=1000;i++) {
                        base = base + " OR " + "@rmpsy\\:psykologisk_undersoegelsestype: " + i;
                    }



                    query = base;
                    System.out.println("query");
                    System.out.println(query);

                    List<NodeRef> list = entryBean.getEntriesbyQuery(query);
                    System.out.println("list");
                    System.out.println(list);

//

                    // indlæs alt
                    // knyt det nye aspekt på - set hvordan det ser ud i nodebrowser
                    // sæt værdier ind for et tilfældigt fil.



//                     psycBean.createAllData();

//                    psycValuesBean.loadPropertyValues();
//                    psycValuesBean.pingMap();


                    System.out.println("output fra test");

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

        Writer webScriptWriter = res.getWriter();

        JSONUtils.write(webScriptWriter, result);

    }
}
