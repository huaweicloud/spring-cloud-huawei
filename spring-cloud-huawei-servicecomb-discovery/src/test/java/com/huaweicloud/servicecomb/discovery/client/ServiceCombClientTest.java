/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huaweicloud.servicecomb.discovery.client;

import java.io.IOException;
import java.util.List;

import java.util.Map;
import org.apache.http.HttpEntity;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.cloud.client.ServiceInstance;
import com.huaweicloud.common.exception.RemoteOperationException;
import com.huaweicloud.common.exception.RemoteServerUnavailableException;
import com.huaweicloud.common.exception.ServiceCombException;
import com.huaweicloud.common.transport.DefaultHttpTransport;
import com.huaweicloud.common.transport.HttpTransport;
import com.huaweicloud.common.transport.Response;
import com.huaweicloud.servicecomb.discovery.client.model.HeartbeatRequest;
import com.huaweicloud.servicecomb.discovery.client.model.Microservice;
import com.huaweicloud.servicecomb.discovery.client.model.MicroserviceInstance;
import com.huaweicloud.servicecomb.discovery.client.model.MicroserviceInstanceSingleResponse;
import com.huaweicloud.servicecomb.discovery.client.model.MicroserviceInstancesResponse;
import com.huaweicloud.servicecomb.discovery.client.model.MicroserviceResponse;

import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Injectable;

/**
 * @Author wangqijun
 * @Date 15:18 2019-08-19
 **/
public class ServiceCombClientTest {

  private static final String url = "http://127.0.0.1:30100";

  @Test
  public void getServiceCenterInstances(@Injectable
      HttpTransport httpTransport)
      throws RemoteOperationException, RemoteServerUnavailableException {
    final int expectedCode = 200;
    Response response = new Response();
    response.setStatusCode(expectedCode);
    String responseString = "{\n"
        + "  \"instances\": [\n"
        + "    {\n"
        + "      \"instanceId\": \"111\",\n"
        + "      \"serviceId\": \"222\",\n"
        + "      \"version\": \"0.1\",\n"
        + "      \"hostName\": \"string\",\n"
        + "      \"endpoints\": [\n"
        + "        \"string\"\n"
        + "      ],\n"
        + "      \"status\": \"UP\",\n"
        + "      \"timestamp\": \"212121212121\",\n"
        + "      \"modTimestamp\": \"432432432432\"\n"
        + "    }\n"
        + "  ]\n"
        + "}";

    response.setContent(responseString);

    new Expectations() {
      {
        Deencapsulation.newUninitializedInstance(DefaultHttpTransport.class);
        result = httpTransport;
        httpTransport.sendGetRequest(anyString);
        result = response;
      }
    };
    ServiceCombClient serviceCombClient = new ServiceCombClient(url, httpTransport);
    MicroserviceInstancesResponse actual = serviceCombClient
        .getServiceCenterInstances();
    Assert.assertNotNull(actual);
    Assert.assertEquals(actual.getInstances().size(), 1);
    Assert.assertEquals(actual.getInstances().get(0).getServiceId(), "222");
    Assert.assertEquals(actual.getInstances().get(0).getInstanceId(), "111");
  }


  @Test
  public void registerMicroservice(@Injectable
      HttpTransport httpTransport) throws ServiceCombException, IOException {
    final int expectedCode = 200;
    Response response = new Response();
    response.setStatusCode(expectedCode);
    String responseString = "{\"serviceId\": \"22222\"}";

    response.setContent(responseString);

    Microservice microservice = new Microservice();
    new Expectations() {
      {
        Deencapsulation.newUninitializedInstance(DefaultHttpTransport.class);
        result = httpTransport;
        httpTransport.sendPostRequest(anyString, (HttpEntity) any);
        result = response;
      }
    };
    ServiceCombClient serviceCombClient = new ServiceCombClient(url, httpTransport);
    String actual = serviceCombClient.registerMicroservice(microservice);
    Assert.assertEquals("22222", actual);
  }

