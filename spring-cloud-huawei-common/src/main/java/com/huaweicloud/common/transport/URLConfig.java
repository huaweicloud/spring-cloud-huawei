package com.huaweicloud.common.transport;

import com.huaweicloud.common.util.URLUtil;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * @Author GuoYl123
 * @Date 2019/12/19
 **/
public class URLConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(URLConfig.class);

  private List<String> urlList = new ArrayList<>();

  private int index = 0;

  private int resolveUrlSize = 0;

  private int afterDnsResolveIndex = 0;

  public String getUrl() {
    if (resolveUrlSize > 0) {
      String url = urlList.get(afterDnsResolveIndex);
      return url;
    }
    return urlList.get(index);
  }

  public void addUrl(List<String> urls) {
    if (CollectionUtils.isEmpty(urls)) {
      return;
    }
    urlList.addAll(urls);
  }

  public void addUrlAfterDnsResolve(String url) {
    if (StringUtils.isEmpty(url)) {
      return;
    }
    try (Socket s = new Socket()) {
      String[] ipPort = URLUtil.splitIpPort(url);
      s.connect(new InetSocketAddress(ipPort[0], Integer.parseInt(ipPort[1])), 3000);
    } catch (IOException e) {
      return;
    }
    LOGGER.info("choose auto discovery endpoint: {}", url);
    if (resolveUrlSize == 0) {
      afterDnsResolveIndex = urlList.size();
    }
    urlList.add(url);
    resolveUrlSize++;
  }

  public boolean isEmpty() {
    return urlList.isEmpty();
  }

  public synchronized void toggle() {
    if (resolveUrlSize > 0) {
      afterDnsResolveIndex = afterDnsResolveIndex + 1 < urlList.size() ? afterDnsResolveIndex + 1
          : urlList.size() - resolveUrlSize;
    } else {
      index = (index + 1) % urlList.size();
    }
  }
}
