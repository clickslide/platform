package org.wso2.carbon.eventbridge.restapi.internal;

/**
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.wso2.carbon.eventbridge.core.engine.Engine;
import org.wso2.carbon.eventbridge.restapi.RestAPIServlet;
import org.wso2.carbon.eventbridge.restapi.jaxrs.RestAPIApp;
import org.wso2.carbon.eventbridge.restapi.jaxrs.RestAPIContext;
import org.wso2.carbon.eventbridge.restapi.rest.RestEventReceiver;
import org.wso2.carbon.identity.authentication.AuthenticationService;

import javax.servlet.ServletException;
import java.util.Dictionary;
import java.util.Hashtable;

/**
 * @scr.component name="org.wso2.carbon.bam.eventreceiver.component" immediate="true"
 * @scr.reference name="osgi.httpservice" interface="org.osgi.service.http.HttpService"
 * cardinality="1..1" policy="dynamic" bind="setHttpService"  unbind="unsetHttpService"
 * @scr.reference name="org.wso2.carbon.identity.authentication.internal.AuthenticationServiceComponent"
 * interface="org.wso2.carbon.identity.authentication.AuthenticationService"
 * cardinality="1..1" policy="dynamic" bind="setAuthenticationService"  unbind="unsetAuthenticationService"
 * @scr.reference name="eventbridge.core"
 * interface="org.wso2.carbon.eventbridge.core.engine.Engine"
 * cardinality="1..1" policy="dynamic" bind="setEngine"  unbind="unsetEngine"
 *
 */
public class RestAPIServiceComponent {

    private static Log log = LogFactory.getLog(RestAPIServiceComponent.class);

    private String path = "/bam";

    protected void activate(ComponentContext componentContext) {
        Utils.setEventReceiver(new RestEventReceiver());

    }

    protected void setHttpService(HttpService httpService) {
        try {
            Dictionary<String, String> initParams = new Hashtable<String, String>();
            initParams.put("javax.ws.rs.Application", RestAPIApp.class.getName());
            httpService.registerServlet(path, new RestAPIServlet(), initParams, new RestAPIContext());
        } catch (ServletException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } catch (NamespaceException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }

    }

    protected void unsetHttpService(HttpService httpService) {
        httpService.unregister(path);
    }

    protected void setAuthenticationService(AuthenticationService authenticationService) {
        Utils.setAuthenticationService(authenticationService);
    }

    protected void unsetAuthenticationService(AuthenticationService authenticationService) {
        Utils.setAuthenticationService(null);
    }

    protected void setEngine(Engine engine) {
        Utils.setEngine(engine);
    }

    protected void unsetEngine(Engine engine) {
        Utils.setEngine(null);
    }
}
