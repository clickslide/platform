/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.mediator.bam.ui;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.SynapseConstants;
import org.wso2.carbon.mediator.service.MediatorException;
import org.wso2.carbon.mediator.service.ui.AbstractMediator;

import javax.xml.namespace.QName;


public class BamMediator extends AbstractMediator {

    private String serverProfile = "";
    private String streamName = "";
    private String streamVersion = "";

    public String getServerProfile(){
        return this.serverProfile;
    }

    public void setServerProfile(String serverProfile1){
        this.serverProfile = serverProfile1;
    }

    public String getStreamName(){
        return this.streamName;
    }

    public void setStreamName(String streamName){
        this.streamName = streamName;
    }

    public String getStreamVersion(){
        return this.streamVersion;
    }

    public void setStreamVersion(String streamVersion){
        this.streamVersion = streamVersion;
    }

    public String getTagLocalName() {
        return "bam";
    }

    public OMElement serialize(OMElement parent) {
        OMElement bamElement = fac.createOMElement("bam", synNS);
        saveTracingState(bamElement, this);

        bamElement.addChild(serializeServerProfile());
        bamElement.addChild(serializeStreamConfiguration());

        if (parent != null) {
            parent.addChild(bamElement);
        } else {
            String msg = "The parent element is not specified";
            throw new MediatorException(msg);
        }
        return bamElement;
    }

    public void build(OMElement omElement) {

        OMElement profileElement = omElement.getFirstChildWithName(
                new QName(SynapseConstants.SYNAPSE_NAMESPACE, "serverProfile"));
        if (profileElement != null){
            processProfile(profileElement);
        } else {
            String msg = "The 'serverProfile' element is not specified";
            throw new MediatorException(msg);
        }

        OMElement streamElement = omElement.getFirstChildWithName(
                new QName(SynapseConstants.SYNAPSE_NAMESPACE, "streamConfig"));
        if(streamElement != null){
            processStreamConfiguration(streamElement);
        } else {
            String msg = "The 'streamConfig' element is not specified";
            throw new MediatorException(msg);
        }

        processAuditStatus(this, omElement);
    }

    private void processProfile(OMElement profile){
        OMAttribute pathAttr = profile.getAttribute(new QName("name"));
        if(pathAttr != null){
            String pathValue = pathAttr.getAttributeValue();
            this.setServerProfile(pathValue);
        } else {
            String msg = "The 'name' attribute of Profile is not specified";
            throw new MediatorException(msg);
        }
    }

    private void processStreamConfiguration(OMElement streamConfig){
        OMAttribute streamNameAttr = streamConfig.getAttribute(new QName("name"));
        OMAttribute streamVersionAttr = streamConfig.getAttribute(new QName("version"));
        if(streamNameAttr != null && streamVersionAttr != null){
            String nameValue = streamNameAttr.getAttributeValue();
            String versionValue = streamVersionAttr.getAttributeValue();
            this.setStreamName(nameValue);
            this.setStreamVersion(versionValue);
        } else {
            String msg = "The stream name or stream version attributes are not specified";
            throw new MediatorException(msg);
        }
    }

    private OMElement serializeServerProfile(){
        OMElement profileElement = fac.createOMElement("serverProfile", synNS);
        profileElement.addAttribute("name", this.serverProfile, nullNS);
        return profileElement;
    }

    private OMElement serializeStreamConfiguration(){
        OMElement streamConfigElement = fac.createOMElement("streamConfig",synNS);
        streamConfigElement.addAttribute("name", this.streamName, nullNS);
        streamConfigElement.addAttribute("version", this.streamVersion, nullNS);
        return streamConfigElement;
    }

}
