/*
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

package org.wso2.carbon.input.transport.adaptor.manager.core.internal;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.input.transport.adaptor.core.config.InputTransportAdaptorConfiguration;
import org.wso2.carbon.input.transport.adaptor.manager.core.InputTransportAdaptorFile;
import org.wso2.carbon.input.transport.adaptor.manager.core.InputTransportAdaptorInfo;
import org.wso2.carbon.input.transport.adaptor.manager.core.InputTransportAdaptorManagerService;
import org.wso2.carbon.input.transport.adaptor.manager.core.InputTransportAdaptorNotificationListener;
import org.wso2.carbon.input.transport.adaptor.manager.core.exception.InputTransportAdaptorManagerConfigurationException;
import org.wso2.carbon.input.transport.adaptor.manager.core.internal.util.InputTransportAdaptorManagerConstants;
import org.wso2.carbon.input.transport.adaptor.manager.core.internal.util.helper.InputTransportAdaptorConfigurationFilesystemInvoker;
import org.wso2.carbon.input.transport.adaptor.manager.core.internal.util.helper.InputTransportAdaptorConfigurationHelper;

import javax.xml.stream.XMLStreamException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * carbon implementation of the transport adaptor manager.
 */
public class CarbonInputTransportAdaptorManagerService
        implements InputTransportAdaptorManagerService {
    private static final Log log = LogFactory.getLog(CarbonInputTransportAdaptorManagerService.class);

    /**
     * transport configuration map to keep the transport configuration details
     */

    public List<InputTransportAdaptorNotificationListener> inputTransportAdaptorNotificationListener;
    private Map<Integer, Map<String, InputTransportAdaptorConfiguration>> tenantSpecificTransportAdaptorConfigurationMap;
    private Map<Integer, Map<String, InputTransportAdaptorInfo>> tenantSpecificInputTransportAdaptorInfoMap;
    private Map<Integer, List<InputTransportAdaptorFile>> transportAdaptorFileMap;

    public CarbonInputTransportAdaptorManagerService() {
        tenantSpecificTransportAdaptorConfigurationMap = new ConcurrentHashMap<Integer, Map<String, InputTransportAdaptorConfiguration>>();
        transportAdaptorFileMap = new ConcurrentHashMap<Integer, List<InputTransportAdaptorFile>>();
        tenantSpecificInputTransportAdaptorInfoMap = new HashMap<Integer, Map<String, InputTransportAdaptorInfo>>();
        inputTransportAdaptorNotificationListener = new ArrayList<InputTransportAdaptorNotificationListener>();
    }

    public void registerDeploymentNotifier(
            InputTransportAdaptorNotificationListener inputTransportAdaptorNotificationListener)
            throws InputTransportAdaptorManagerConfigurationException {
        this.inputTransportAdaptorNotificationListener.add(inputTransportAdaptorNotificationListener);

        notifyTransportAdaptorConfigurationFiles();
    }

    /**
     * This method is used to store all the deployed and non-deployed transport adaptors in a map
     * (A flag used to uniquely identity whether the transport adaptor deployed or not
     *
     * @param tenantId
     * @param transportAdaptorName
     * @param filePath
     * @param status
     */
    public void addFileConfiguration(int tenantId, String transportAdaptorName, String filePath,
                                     InputTransportAdaptorFile.Status status,
                                     AxisConfiguration axisConfiguration,
                                     String deploymentStatusMessage, String dependency) {

        List<InputTransportAdaptorFile> inputTransportAdaptorFileList = transportAdaptorFileMap.get(tenantId);

        if (inputTransportAdaptorFileList == null) {
            inputTransportAdaptorFileList = new ArrayList<InputTransportAdaptorFile>();
        } else {
            Iterator<InputTransportAdaptorFile> transportAdaptorFileIterator = inputTransportAdaptorFileList.iterator();
            while (transportAdaptorFileIterator.hasNext()) {
                if (transportAdaptorFileIterator.next().getFilePath().equals(filePath)) {
                    return;
                }
            }
        }

        InputTransportAdaptorFile inputTransportAdaptorFile = new InputTransportAdaptorFile();
        inputTransportAdaptorFile.setFilePath(filePath);
        inputTransportAdaptorFile.setTransportAdaptorName(transportAdaptorName);
        inputTransportAdaptorFile.setAxisConfiguration(axisConfiguration);
        inputTransportAdaptorFile.setDependency(dependency);
        inputTransportAdaptorFile.setDeploymentStatusMessage(deploymentStatusMessage);
        inputTransportAdaptorFile.setStatus(status);
        inputTransportAdaptorFileList.add(inputTransportAdaptorFile);
        transportAdaptorFileMap.put(tenantId, inputTransportAdaptorFileList);


    }

    public void editInputTransportAdaptorConfigurationFile(String transportAdaptorConfiguration,
                                                           String transportAdaptorName,
                                                           AxisConfiguration axisConfiguration)
            throws InputTransportAdaptorManagerConfigurationException {
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();

        try {
            OMElement omElement = AXIOMUtil.stringToOM(transportAdaptorConfiguration);
            omElement.toString();
            InputTransportAdaptorConfiguration transportAdaptorConfigurationObject = InputTransportAdaptorConfigurationHelper.fromOM(omElement);
            if (!transportAdaptorConfigurationObject.getName().equals(transportAdaptorName)) {
                if (checkAdaptorValidity(tenantId, transportAdaptorConfigurationObject.getName())) {
                    validateToEditTransportAdaptorConfiguration(tenantId, transportAdaptorName, axisConfiguration, omElement);
                } else {
                    throw new InputTransportAdaptorManagerConfigurationException("There is a transport adaptor already registered with the same name");
                }
            } else {
                validateToEditTransportAdaptorConfiguration(tenantId, transportAdaptorName, axisConfiguration, omElement);
            }

        } catch (XMLStreamException e) {
            log.error("Error while creating the xml object");
            throw new InputTransportAdaptorManagerConfigurationException("Not a valid xml object, " + e.getMessage());
        }
    }

    public void editNotDeployedInputTransportAdaptorConfigurationFile(
            String transportAdaptorConfiguration,
            String filePath,
            AxisConfiguration axisConfiguration)
            throws InputTransportAdaptorManagerConfigurationException {
        try {
            OMElement omElement = AXIOMUtil.stringToOM(transportAdaptorConfiguration);
            omElement.toString();
            int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
            InputTransportAdaptorConfiguration transportAdaptorConfigurationObject = InputTransportAdaptorConfigurationHelper.fromOM(omElement);
            if (checkAdaptorValidity(tenantId, transportAdaptorConfigurationObject.getName())) {
                if (InputTransportAdaptorConfigurationHelper.validateTransportAdaptorConfiguration(transportAdaptorConfigurationObject)) {
                    removeInputTransportAdaptorConfigurationFile(filePath, axisConfiguration);
                    InputTransportAdaptorConfigurationFilesystemInvoker.saveConfigurationToFileSystem(omElement, transportAdaptorConfigurationObject.getName(), filePath, axisConfiguration);
                } else {
                    log.error("There is no transport adaptor type called " + transportAdaptorConfigurationObject.getType() + " is available");
                    throw new InputTransportAdaptorManagerConfigurationException("There is no transport adaptor type called " + transportAdaptorConfigurationObject.getType() + " is available ");
                }
            } else {
                throw new InputTransportAdaptorManagerConfigurationException("There is a transport adaptor with the same name");
            }
        } catch (XMLStreamException e) {
            log.error("Error while creating the xml object");
            throw new InputTransportAdaptorManagerConfigurationException("Not a valid xml object " + e.getMessage());
        }
    }

    @Override
    public void disableStatistics(String transportAdaptorName, AxisConfiguration axisConfiguration)
            throws InputTransportAdaptorManagerConfigurationException {

        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration = getInputTransportAdaptorConfiguration(transportAdaptorName, tenantId);
        inputTransportAdaptorConfiguration.setEnableStatistics(false);
        editTracingStatistics(inputTransportAdaptorConfiguration, transportAdaptorName, tenantId, axisConfiguration);
    }

    @Override
    public void enableStatistics(String transportAdaptorName, AxisConfiguration axisConfiguration)
            throws InputTransportAdaptorManagerConfigurationException {
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration = getInputTransportAdaptorConfiguration(transportAdaptorName, tenantId);
        inputTransportAdaptorConfiguration.setEnableStatistics(true);
        editTracingStatistics(inputTransportAdaptorConfiguration, transportAdaptorName, tenantId, axisConfiguration);
    }

    @Override
    public void disableTracing(String transportAdaptorName, AxisConfiguration axisConfiguration)
            throws InputTransportAdaptorManagerConfigurationException {
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration = getInputTransportAdaptorConfiguration(transportAdaptorName, tenantId);
        inputTransportAdaptorConfiguration.setEnableTracing(false);
        editTracingStatistics(inputTransportAdaptorConfiguration, transportAdaptorName, tenantId, axisConfiguration);
    }

    @Override
    public void enableTracing(String transportAdaptorName, AxisConfiguration axisConfiguration)
            throws InputTransportAdaptorManagerConfigurationException {
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration = getInputTransportAdaptorConfiguration(transportAdaptorName, tenantId);
        inputTransportAdaptorConfiguration.setEnableTracing(true);
        editTracingStatistics(inputTransportAdaptorConfiguration, transportAdaptorName, tenantId, axisConfiguration);
    }

    private void editTracingStatistics(
            InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration,
            String transportAdaptorName, int tenantId, AxisConfiguration axisConfiguration)
            throws InputTransportAdaptorManagerConfigurationException {

        String pathInFileSystem = getFilePath(tenantId, transportAdaptorName);
        removeInputTransportAdaptorConfiguration(transportAdaptorName, axisConfiguration);
        OMElement omElement = InputTransportAdaptorConfigurationHelper.transportAdaptorConfigurationToOM(inputTransportAdaptorConfiguration);
        InputTransportAdaptorConfigurationFilesystemInvoker.saveConfigurationToFileSystem(omElement, transportAdaptorName, pathInFileSystem, axisConfiguration);
    }


    public void saveInputTransportAdaptorConfiguration(
            InputTransportAdaptorConfiguration transportAdaptorConfiguration,
            AxisConfiguration axisConfiguration)
            throws InputTransportAdaptorManagerConfigurationException {

        String transportName = transportAdaptorConfiguration.getName();
        OMElement omElement = InputTransportAdaptorConfigurationHelper.transportAdaptorConfigurationToOM(transportAdaptorConfiguration);

        if (InputTransportAdaptorConfigurationHelper.validateTransportAdaptorConfiguration(InputTransportAdaptorConfigurationHelper.fromOM(omElement))) {
            File directory = new File(axisConfiguration.getRepository().getPath());
            if (!directory.exists()) {
                if (directory.mkdir()) {
                    throw new InputTransportAdaptorManagerConfigurationException("Cannot create directory to add tenant specific transport adaptor :" + transportName);
                }
            }
            directory = new File(directory.getAbsolutePath() + File.separator + InputTransportAdaptorManagerConstants.TM_ELE_DIRECTORY);
            if (!directory.exists()) {
                if (!directory.mkdir()) {
                    throw new InputTransportAdaptorManagerConfigurationException("Cannot create directory " + InputTransportAdaptorManagerConstants.TM_ELE_DIRECTORY + " to add tenant specific transport adaptor :" + transportName);
                }
            }
            String pathInFileSystem = directory.getAbsolutePath() + File.separator + transportName + ".xml";
            InputTransportAdaptorConfigurationFilesystemInvoker.saveConfigurationToFileSystem(omElement, transportName, pathInFileSystem, axisConfiguration);
        } else {
            log.error("There is no transport adaptor type called " + transportAdaptorConfiguration.getType() + " is available");
            throw new InputTransportAdaptorManagerConfigurationException("There is no transport adaptor type called " + transportAdaptorConfiguration.getType() + " is available ");
        }
    }


    public void removeInputTransportAdaptorConfiguration(String transportAdaptorName,
                                                         AxisConfiguration axisConfiguration)
            throws InputTransportAdaptorManagerConfigurationException {

        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        List<InputTransportAdaptorFile> inputTransportAdaptorFileList = transportAdaptorFileMap.get(tenantId);
        if (inputTransportAdaptorFileList != null) {
            Iterator<InputTransportAdaptorFile> transportAdaptorFileIterator = inputTransportAdaptorFileList.iterator();
            while (transportAdaptorFileIterator.hasNext()) {

                InputTransportAdaptorFile inputTransportAdaptorFile = transportAdaptorFileIterator.next();
                if ((inputTransportAdaptorFile.getTransportAdaptorName().equals(transportAdaptorName))) {

                    String filePath = inputTransportAdaptorFile.getFilePath();
                    InputTransportAdaptorConfigurationFilesystemInvoker.deleteTransportAdaptorFile(filePath, axisConfiguration);
                    break;
                }
            }
        }
    }

    public void removeInputTransportAdaptorConfigurationFile(String filePath,
                                                             AxisConfiguration axisConfiguration)
            throws InputTransportAdaptorManagerConfigurationException {

        InputTransportAdaptorConfigurationFilesystemInvoker.deleteTransportAdaptorFile(filePath, axisConfiguration);
    }

    @Override
    public String getInputTransportAdaptorConfigurationFile(String transportAdaptorName,
                                                            AxisConfiguration axisConfiguration)
            throws InputTransportAdaptorManagerConfigurationException {

        String filePath = "";
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        List<InputTransportAdaptorFile> inputTransportAdaptorFileList = transportAdaptorFileMap.get(tenantId);
        if (inputTransportAdaptorFileList != null) {
            Iterator<InputTransportAdaptorFile> transportAdaptorFileIterator = inputTransportAdaptorFileList.iterator();
            while (transportAdaptorFileIterator.hasNext()) {
                InputTransportAdaptorFile inputTransportAdaptorFile = transportAdaptorFileIterator.next();
                if ((inputTransportAdaptorFile.getTransportAdaptorName().equals(transportAdaptorName))) {

                    filePath = inputTransportAdaptorFile.getFilePath();

                }
            }
        }
        return readTransportAdaptorConfigurationFile(filePath);
    }

    public String getNotDeployedInputTransportAdaptorConfigurationFile(String filePath)
            throws InputTransportAdaptorManagerConfigurationException {

        return readTransportAdaptorConfigurationFile(filePath);
    }

    private String readTransportAdaptorConfigurationFile(String filePath)
            throws InputTransportAdaptorManagerConfigurationException {
        BufferedReader bufferedReader = null;
        StringBuffer stringBuffer = new StringBuffer();
        try {
            bufferedReader = new BufferedReader(new FileReader(filePath));
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line).append("\n");
            }
        } catch (FileNotFoundException e) {
            throw new InputTransportAdaptorManagerConfigurationException("Transport adaptor file not found ", e);
        } catch (IOException e) {
            throw new InputTransportAdaptorManagerConfigurationException("Cannot read the transport adaptor file ", e);
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
                throw new InputTransportAdaptorManagerConfigurationException("Error occurred when reading the file ", e);
            }
        }

        return stringBuffer.toString().trim();
    }


    public List<InputTransportAdaptorConfiguration> getAllInputTransportAdaptorConfiguration(
            AxisConfiguration axisConfiguration)
            throws InputTransportAdaptorManagerConfigurationException {

        List<InputTransportAdaptorConfiguration> transportAdaptorConfigurations = new ArrayList<InputTransportAdaptorConfiguration>();
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        if (tenantSpecificTransportAdaptorConfigurationMap.get(tenantId) != null) {
            for (InputTransportAdaptorConfiguration transportAdaptorConfiguration : tenantSpecificTransportAdaptorConfigurationMap.get(
                    tenantId).values()) {
                transportAdaptorConfigurations.add(transportAdaptorConfiguration);
            }
        }
        return transportAdaptorConfigurations;
    }

    public List<InputTransportAdaptorFile> getNotDeployedInputTransportAdaptorConfigurationFiles(
            AxisConfiguration axisConfiguration) {

        List<InputTransportAdaptorFile> unDeployedInputTransportAdaptorFileList = new ArrayList<InputTransportAdaptorFile>();
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        if (transportAdaptorFileMap.get(tenantId) != null) {
            for (InputTransportAdaptorFile inputTransportAdaptorFile : transportAdaptorFileMap.get(tenantId)) {
                if (!inputTransportAdaptorFile.getStatus().equals(InputTransportAdaptorFile.Status.DEPLOYED)) {
                    unDeployedInputTransportAdaptorFileList.add(inputTransportAdaptorFile);
                }
            }
        }
        return unDeployedInputTransportAdaptorFileList;
    }


    @Override
    public InputTransportAdaptorConfiguration getInputTransportAdaptorConfiguration(String name,
                                                                                    int tenantId)
            throws InputTransportAdaptorManagerConfigurationException {

        if (tenantSpecificTransportAdaptorConfigurationMap.get(tenantId) == null) {
            return null;
        }
        return tenantSpecificTransportAdaptorConfigurationMap.get(tenantId).get(name);
    }

    @Override
    public List<InputTransportAdaptorInfo> getInputTransportAdaptorInfo(int tenantId) {

        Map<String, InputTransportAdaptorInfo> inputTransportAdaptorInfoMap = tenantSpecificInputTransportAdaptorInfoMap.get(tenantId);

        if (inputTransportAdaptorInfoMap != null) {
            List<InputTransportAdaptorInfo> inputTransportAdaptorInfoList = new ArrayList<InputTransportAdaptorInfo>();
            for (InputTransportAdaptorInfo inputTransportAdaptorInfo : inputTransportAdaptorInfoMap.values()) {
                inputTransportAdaptorInfoList.add(inputTransportAdaptorInfo);
            }
            return inputTransportAdaptorInfoList;
        }
        return null;
    }


    /**
     * to add to the input adaptor maps that gives information which transport adaptors supports
     * for transport adaptor configuration
     *
     * @param tenantId
     * @param transportAdaptorConfiguration
     * @throws org.wso2.carbon.input.transport.adaptor.manager.core.exception.InputTransportAdaptorManagerConfigurationException
     *
     */
    private void addToTenantSpecificTransportAdaptorInfoMap(int tenantId,
                                                            InputTransportAdaptorConfiguration transportAdaptorConfiguration)
            throws InputTransportAdaptorManagerConfigurationException {

        if (transportAdaptorConfiguration.getInputConfiguration() != null) {
            InputTransportAdaptorInfo inputTransportAdaptorInfo = new InputTransportAdaptorInfo();
            inputTransportAdaptorInfo.setTransportAdaptorName(transportAdaptorConfiguration.getName());
            inputTransportAdaptorInfo.setTransportAdaptorType(transportAdaptorConfiguration.getType());
            Map<String, InputTransportAdaptorInfo> transportAdaptorInfoMap = tenantSpecificInputTransportAdaptorInfoMap.get(tenantId);

            if (transportAdaptorInfoMap != null) {

                transportAdaptorInfoMap.put(transportAdaptorConfiguration.getName(), inputTransportAdaptorInfo);
            } else {
                transportAdaptorInfoMap = new HashMap<String, InputTransportAdaptorInfo>();
                transportAdaptorInfoMap.put(transportAdaptorConfiguration.getName(), inputTransportAdaptorInfo);
            }
            tenantSpecificInputTransportAdaptorInfoMap.put(tenantId, transportAdaptorInfoMap);

        }

    }

    /**
     * to remove the transport adaptor configuration when deployed from the map after un-deploy
     *
     * @param tenantId
     * @param transportAdaptorName
     */
    private void removeFromTenantSpecificTransportAdaptorInfoMap(int tenantId,
                                                                 String transportAdaptorName) {

        Map<String, InputTransportAdaptorInfo> inputTransportAdaptorInfoMap = tenantSpecificInputTransportAdaptorInfoMap.get(tenantId);

        if (inputTransportAdaptorInfoMap != null && inputTransportAdaptorInfoMap.containsKey(transportAdaptorName)) {
            inputTransportAdaptorInfoMap.remove(transportAdaptorName);
        }

    }

    /**
     * To check whether there is a transport adaptor with the same name
     *
     * @param tenantId
     * @param transportAdaptorName
     * @return
     */
    public boolean checkAdaptorValidity(int tenantId, String transportAdaptorName) {

        if (transportAdaptorFileMap.size() > 0) {
            List<InputTransportAdaptorFile> inputTransportAdaptorFileList = transportAdaptorFileMap.get(tenantId);
            if (inputTransportAdaptorFileList != null) {
                Iterator<InputTransportAdaptorFile> transportAdaptorFileIterator = inputTransportAdaptorFileList.iterator();
                while (transportAdaptorFileIterator.hasNext()) {
                    InputTransportAdaptorFile inputTransportAdaptorFile = transportAdaptorFileIterator.next();
                    if ((inputTransportAdaptorFile.getTransportAdaptorName().equals(transportAdaptorName)) && (inputTransportAdaptorFile.getStatus().equals(InputTransportAdaptorFile.Status.DEPLOYED))) {
                        log.error("Transport adaptor " + transportAdaptorName + " is already registered with this tenant");
                        return false;
                    }
                }
            }
        }
        return true;
    }


    /**
     * to get the file path of a transport adaptor
     *
     * @param tenantId
     * @param transportAdaptorName
     * @return
     */
    private String getFilePath(int tenantId, String transportAdaptorName) {

        if (transportAdaptorFileMap.size() > 0) {
            List<InputTransportAdaptorFile> inputTransportAdaptorFileList = transportAdaptorFileMap.get(tenantId);
            if (inputTransportAdaptorFileList != null) {
                Iterator<InputTransportAdaptorFile> transportAdaptorFileIterator = inputTransportAdaptorFileList.iterator();
                while (transportAdaptorFileIterator.hasNext()) {
                    InputTransportAdaptorFile inputTransportAdaptorFile = transportAdaptorFileIterator.next();
                    if ((inputTransportAdaptorFile.getTransportAdaptorName().equals(transportAdaptorName))) {
                        return inputTransportAdaptorFile.getFilePath();
                    }
                }
            }
        }
        return null;
    }

    /**
     * this stores the transport adaptor configuration to the file system after validating the transport adaptor when doing editing
     *
     * @param tenantId
     * @param transportAdaptorName
     * @param axisConfiguration
     * @param omElement
     * @throws org.wso2.carbon.input.transport.adaptor.manager.core.exception.InputTransportAdaptorManagerConfigurationException
     *
     */
    private void validateToEditTransportAdaptorConfiguration(int tenantId,
                                                             String transportAdaptorName,
                                                             AxisConfiguration axisConfiguration,
                                                             OMElement omElement)
            throws InputTransportAdaptorManagerConfigurationException {
        InputTransportAdaptorConfiguration transportAdaptorConfiguration = InputTransportAdaptorConfigurationHelper.fromOM(omElement);
        if (InputTransportAdaptorConfigurationHelper.validateTransportAdaptorConfiguration(transportAdaptorConfiguration)) {
            String pathInFileSystem = getFilePath(tenantId, transportAdaptorName);
            removeInputTransportAdaptorConfiguration(transportAdaptorName, axisConfiguration);
            InputTransportAdaptorConfigurationFilesystemInvoker.saveConfigurationToFileSystem(omElement, transportAdaptorName, pathInFileSystem, axisConfiguration);
        } else {
            log.error("There is no transport adaptor type called " + transportAdaptorConfiguration.getType() + " is available ");
            throw new InputTransportAdaptorManagerConfigurationException("There is no transport adaptor type called " + transportAdaptorConfiguration.getType() + " is available ");
        }
    }

    /**
     * to remove the deployed and not-deployed the transport adaptor configuration from the map.
     *
     * @param filePath
     * @param tenantId
     */
    public void removeTransportAdaptorConfigurationFromMap(String filePath, int tenantId) {
        List<InputTransportAdaptorFile> inputTransportAdaptorFileList = transportAdaptorFileMap.get(tenantId);
        if (inputTransportAdaptorFileList != null) {
            Iterator<InputTransportAdaptorFile> transportAdaptorFileIterator = inputTransportAdaptorFileList.iterator();
            while (transportAdaptorFileIterator.hasNext()) {
                InputTransportAdaptorFile inputTransportAdaptorFile = transportAdaptorFileIterator.next();
                if ((inputTransportAdaptorFile.getFilePath().equals(filePath))) {
                    if (inputTransportAdaptorFile.getStatus().equals(InputTransportAdaptorFile.Status.DEPLOYED)) {
                        String transportAdaptorName = inputTransportAdaptorFile.getTransportAdaptorName();
                        removeFromTenantSpecificTransportAdaptorInfoMap(tenantId, transportAdaptorName);
                        Iterator<InputTransportAdaptorNotificationListener> deploymentListenerIterator = inputTransportAdaptorNotificationListener.iterator();
                        while (deploymentListenerIterator.hasNext()) {
                            InputTransportAdaptorNotificationListener inputTransportAdaptorNotificationListener = deploymentListenerIterator.next();
                            if (tenantSpecificTransportAdaptorConfigurationMap.get(tenantId) != null) {
                                inputTransportAdaptorNotificationListener.configurationRemoved(tenantId, tenantSpecificTransportAdaptorConfigurationMap.get(tenantId).get(transportAdaptorName));
                            }
                        }
                        if (tenantSpecificTransportAdaptorConfigurationMap.get(tenantId) != null) {
                            tenantSpecificTransportAdaptorConfigurationMap.get(tenantId).remove(transportAdaptorName);
                        }
                    }
                    inputTransportAdaptorFileList.remove(inputTransportAdaptorFile);
                    return;
                }
            }
        }
    }

    /**
     * to add to the tenant specific transport adaptor configuration map (only the correctly deployed transport adaptors)
     *
     * @param tenantId
     * @param transportAdaptorConfiguration
     * @throws org.wso2.carbon.input.transport.adaptor.manager.core.exception.InputTransportAdaptorManagerConfigurationException
     *
     */
    public void addTransportAdaptorConfigurationForTenant(
            int tenantId, InputTransportAdaptorConfiguration transportAdaptorConfiguration)
            throws InputTransportAdaptorManagerConfigurationException {
        Map<String, InputTransportAdaptorConfiguration> transportAdaptorConfigurationMap
                = tenantSpecificTransportAdaptorConfigurationMap.get(tenantId);

        if (transportAdaptorConfigurationMap == null) {
            transportAdaptorConfigurationMap = new ConcurrentHashMap<String, InputTransportAdaptorConfiguration>();
            transportAdaptorConfigurationMap.put(transportAdaptorConfiguration.getName(), transportAdaptorConfiguration);
            tenantSpecificTransportAdaptorConfigurationMap.put(tenantId, transportAdaptorConfigurationMap);
        } else {
            transportAdaptorConfigurationMap.put(transportAdaptorConfiguration.getName(), transportAdaptorConfiguration);
        }
        addToTenantSpecificTransportAdaptorInfoMap(tenantId, transportAdaptorConfiguration);
    }


    public void tryReDeployingTransportAdaptorConfigurationFiles()
            throws InputTransportAdaptorManagerConfigurationException {

        List<InputTransportAdaptorFile> inputTransportAdaptorFiles = new ArrayList<InputTransportAdaptorFile>();

        if (transportAdaptorFileMap != null && transportAdaptorFileMap.size() > 0) {

            Iterator transportAdaptorEntryIterator = transportAdaptorFileMap.entrySet().iterator();
            while (transportAdaptorEntryIterator.hasNext()) {
                Map.Entry transportAdaptorEntry = (Map.Entry) transportAdaptorEntryIterator.next();
                Iterator<InputTransportAdaptorFile> transportAdaptorFileIterator = ((List<InputTransportAdaptorFile>) transportAdaptorEntry.getValue()).iterator();
                while (transportAdaptorFileIterator.hasNext()) {
                    InputTransportAdaptorFile inputTransportAdaptorFile = transportAdaptorFileIterator.next();
                    if (inputTransportAdaptorFile.getStatus().equals(InputTransportAdaptorFile.Status.WAITING_FOR_DEPENDENCY)) {
                        inputTransportAdaptorFiles.add(inputTransportAdaptorFile);
                        transportAdaptorFileIterator.remove();
                    }
                }
            }
        }

        for (int i = 0; i < inputTransportAdaptorFiles.size(); i++) {
            InputTransportAdaptorFile inputTransportAdaptorFile = inputTransportAdaptorFiles.get(i);
            try {
                InputTransportAdaptorConfigurationFilesystemInvoker.reload(inputTransportAdaptorFile.getFilePath(), inputTransportAdaptorFile.getAxisConfiguration());
            } catch (Exception e) {
                log.error("Exception occurred while trying to deploy the Input Transport Adaptor configuration file : " + new File(inputTransportAdaptorFile.getFilePath()).getName());
            }

        }
    }

    public void notifyTransportAdaptorConfigurationFiles()
            throws InputTransportAdaptorManagerConfigurationException {

        if (transportAdaptorFileMap != null && transportAdaptorFileMap.size() > 0) {
            Iterator transportAdaptorEntryIterator = transportAdaptorFileMap.entrySet().iterator();
            while (transportAdaptorEntryIterator.hasNext()) {
                Map.Entry transportAdaptorEntry = (Map.Entry) transportAdaptorEntryIterator.next();
                Iterator<InputTransportAdaptorFile> transportAdaptorFileIterator = ((List<InputTransportAdaptorFile>) transportAdaptorEntry.getValue()).iterator();
                while (transportAdaptorFileIterator.hasNext()) {
                    InputTransportAdaptorFile inputTransportAdaptorFile = transportAdaptorFileIterator.next();
                    if (inputTransportAdaptorFile.getStatus().equals(InputTransportAdaptorFile.Status.DEPLOYED)) {
                        int tenantId = PrivilegedCarbonContext.getCurrentContext(inputTransportAdaptorFile.getAxisConfiguration()).getTenantId();
                        Iterator<InputTransportAdaptorNotificationListener> deploymentListenerIterator = inputTransportAdaptorNotificationListener.iterator();
                        while (deploymentListenerIterator.hasNext()) {
                            InputTransportAdaptorNotificationListener inputTransportAdaptorNotificationListener = deploymentListenerIterator.next();
                            inputTransportAdaptorNotificationListener.configurationAdded(tenantId, inputTransportAdaptorFile.getTransportAdaptorName());
                        }
                    }
                }
            }
        }
    }


}