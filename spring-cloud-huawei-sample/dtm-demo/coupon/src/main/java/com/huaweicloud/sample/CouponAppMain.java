package com.huaweicloud.sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @Author wangqijun
 * @Date 11:42 2019-09-17
 **/
@SpringBootApplication
@EnableDiscoveryClient
public class CouponAppMain {
  public static void main(String[] args) {
    SpringApplication.run(CouponAppMain.class, args);
  }
}
