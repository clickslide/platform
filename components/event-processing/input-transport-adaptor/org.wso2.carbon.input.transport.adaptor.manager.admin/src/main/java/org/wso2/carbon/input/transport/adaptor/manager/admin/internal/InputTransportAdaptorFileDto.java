package org.wso2.carbon.input.transport.adaptor.manager.admin.internal;/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/**
 * to store Not deployed transport adaptor configuration file details (filepath & transport adaptor name)
 */
public class InputTransportAdaptorFileDto {

    private String filePath;
    private String transportAdaptorName;

    public InputTransportAdaptorFileDto(String filePath, String transportAdaptorName) {
        this.filePath = filePath;
        this.transportAdaptorName = transportAdaptorName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getTransportAdaptorName() {
        return transportAdaptorName;
    }

    public void setTransportAdaptorName(String transportAdaptorName) {
        this.transportAdaptorName = transportAdaptorName;
    }
}
