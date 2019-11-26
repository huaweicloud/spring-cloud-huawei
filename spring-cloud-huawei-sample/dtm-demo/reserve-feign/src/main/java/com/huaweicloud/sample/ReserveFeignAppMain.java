package com.huaweicloud.sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @Author wangqijun
 * @Date 17:01 2019-09-10
 **/

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class ReserveFeignAppMain {
  public static void main(String[] args) {
    SpringApplication.run(ReserveFeignAppMain.class, args);
  }
}