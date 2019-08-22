package org.springframework.cloud.servicecomb.discovery.client;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.servicecomb.discovery.client.exception.RemoteOperationException;
import org.springframework.cloud.servicecomb.discovery.client.exception.RemoteServerUnavailableException;
import org.springframework.cloud.servicecomb.discovery.client.exception.ServiceCombException;
import org.springframework.cloud.servicecomb.discovery.client.model.HeartbeatRequest;
import org.springframework.cloud.servicecomb.discovery.client.model.Microservice;
import org.springframework.cloud.servicecomb.discovery.client.model.MicroserviceInstance;
import org.springframework.cloud.servicecomb.discovery.client.model.MicroserviceInstanceSingleResponse;
import org.springframework.cloud.servicecomb.discovery.client.model.MicroserviceInstancesResponse;
import org.springframework.cloud.servicecomb.discovery.client.model.Response;

import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Injectable;

/**
 * @Author wangqijun
 * @Date 15:18 2019-08-19
 **/
public class ServiceCombClientTest {

  @Test
  public void getServiceCenterInstances(@Injectable
      Boolean autoDiscovery, @Injectable
      String url, @Injectable
      HttpTransport httpTransport) throws RemoteOperationException, RemoteServerUnavailableException, IOException {
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

    autoDiscovery = false;
    new Expectations() {
      {
        Deencapsulation.newUninitializedInstance(DefaultHttpTransport.class);
        result = httpTransport;
        httpTransport.sendGetRequest(anyString);
        result = response;
      }
    };
    ServiceCombClient serviceCombClient = new ServiceCombClient(url, httpTransport, autoDiscovery);
    MicroserviceInstancesResponse actual = serviceCombClient
        .getServiceCenterInstances();
    Assert.assertNotNull(actual);
    Assert.assertEquals(actual.getInstances().size(), 1);
    Assert.assertEquals(actual.getInstances().get(0).getServiceId(), "222");
    Assert.assertEquals(actual.getInstances().get(0).getInstanceId(), "111");
  }


  @Test
  public void registerMicroservice(@Injectable
      Boolean autoDiscovery, @Injectable
      String url, @Injectable
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
        httpTransport.sendPostRequest(anyString, (HttpEntity) any);
        result = response;
      }
    };
    ServiceCombClient serviceCombClient = new ServiceCombClient(url, httpTransport, autoDiscovery);
    String actual = serviceCombClient.registerMicroservice(microservice);
    Assert.assertEquals("22222", actual);
  }

  @Test
  public void getServiceId(@Injectable
      Boolean autoDiscovery, @Injectable
      String url, @Injectable
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
    ServiceCombClient serviceCombClient = new ServiceCombClient(url, httpTransport, autoDiscovery);
    String actual = serviceCombClient.getServiceId(microservice);
    Assert.assertEquals("22222", actual);
  }

  @Test
  public void registerInstance(@Injectable
      Boolean autoDiscovery, @Injectable
      String url, @Injectable
      HttpTransport httpTransport) throws ServiceCombException, IOException {
    final int expectedCode = 200;
    Response response = new Response();
    response.setStatusCode(expectedCode);
    String responseString = "{\"instanceId\": \"22222\"}";

    response.setContent(responseString);

    autoDiscovery = false;

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
    ServiceCombClient serviceCombClient = new ServiceCombClient(url, httpTransport, autoDiscovery);
    String actual = serviceCombClient.registerInstance(microserviceInstance);
    Assert.assertEquals("22222", actual);
  }

  @Test
  public void getInstances(@Injectable
      Boolean autoDiscovery, @Injectable
      String url, @Injectable
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
    ServiceCombClient serviceCombClient = new ServiceCombClient(url, httpTransport, autoDiscovery);
    List<ServiceInstance> actual = serviceCombClient.getInstances(microservice);
    Assert.assertEquals(1, actual.size());
  }

  @Test
  public void heartbeat(@Injectable
      Boolean autoDiscovery, @Injectable
      String url, @Injectable
      HttpTransport httpTransport) throws ServiceCombException, IOException {
    final int expectedCode = 200;
    Response response = new Response();
    response.setStatusCode(expectedCode);
    String responseString = "{\"serviceId\": \"22222\"}";

    response.setContent(responseString);

    autoDiscovery = false;

    HeartbeatRequest heartbeatRequest = new HeartbeatRequest("1", "2");
    new Expectations() {
      {
        Deencapsulation.newUninitializedInstance(DefaultHttpTransport.class);
        result = httpTransport;
        httpTransport.sendPutRequest(anyString, (HttpEntity) any);
        result = response;
      }
    };
    ServiceCombClient serviceCombClient = new ServiceCombClient(url, httpTransport, autoDiscovery);
    serviceCombClient.heartbeat(heartbeatRequest);
  }

  @Test
  public void updateInstanceStatus(@Injectable
      Boolean autoDiscovery, @Injectable
      String url, @Injectable
      HttpTransport httpTransport) throws ServiceCombException, IOException {
    final int expectedCode = 200;
    Response response = new Response();
    response.setStatusCode(expectedCode);
    String responseString = "{}";

    response.setContent(responseString);

    autoDiscovery = false;

    new Expectations() {
      {
        Deencapsulation.newUninitializedInstance(DefaultHttpTransport.class);
        result = httpTransport;
        httpTransport.sendPutRequest(anyString, (HttpEntity) any);
        result = response;
      }
    };
    ServiceCombClient serviceCombClient = new ServiceCombClient(url, httpTransport, autoDiscovery);
    serviceCombClient.updateInstanceStatus("1", "2", "UP");
  }

  @Test
  public void getInstance(@Injectable
      Boolean autoDiscovery, @Injectable
      String url, @Injectable
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

    autoDiscovery = false;

    new Expectations() {
      {
        Deencapsulation.newUninitializedInstance(DefaultHttpTransport.class);
        result = httpTransport;
        httpTransport.sendGetRequest(anyString);
        result = response;
      }
    };
    ServiceCombClient serviceCombClient = new ServiceCombClient(url, httpTransport, autoDiscovery);
    MicroserviceInstanceSingleResponse actual = serviceCombClient.getInstance("1", "2");
    Assert.assertEquals("1", actual.getInstance().getServiceId());
  }
}