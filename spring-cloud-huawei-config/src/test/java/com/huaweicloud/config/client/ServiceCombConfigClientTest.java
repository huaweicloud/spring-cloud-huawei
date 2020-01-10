package com.huaweicloud.config.client;

import com.huaweicloud.config.ServiceCombConfigProperties;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import com.huaweicloud.common.exception.RemoteOperationException;
import com.huaweicloud.common.exception.RemoteServerUnavailableException;
import com.huaweicloud.common.transport.DefaultHttpTransport;
import com.huaweicloud.common.transport.HttpTransport;
import com.huaweicloud.common.transport.Response;

import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Injectable;

/**
 * @Author wangqijun
 * @Date 20:58 2019-10-27
 **/
public class ServiceCombConfigClientTest {

  @Test
  public void loadAll(@Injectable
      HttpTransport httpTransport)
      throws RemoteServerUnavailableException, RemoteOperationException {
    final int expectedCode = 200;
    Response response = new Response();
    response.setStatusCode(expectedCode);
    String url = "http://127.0.0.1:30113";
    String responseString = "{\n"
        + "    \"application\": {\n"
        + "        \"ffd\": \"dd\"\n"
        + "    },\n"
        + "    \"price@default\": {\n"
        + "        \"dd\": \"ss\"\n"
        + "    }\n"
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
    ServiceCombConfigClient serviceCombClient = new ServiceCombConfigClient(url, httpTransport);
    ServiceCombConfigProperties serviceCombConfigProperties = new ServiceCombConfigProperties();
    serviceCombConfigProperties.setServiceName("price");
    serviceCombConfigProperties.setAppName("default");
    serviceCombConfigProperties.setVersion("0.0.1");
    Map<String, String> actual = serviceCombClient.loadAll(serviceCombConfigProperties, "default");
    Assert.assertEquals(2, actual.size());
    Assert.assertEquals("dd", actual.get("ffd"));
    Assert.assertEquals("ss", actual.get("dd"));
  }
}