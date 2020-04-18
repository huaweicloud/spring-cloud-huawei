package com.huaweicloud.servicecomb.discovery.ribbon;

import com.netflix.loadbalancer.IPing;
import com.netflix.loadbalancer.Server;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @Author GuoYl123
 * @Date 2020/4/17
 **/
public class ServiceCombIPing implements IPing {

  @Override
  public boolean isAlive(Server server) {
    try (Socket s = new Socket()) {
      s.connect(new InetSocketAddress(server.getHost(), server.getPort()), 3000);
    } catch (IOException e) {
      return false;
    }
    return true;
  }
}
