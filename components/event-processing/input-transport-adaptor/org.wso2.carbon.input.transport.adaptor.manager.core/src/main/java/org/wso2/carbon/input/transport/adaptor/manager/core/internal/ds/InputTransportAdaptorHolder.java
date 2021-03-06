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
package org.wso2.carbon.input.transport.adaptor.manager.core.internal.ds;


import org.wso2.carbon.input.transport.adaptor.core.InputTransportAdaptorService;

/**
 * This method is used to hold the transport adaptor service
 */
public class InputTransportAdaptorHolder {

    private static InputTransportAdaptorHolder instance = new InputTransportAdaptorHolder();

    private InputTransportAdaptorService transportAdaptorService;

    public static InputTransportAdaptorHolder getInstance() {
        return instance;
    }

    public void setInputTransportAdaptorService(InputTransportAdaptorService transportAdaptorService) {
        this.transportAdaptorService = transportAdaptorService;
    }

    public void unSetTInputTransportAdaptorService() {
        this.transportAdaptorService = null;
    }

    public InputTransportAdaptorService getInputTransportAdaptorService() {
        return this.transportAdaptorService;
    }
}
