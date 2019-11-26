package com.huaweicloud.config.client;

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
      String url, @Injectable
      HttpTransport httpTransport) throws RemoteServerUnavailableException, RemoteOperationException {
    final int expectedCode = 200;
    Response response = new Response();
    response.setStatusCode(expectedCode);
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
    Map<String, String> actual = serviceCombClient.loadAll("price@default#0.0.1", "default");
    Assert.assertEquals(2, actual.size());
    Assert.assertEquals("dd", actual.get("ffd"));
    Assert.assertEquals("ss", actual.get("dd"));
  }
}