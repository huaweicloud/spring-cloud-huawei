package com.huaweicloud.sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @Author wangqijun
 * @Date 09:53 2019-09-23
 **/
@SpringBootApplication
@EnableDiscoveryClient
public class TicketAppMain {
  public static void main(String[] args) {
    SpringApplication.run(TicketAppMain.class, args);
  }
}
