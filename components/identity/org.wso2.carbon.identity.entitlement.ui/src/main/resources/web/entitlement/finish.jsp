<!--
 ~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ WSO2 Inc. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~    http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied.  See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 -->
<%@ page import="org.apache.axis2.context.ConfigurationContext"%>
<%@ page import="org.wso2.carbon.CarbonConstants"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil"%>
<%@ page import="org.wso2.carbon.utils.ServerConstants"%>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.client.EntitlementPolicyAdminServiceClient"%>
<%@ page import="java.util.ResourceBundle"%>
<%@ page import="java.util.List" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyCreator" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.*" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyCreationException" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyConstants" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.util.PolicyEditorUtil" %>
<jsp:useBean id="entitlementPolicyBean" type="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean"
             class="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean" scope="session"/>
<jsp:setProperty name="entitlementPolicyBean" property="*" />
<%

    String ruleElementOrder = request.getParameter("ruleElementOrder");
    if(ruleElementOrder != null && ruleElementOrder.trim().length() > 0){
        entitlementPolicyBean.setRuleElementOrder(ruleElementOrder.trim());
    }
    
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.
                    CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
	String forwardTo = null;
	String BUNDLE = "org.wso2.carbon.identity.entitlement.ui.i18n.Resources";
	ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

    org.wso2.carbon.identity.entitlement.stub.dto.PolicyDTO policy = null;
    String policyName = entitlementPolicyBean.getPolicyName();
    String algorithmName = entitlementPolicyBean.getAlgorithmName();
    String policyDescription = entitlementPolicyBean.getPolicyDescription();

    List<RuleDTO> ruleDTOs = entitlementPolicyBean.getRuleDTOs();
    TargetDTO targetDTO = entitlementPolicyBean.getTargetDTO();
    List<ObligationDTO> obligationDTOs = entitlementPolicyBean.getObligationDTOs();



    PolicyDTO  policyDTO = new PolicyDTO();
    if(policyName != null && policyName.trim().length() > 0 && algorithmName != null
            && algorithmName.trim().length() > 0) {
        policyDTO.setPolicyId(policyName);
        policyDTO.setRuleAlgorithm(algorithmName);
        policyDTO.setDescription(policyDescription);
        policyDTO.setRuleOrder(ruleElementOrder);
    }

    policyDTO.setRuleDTOs(ruleDTOs);
    policyDTO.setTargetDTO(targetDTO);
    policyDTO.setObligationDTOs(obligationDTOs);

    try {

        EntitlementPolicyAdminServiceClient client = new EntitlementPolicyAdminServiceClient(cookie,
                serverURL, configContext);
        try{
            policy = client.getPolicy(policyName);
        } catch (Exception e){
            //ignore
        }

        if(policy == null){
            policy = new  org.wso2.carbon.identity.entitlement.stub.dto.PolicyDTO();
        }


            policyMetaData = PolicyEditorUtil.processPolicyData(targetDTO, ruleDTOs, obligationDTOs,
                                                            ruleElementOrder, entitlementPolicyBean);
            policy = policyCreator.createBasicPolicy(policyElement, ruleDTOs, targetDTO, obligationDTOs);
            policyDTO.setPolicyEditor(EntitlementPolicyConstants.BASIC_POLICY_EDITOR);
            if(policyMetaData != null){
                policyDTO.setBasicPolicyEditorMetaData(policyMetaData);
            }

        } else {
            policy = policyCreator.createPolicy(policyElement, subElementDTOs, ruleElementDTOs);
        }

        policyDTO.setPolicyId(policyName);
        policyDTO.setPolicy(policy);

        if(entitlementPolicyBean.isEditPolicy()){
            client.updatePolicy(policyDTO);    
        } else {
            client.addPolicy(policyDTO);
        }
        entitlementPolicyBean.cleanEntitlementPolicyBean();
        String message = resourceBundle.getString("ent.policy.added.successfully");
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.INFO, request);
        forwardTo = "index.jsp?";
    } catch (EntitlementPolicyCreationException e) {
        String message = resourceBundle.getString("error.while.creating.policy");
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
        forwardTo = "index.jsp?";
    } catch (Exception e) {
        String message = resourceBundle.getString("error.while.adding.policy") + " " + e.getMessage();
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
        forwardTo = "index.jsp?";
    }
%>
<script
	type="text/javascript">
    function forward() {
        location.href = "<%=forwardTo%>";
	}
</script>

<script type="text/javascript">
	forward();
</script>