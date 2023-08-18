package com.huaweicloud.governance.authentication.securityPolicy;

import java.util.Map;

import com.huaweicloud.governance.authentication.AccessController;
import com.huaweicloud.governance.authentication.AuthenticationAdapter;

/**
 * Add security policy list control to service access
 */
public class SecurityPolicyAccessController implements AccessController {
  private SecurityPolicyProperties securityPolicyProperties;

  private final AuthenticationAdapter authenticationAdapter;

  public SecurityPolicyAccessController(AuthenticationAdapter authenticationAdapter,
      SecurityPolicyProperties securityPolicyProperties) {
    this.authenticationAdapter = authenticationAdapter;
    this.securityPolicyProperties = securityPolicyProperties;
  }

  @Override
  public boolean isAllowed(String serviceId, String instanceId, Map<String, String> requestMap) {
    return checkAllow(serviceId, requestMap) && !checkDeny(serviceId, requestMap);
  }

  private boolean checkDeny(String serviceId, Map<String, String> requestMap) {
    // Forced mode, white policy not match or black policy match intercept
    if (securityPolicyProperties.matchDeny(serviceId, requestMap.get("uri"), requestMap.get("method"))) {
      if ("permissive".equals(securityPolicyProperties.getMode())) {
        //TODO sending alarm message
        return false;
      } else {
        return true;
      }
    } else {
      return false;
    }
  }

  private boolean checkAllow(String serviceId, Map<String, String> requestMap) {
    // Tolerance mode, white policy not match or black policy match allow passing
    if (securityPolicyProperties.matchAllow(serviceId, requestMap.get("uri"), requestMap.get("method"))) {
      return true;
    } else {
      if ("permissive".equals(securityPolicyProperties.getMode())) {
        //TODO sending alarm message
        return true;
      } else {
        return false;
      }
    }
  }

  @Override
  public String getPublicKeyFromInstance(String instanceId, String serviceId) {
    return authenticationAdapter.getPublicKeyFromInstance(instanceId, serviceId);
  }

  @Override
  public String interceptMessage() {
    return "UNAUTHORIZED BY SECURITY POLICY";
  }
}