  @Test
  public void getServiceId(@Injectable
      Boolean autoDiscovery, @Injectable
      HttpTransport httpTransport) throws ServiceCombException, IOException {
    final int expectedCode = 200;
    Response response = new Response();
    response.setStatusCode(expectedCode);
    String responseString = "{\"serviceId\": \"22222\"}";

    response.setContent(responseString);

    autoDiscovery = false;

    Microservice microservice = new Microservice();
    new Expectations() {
      {
        Deencapsulation.newUninitializedInstance(DefaultHttpTransport.class);
        result = httpTransport;
        httpTransport.sendGetRequest(anyString);
        result = response;
      }
    };
    ServiceCombClient serviceCombClient = new ServiceCombClient(url, httpTransport);
    String actual = serviceCombClient.getServiceId(microservice);
    Assert.assertEquals("22222", actual);
  }

  @Test
  public void registerInstance(@Injectable
      HttpTransport httpTransport) throws ServiceCombException, IOException {
    final int expectedCode = 200;
    Response response = new Response();
    response.setStatusCode(expectedCode);
    String responseString = "{\"instanceId\": \"22222\"}";

    response.setContent(responseString);

    MicroserviceInstance microserviceInstance = new MicroserviceInstance();
    microserviceInstance.setServiceId("111111");
    new Expectations() {
      {
        Deencapsulation.newUninitializedInstance(DefaultHttpTransport.class);
        result = httpTransport;
        httpTransport.sendPostRequest(anyString, (HttpEntity) any);
        result = response;
      }
    };
    ServiceCombClient serviceCombClient = new ServiceCombClient(url, httpTransport);
    String actual = serviceCombClient.registerInstance(microserviceInstance);
    Assert.assertEquals("22222", actual);
  }

  @Test
  public void getInstances(@Injectable
      HttpTransport httpTransport) throws ServiceCombException, IOException {
    final int expectedCode = 200;
    Response response = new Response();
    response.setStatusCode(expectedCode);
    String responseString = "{\n"
        + "  \"instances\": [\n"
        + "    {\n"
        + "      \"instanceId\": \"1\",\n"
        + "      \"serviceId\": \"2\",\n"
        + "      \"version\": \"0.1\",\n"
        + "      \"hostName\": \"string\",\n"
        + "      \"endpoints\": [\n"
        + "        \"string\"\n"
        + "      ],\n"
        + "      \"status\": \"UP\",\n"
        + "      \"dataCenterInfo\": {\n"
        + "        \"name\": \"string\",\n"
        + "        \"region\": \"string\",\n"
        + "        \"availableZone\": \"string\"\n"
        + "      },\n"
        + "      \"timestamp\": \"string\",\n"
        + "      \"modTimestamp\": \"string\"\n"
        + "    }\n"
        + "  ]\n"
        + "}";

    response.setContent(responseString);

    Microservice microservice = new Microservice();
    new Expectations() {
      {
        Deencapsulation.newUninitializedInstance(DefaultHttpTransport.class);
        result = httpTransport;
        httpTransport.sendGetRequest(anyString, (Map<String, String>) any);
        result = response;
      }
    };
    ServiceCombClient serviceCombClient = new ServiceCombClient(url, httpTransport);
    List<ServiceInstance> actual = serviceCombClient.getInstances(microservice, null);
    Assert.assertEquals(1, actual.size());
  }

  @Test
  public void heartbeat(@Injectable
      HttpTransport httpTransport) throws ServiceCombException, IOException {
    final int expectedCode = 200;
    Response response = new Response();
    response.setStatusCode(expectedCode);
    String responseString = "{\"serviceId\": \"22222\"}";

    response.setContent(responseString);

    HeartbeatRequest heartbeatRequest = new HeartbeatRequest("1", "2");
    new Expectations() {
      {
        Deencapsulation.newUninitializedInstance(DefaultHttpTransport.class);
        result = httpTransport;
        httpTransport.sendPutRequest(anyString, (HttpEntity) any);
        result = response;
      }
    };
    ServiceCombClient serviceCombClient = new ServiceCombClient(url, httpTransport);
    serviceCombClient.heartbeat(heartbeatRequest);
  }

