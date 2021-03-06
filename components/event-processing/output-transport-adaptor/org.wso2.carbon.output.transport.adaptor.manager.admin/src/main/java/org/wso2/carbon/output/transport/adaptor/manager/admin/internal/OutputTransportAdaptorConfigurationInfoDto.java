package org.wso2.carbon.output.transport.adaptor.manager.admin.internal;

/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Transport Configuration Details are stored (Name and Type) which needed to display in the available transport adaptor list
 */
public class OutputTransportAdaptorConfigurationInfoDto {
    private String transportAdaptorName;
    private String transportAdaptorType;
    private boolean enableTracing;
    private boolean enableStats;

    public String getTransportAdaptorName() {
        return transportAdaptorName;
    }

    public void setTransportAdaptorName(String transportAdaptorName) {
        this.transportAdaptorName = transportAdaptorName;
    }

    public String getTransportAdaptorType() {
        return transportAdaptorType;
    }

    public void setTransportAdaptorType(String transportAdaptorType) {
        this.transportAdaptorType = transportAdaptorType;
    }

    public boolean isEnableTracing() {
        return enableTracing;
    }

    public void setEnableTracing(boolean enableTracing) {
        this.enableTracing = enableTracing;
    }

    public boolean isEnableStats() {
        return enableStats;
    }

    public void setEnableStats(boolean enableStats) {
        this.enableStats = enableStats;
    }
}
