package dk.magenta.beans;


import dk.magenta.model.DatabaseModel;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PsycBean {

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    private SiteService siteService;

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    private NodeService nodeService;



    public NodeRef getLibraryForInterviewRating() {
        NodeRef psycLibrary = siteService.getContainer("retspsyk", DatabaseModel.PROP_PSYC_LIBRARY);
        String expression = DatabaseModel.PROP_PSYC_LIBRARY_INTERVIEWRATING;

        NodeRef child = nodeService.getChildByName(psycLibrary, ContentModel.ASSOC_CONTAINS, expression);
        return child;
    }

    // Data for Psykiatriske interviews og ratingscales
    public void createDataForInterviewRating() {

        ArrayList<String> values = new ArrayList<>();
        values.add("PSE-10");
        values.add("PANSS");
        values.add("EASE");
        values.add("MAS");
        values.add("BDI-II");
        values.add("HAM-D");
        values.add("BAI");
        values.add("HAM-A");
        values.add("Y-BOCS");

        values.add("SCID-5-PD");
        values.add("OPD-2");
        values.add("ZAN-BPD");
        values.add("Hare’s PCL-R");
        values.add("Hare's PCL:SV");
        values.add("Hare's PCL:YV");
        values.add("CAPP");
        values.add("Hansson Sex Attitude Questionnaire");

        values.add("Hostility Towards Women Questionnaire");
        values.add("ABAS-3");
        values.add("Vineland-3");
        values.add("ADOS-2");
        values.add("ADI-R");
        values.add("BRIEF");
        values.add("DIVA 2");

        NodeRef library = this.getLibraryForInterviewRating();

        for (int i=0; i<=values.size()-1;i++ ) {
            String name = values.get(i);
            String id = String.valueOf(i);

            Map<QName, Serializable> props = new HashMap<>();
            props.put(DatabaseModel.PROP_ANVENDTUNDERSOEGELSESINST_ID, id);
            props.put(DatabaseModel.PROP_ANVENDTUNDERSOEGELSESINST_NAME, name);

            nodeService.createNode(library, ContentModel.ASSOC_CONTAINS,
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name),
                    DatabaseModel.TYPE_ANVENDTUNDERSOEGELSESINST, props).getChildRef();
        }
    }

}


