package com.huaweicloud.common.transport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.junit.Test;
import com.huaweicloud.common.exception.RemoteServerUnavailableException;

import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;

/**
 * @Author wangqijun
 * @Date 14:19 2019-10-17
 **/
public class DefaultHttpTransportTest {

  @Injectable
  HttpResponse httpResponse;

  @Injectable
  HttpClient httpClient;

  @Injectable
  SSLConfig sslConfig;

  @Tested
  private DefaultHttpTransport httpHttpTransport;

  @Before
  public void setUp() throws Exception {
    httpHttpTransport = Deencapsulation.newUninitializedInstance(DefaultHttpTransport.class);
    //httpHttpTransport=DefaultHttpTransport.getInstance();
  }

  @Test
  public void getInstance() {
    DefaultHttpTransport instance = DefaultHttpTransport.getInstance();
    assertNotNull(instance);
    DefaultHttpTransport instance2 = DefaultHttpTransport.getInstance();
    assertEquals(instance, instance2);
  }

  @Test
  public void execute() throws IOException,
      RemoteServerUnavailableException {
    final int expectedCode = 200;
    final String expectedMessage = "this is message";
    final String expectedEntity = "content";
    httpResponse.setStatusCode(expectedCode);
    HttpGet httpGet = new HttpGet("http://dd.cc");
    new Expectations(EntityUtils.class) {
      {
        EntityUtils.toString(httpResponse.getEntity());
        result = expectedEntity;
        httpResponse.getStatusLine().getStatusCode();
        result = expectedCode;
        httpResponse.getStatusLine().getReasonPhrase();
        result = expectedMessage;
      }
    };
    Response resp = httpHttpTransport.execute(httpGet);
    assertEquals(resp.getStatusCode(), expectedCode);
    assertEquals(expectedMessage, resp.getStatusMessage());
    assertEquals(expectedEntity, resp.getContent());
  }
}