  @Test
  public void updateInstanceStatus(@Injectable
      HttpTransport httpTransport) throws ServiceCombException, IOException {
    final int expectedCode = 200;
    Response response = new Response();
    response.setStatusCode(expectedCode);
    String responseString = "{}";

    response.setContent(responseString);

    new Expectations() {
      {
        Deencapsulation.newUninitializedInstance(DefaultHttpTransport.class);
        result = httpTransport;
        httpTransport.sendPutRequest(anyString, (HttpEntity) any);
        result = response;
      }
    };
    ServiceCombClient serviceCombClient = new ServiceCombClient(url, httpTransport);
    serviceCombClient.updateInstanceStatus("1", "2", "UP");
  }

  @Test
  public void getInstance(@Injectable
      Boolean autoDiscovery, @Injectable
      HttpTransport httpTransport) throws ServiceCombException, IOException {
    final int expectedCode = 200;
    Response response = new Response();
    response.setStatusCode(expectedCode);
    String responseString = "{\n"
        + "  \"instance\": {\n"
        + "    \"instanceId\": \"2\",\n"
        + "    \"serviceId\": \"1\",\n"
        + "    \"version\": \"string\",\n"
        + "    \"hostName\": \"string\",\n"
        + "    \"endpoints\": [\n"
        + "      \"string\"\n"
        + "    ],\n"
        + "    \"status\": \"UP\",\n"
        + "    \"timestamp\": \"string\",\n"
        + "    \"modTimestamp\": \"string\"\n"
        + "  }\n"
        + "}";

    response.setContent(responseString);

    new Expectations() {
      {
        Deencapsulation.newUninitializedInstance(DefaultHttpTransport.class);
        result = httpTransport;
        httpTransport.sendGetRequest(anyString);
        result = response;
      }
    };
    ServiceCombClient serviceCombClient = new ServiceCombClient(url, httpTransport);
    MicroserviceInstanceSingleResponse actual = serviceCombClient.getInstance("1", "2");
    Assert.assertEquals("1", actual.getInstance().getServiceId());
  }

  @Test
  public void getServices(@Injectable
      HttpTransport httpTransport) throws ServiceCombException, IOException {
    final int expectedCode = 200;
    Response response = new Response();
    response.setStatusCode(expectedCode);
    String responseString = "{\n"
        + "  \"services\": [\n"
        + "    {\n"
        + "      \"serviceId\": \"string\",\n"
        + "      \"environment\": \"string\",\n"
        + "      \"appId\": \"string\",\n"
        + "      \"serviceName\": \"string\",\n"
        + "      \"version\": \"string\",\n"
        + "      \"description\": \"string\",\n"
        + "      \"level\": \"string\",\n"
        + "      \"registerBy\": \"string\",\n"
        + "      \"schemas\": [\n"
        + "        \"string\"\n"
        + "      ],\n"
        + "      \"status\": \"UP\",\n"
        + "      \"timestamp\": \"string\",\n"
        + "      \"modTimestamp\": \"string\",\n"
        + "      \"framework\": {\n"
        + "        \"name\": \"string\",\n"
        + "        \"version\": \"string\"\n"
        + "      },\n"
        + "      \"paths\": [\n"
        + "        {\n"
        + "          \"Path\": \"string\",\n"
        + "          \"Property\": {\n"
        + "            \"additionalProp1\": \"string\",\n"
        + "            \"additionalProp2\": \"string\",\n"
        + "            \"additionalProp3\": \"string\"\n"
        + "          }\n"
        + "        }\n"
        + "      ],\n"
        + "      \"properties\": {\n"
        + "        \"additionalProp1\": \"string\",\n"
        + "        \"additionalProp2\": \"string\",\n"
        + "        \"additionalProp3\": \"string\"\n"
        + "      }\n"
        + "    }\n"
        + "  ]\n"
        + "}";

    response.setContent(responseString);

    new Expectations() {
      {
        Deencapsulation.newUninitializedInstance(DefaultHttpTransport.class);
        result = httpTransport;
        httpTransport.sendGetRequest(anyString);
        result = response;
      }
    };
    ServiceCombClient serviceCombClient = new ServiceCombClient(url, httpTransport);
    serviceCombClient.autoDiscovery(false);
    MicroserviceResponse actual = serviceCombClient.getServices();
    Assert.assertEquals(1, actual.getServices().size());
  }
}