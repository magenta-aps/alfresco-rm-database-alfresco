// 
// Copyright (c) 2017-2018, Magenta ApS
// 
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at http://mozilla.org/MPL/2.0/.
// 

/*
Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package dk.magenta.webscripts.contents;

import dk.magenta.beans.ContentsBean;
import dk.magenta.utils.JSONUtils;
import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

public class GetThumbnail extends AbstractWebScript {

    private ContentsBean contentsBean;

    public void setContentsBean(ContentsBean contentsBean) {
        this.contentsBean = contentsBean;
    }



    @Override
    public void execute(WebScriptRequest webScriptRequest, WebScriptResponse webScriptResponse) throws IOException {

        Map<String, String> params = JSONUtils.parseParameters(webScriptRequest.getURL());

        webScriptResponse.setContentEncoding("UTF-8");
        Writer webScriptWriter = webScriptResponse.getWriter();
        JSONObject result = new JSONObject();

        try {
            String nodeId = params.get("nodeId");
            String versionId = params.get("versionId");

            NodeRef versionPreviewRef = contentsBean.getThumbnail(nodeId, versionId, false);

            result.put("nodeRef", versionPreviewRef.toString());


        } catch (Exception e) {
            e.printStackTrace();
            result = JSONUtils.getObject("error", e.toString());
            webScriptResponse.setStatus(400);
        }
        JSONUtils.write(webScriptWriter, result);
    }


}
