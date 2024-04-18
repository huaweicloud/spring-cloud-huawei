/*

 * Copyright (C) 2020-2024 Huawei Technologies Co., Ltd. All rights reserved.

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.huaweicloud.governance.authentication.securityPolicy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.huaweicloud.governance.authentication.AuthRequestExtractor;
import com.huaweicloud.governance.authentication.AuthRequestExtractorUtils;
import com.huaweicloud.governance.authentication.AuthenticationAdapter;
import com.huaweicloud.governance.authentication.securityPolicy.SecurityPolicyProperties.Action;
import com.huaweicloud.governance.authentication.securityPolicy.SecurityPolicyProperties.ConfigurationItem;


public class SecurityPolicyAccessControllerTest {
  private final AuthenticationAdapter authenticationAdapter = Mockito.mock(AuthenticationAdapter.class);

  private SecurityPolicyProperties securityPolicyProperties = new SecurityPolicyProperties();

  private AuthRequestExtractor createAuthRequestExtractor(String requestURI) {
    HttpServletRequest request = new HttpServletRequest() {
      @Override
      public String getAuthType() {
        return null;
      }

      @Override
      public Cookie[] getCookies() {
        return new Cookie[0];
      }

      @Override
      public long getDateHeader(String s) {
        return 0;
      }

      @Override
      public String getHeader(String s) {
        return "order";
      }

      @Override
      public Enumeration<String> getHeaders(String s) {
        return null;
      }

      @Override
      public Enumeration<String> getHeaderNames() {
        return null;
      }

      @Override
      public int getIntHeader(String s) {
        return 0;
      }

      @Override
      public String getMethod() {
        return "GET";
      }

      @Override
      public String getPathInfo() {
        return null;
      }

      @Override
      public String getPathTranslated() {
        return null;
      }

      @Override
      public String getContextPath() {
        return null;
      }

      @Override
      public String getQueryString() {
        return null;
      }

      @Override
      public String getRemoteUser() {
        return null;
      }

      @Override
      public boolean isUserInRole(String s) {
        return false;
      }

      @Override
      public Principal getUserPrincipal() {
        return null;
      }

      @Override
      public String getRequestedSessionId() {
        return null;
      }

      @Override
      public String getRequestURI() {
        return requestURI;
      }

      @Override
      public StringBuffer getRequestURL() {
        return null;
      }

      @Override
      public String getServletPath() {
        return null;
      }

      @Override
      public HttpSession getSession(boolean b) {
        return null;
      }

      @Override
      public HttpSession getSession() {
        return null;
      }

      @Override
      public String changeSessionId() {
        return null;
      }

      @Override
      public boolean isRequestedSessionIdValid() {
        return false;
      }

      @Override
      public boolean isRequestedSessionIdFromCookie() {
        return false;
      }

      @Override
      public boolean isRequestedSessionIdFromURL() {
        return false;
      }

      @Deprecated
      @Override
      public boolean isRequestedSessionIdFromUrl() {
        return false;
      }

      @Override
      public boolean authenticate(HttpServletResponse httpServletResponse) throws IOException, ServletException {
        return false;
      }

      @Override
      public void login(String s, String s1) throws ServletException {

      }

      @Override
      public void logout() throws ServletException {

      }

      @Override
      public Collection<Part> getParts() throws IOException, ServletException {
        return null;
      }

      @Override
      public Part getPart(String s) throws IOException, ServletException {
        return null;
      }

      @Override
      public <T extends HttpUpgradeHandler> T upgrade(Class<T> aClass) throws IOException, ServletException {
        return null;
      }

      @Override
      public Object getAttribute(String s) {
        return null;
      }

      @Override
      public Enumeration<String> getAttributeNames() {
        return null;
      }

      @Override
      public String getCharacterEncoding() {
        return null;
      }

      @Override
      public void setCharacterEncoding(String s) throws UnsupportedEncodingException {

      }

      @Override
      public int getContentLength() {
        return 0;
      }

      @Override
      public long getContentLengthLong() {
        return 0;
      }

      @Override
      public String getContentType() {
        return null;
      }

      @Override
      public ServletInputStream getInputStream() throws IOException {
        return null;
      }

      @Override
      public String getParameter(String s) {
        return null;
      }

      @Override
      public Enumeration<String> getParameterNames() {
        return null;
      }

      @Override
      public String[] getParameterValues(String s) {
        return new String[0];
      }

      @Override
      public Map<String, String[]> getParameterMap() {
        return null;
      }

      @Override
      public String getProtocol() {
        return null;
      }

      @Override
      public String getScheme() {
        return null;
      }

      @Override
      public String getServerName() {
        return null;
      }

      @Override
      public int getServerPort() {
        return 0;
      }

      @Override
      public BufferedReader getReader() throws IOException {
        return null;
      }

      @Override
      public String getRemoteAddr() {
        return null;
      }

      @Override
      public String getRemoteHost() {
        return null;
      }

      @Override
      public void setAttribute(String s, Object o) {

      }

      @Override
      public void removeAttribute(String s) {

      }

      @Override
      public Locale getLocale() {
        return null;
      }

      @Override
      public Enumeration<Locale> getLocales() {
        return null;
      }

      @Override
      public boolean isSecure() {
        return false;
      }

      @Override
      public RequestDispatcher getRequestDispatcher(String s) {
        return null;
      }

      @Deprecated
      @Override
      public String getRealPath(String s) {
        return null;
      }

      @Override
      public int getRemotePort() {
        return 0;
      }

      @Override
      public String getLocalName() {
        return null;
      }

      @Override
      public String getLocalAddr() {
        return null;
      }

      @Override
      public int getLocalPort() {
        return 0;
      }

      @Override
      public ServletContext getServletContext() {
        return null;
      }

      @Override
      public AsyncContext startAsync() throws IllegalStateException {
        return null;
      }

      @Override
      public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse)
          throws IllegalStateException {
        return null;
      }

      @Override
      public boolean isAsyncStarted() {
        return false;
      }

      @Override
      public boolean isAsyncSupported() {
        return false;
      }

      @Override
      public AsyncContext getAsyncContext() {
        return null;
      }

      @Override
      public DispatcherType getDispatcherType() {
        return null;
      }
    };
    return AuthRequestExtractorUtils.createWebMvcAuthRequestExtractor(request, "", "");
  }

  @Test
  public void testAllowPermissiveMatch() throws Exception {
    AuthRequestExtractor extractor = createAuthRequestExtractor("/checkTokenSecurityAllow");
    Assertions.assertTrue(getAllowAccessController("permissive")
        .isAllowed(extractor));
  }

  @Test
  public void testDenyPermissiveMatch() throws Exception {
    AuthRequestExtractor extractor = createAuthRequestExtractor("/checkTokenSecurityDeny");
    Assertions.assertTrue(getDenyAccessController("permissive")
        .isAllowed(extractor));
  }

  @Test
  public void testAllowPermissiveNotMatch() throws Exception {
    AuthRequestExtractor extractor = createAuthRequestExtractor("/checkToken");
    Assertions.assertTrue(getAllowAccessController("permissive")
        .isAllowed(extractor));
  }

  @Test
  public void testDenyPermissiveNotMatch() throws Exception {
    AuthRequestExtractor extractor = createAuthRequestExtractor("/checkTokenSecurity");
    Assertions.assertTrue(getDenyAccessController("permissive")
        .isAllowed(extractor));
  }

  @Test
  public void testAllowEnforcingMatch() throws Exception {
    AuthRequestExtractor extractor = createAuthRequestExtractor("/checkTokenSecurityAllow");
    Assertions.assertTrue(getAllowAccessController("enforcing")
        .isAllowed(extractor));
  }

  @Test
  public void testDenyEnforcingMatch() throws Exception {
    AuthRequestExtractor extractor = createAuthRequestExtractor("/checkTokenSecurityDeny");
    Assertions.assertFalse(getDenyAccessController("enforcing")
        .isAllowed(extractor));
  }

  @Test
  public void testAllowEnforcingNotMatch() throws Exception {
    AuthRequestExtractor extractor = createAuthRequestExtractor("/checkToken");
    Assertions.assertFalse(getAllowAccessController("enforcing")
        .isAllowed(extractor));
  }

  @Test
  public void testDenyEnforcingNotMatch() throws Exception {
    AuthRequestExtractor extractor = createAuthRequestExtractor("/checkTokenSecurityAllow");
    Assertions.assertTrue(getBothAccessController("enforcing")
        .isAllowed(extractor));
  }

  @Test
  public void testPermissiveBothMatch() throws Exception {
    AuthRequestExtractor extractor = createAuthRequestExtractor("/checkTokenSecurityBoth");
    Assertions.assertTrue(getBothAccessController("permissive")
        .isAllowed(extractor));
  }

  @Test
  public void testPermissiveBothNotMatch() throws Exception {
    AuthRequestExtractor extractor = createAuthRequestExtractor("/checkToken");
    Assertions.assertTrue(getBothAccessController("permissive")
        .isAllowed(extractor));
  }

  @Test
  public void testPermissiveAllowMatchDenyNotMatch() throws Exception {
    AuthRequestExtractor extractor = createAuthRequestExtractor("/checkTokenSecurityAllow");
    Assertions.assertTrue(getBothAccessController("permissive")
        .isAllowed(extractor));
  }

  @Test
  public void testPermissiveAllowNotMatchDenyMatch() throws Exception {
    AuthRequestExtractor extractor = createAuthRequestExtractor("/checkTokenSecurityDeny");
    Assertions.assertTrue(getBothAccessController("permissive")
        .isAllowed(extractor));
  }

  @Test
  public void testEnforcingBothMatch() throws Exception {
    AuthRequestExtractor extractor = createAuthRequestExtractor("/checkTokenSecurityBoth");
    Assertions.assertFalse(getBothAccessController("enforcing")
        .isAllowed(extractor));
  }

  @Test
  public void testEnforcingBothNotMatch() throws Exception {
    AuthRequestExtractor extractor = createAuthRequestExtractor("/checkToken");
    Assertions.assertFalse(getBothAccessController("enforcing")
        .isAllowed(extractor));
  }

  @Test
  public void testEnforcingAllowMatchDenyNotMatch() throws Exception {
    AuthRequestExtractor extractor = createAuthRequestExtractor("/checkTokenSecurityAllow");
    Assertions.assertTrue(getBothAccessController("enforcing")
        .isAllowed(extractor));
  }

  @Test
  public void testEnforcingAllowNotMatchDenyMatch() throws Exception {
    AuthRequestExtractor extractor = createAuthRequestExtractor("/checkTokenSecurityDeny");
    Assertions.assertFalse(getBothAccessController("enforcing")
        .isAllowed(extractor));
  }

  @Test
  public void testUriPrefixMatch() throws Exception {
    AuthRequestExtractor extractor = createAuthRequestExtractor("/checkTokenPre/security/allow");
    Assertions.assertTrue(getAllowAccessController("enforcing")
        .isAllowed(extractor));
  }

  @Test
  public void testUriPrefixNotMatch() throws Exception {
    AuthRequestExtractor extractor = createAuthRequestExtractor("/checkTokenPer/security/allow");
    Assertions.assertFalse(getAllowAccessController("enforcing")
        .isAllowed(extractor));
  }

  @Test
  public void testUriSuffixMatch() throws Exception {
    AuthRequestExtractor extractor = createAuthRequestExtractor("/checkTokenPer/security/checkTokenSuf");
    Assertions.assertTrue(getAllowAccessController("enforcing")
        .isAllowed(extractor));
  }

  @Test
  public void testUriSuffixNotMatch() throws Exception {
    AuthRequestExtractor extractor = createAuthRequestExtractor("/checkTokenPer/security/checkTokenSfu");
    Assertions.assertFalse(getAllowAccessController("enforcing")
        .isAllowed(extractor));
  }

  @Test
  public void testPolicyIsNull() throws Exception {
    AuthRequestExtractor extractor = createAuthRequestExtractor("/checkTokenPer/security/checkTokenSfu");
    Assertions.assertTrue(getNoSettingAccessController()
        .isAllowed(extractor));
  }

  private SecurityPolicyAccessController getNoSettingAccessController() {
    securityPolicyProperties.setAction(null);
    securityPolicyProperties.setMode(null);
    return new SecurityPolicyAccessController(authenticationAdapter, securityPolicyProperties);
  }

  private SecurityPolicyAccessController getAllowAccessController(String mode) {
    Action action = new Action();
    action.setAllow(buildAllow());
    securityPolicyProperties.setAction(action);
    securityPolicyProperties.setMode(mode);
    return new SecurityPolicyAccessController(authenticationAdapter, securityPolicyProperties);
  }

  private SecurityPolicyAccessController getDenyAccessController(String mode) {
    Action action = new Action();
    action.setDeny(buildDeny());
    securityPolicyProperties.setAction(action);
    securityPolicyProperties.setMode(mode);
    return new SecurityPolicyAccessController(authenticationAdapter, securityPolicyProperties);
  }

  private SecurityPolicyAccessController getBothAccessController(String mode) {
    Action action = new Action();
    action.setAllow(buildAllow());
    action.setDeny(buildDeny());
    securityPolicyProperties.setAction(action);
    securityPolicyProperties.setMode(mode);
    return new SecurityPolicyAccessController(authenticationAdapter, securityPolicyProperties);
  }

  private List<ConfigurationItem> buildDeny() {
    List<ConfigurationItem> list = new ArrayList<>();
    ConfigurationItem configurationItem = new ConfigurationItem();
    configurationItem.setConsumer("order");
    configurationItem.setId("1");
    configurationItem.setMethod("GET");
    configurationItem.setUri("/checkTokenSecurityDeny");
    list.add(configurationItem);
    list.add(buildBoth());
    return list;
  }

  private List<ConfigurationItem> buildAllow() {
    List<ConfigurationItem> list = new ArrayList<>();
    ConfigurationItem configurationItem = new ConfigurationItem();
    configurationItem.setConsumer("order");
    configurationItem.setId("1");
    configurationItem.setMethod("GET");
    configurationItem.setUri("/checkTokenSecurityAllow");
    list.add(configurationItem);
    list.add(buildBoth());
    list.add(buildUriPrefix());
    list.add(buildUriSuffix());
    return list;
  }

  private ConfigurationItem buildBoth() {
    ConfigurationItem configurationItem = new ConfigurationItem();
    configurationItem.setConsumer("order");
    configurationItem.setId("1");
    configurationItem.setMethod("GET");
    configurationItem.setUri("/checkTokenSecurityBoth");
    return configurationItem;
  }

  private ConfigurationItem buildUriPrefix() {
    ConfigurationItem configurationItem = new ConfigurationItem();
    configurationItem.setConsumer("order");
    configurationItem.setId("1");
    configurationItem.setMethod("GET");
    configurationItem.setUri("/checkTokenPre/*");
    return configurationItem;
  }

  private ConfigurationItem buildUriSuffix() {
    ConfigurationItem configurationItem = new ConfigurationItem();
    configurationItem.setConsumer("order");
    configurationItem.setId("1");
    configurationItem.setMethod("GET");
    configurationItem.setUri("*/checkTokenSuf");
    return configurationItem;
  }
}
