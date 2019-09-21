package org.springframework.cloud.dtm.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

/**
 * @Author wangqijun
 * @Date 12:45 2019-09-18
 **/
public class RestTemplateForDtmAutoConfiguration {
  @Autowired(required = false)
  private Collection<RestTemplate> restTemplates;

  @Autowired
  private RestTemplateForDtmInterceptor restTemplateForDtmInterceptor;

  @Bean
  public RestTemplateForDtmInterceptor restTemplateForDtmInterceptor() {
    return new RestTemplateForDtmInterceptor();
  }

  @PostConstruct
  public void init() {
    if (this.restTemplates != null) {
      for (RestTemplate restTemplate : restTemplates) {
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<ClientHttpRequestInterceptor>(
            restTemplate.getInterceptors());
        interceptors.add(this.restTemplateForDtmInterceptor);
        restTemplate.setInterceptors(interceptors);
      }
    }
  }
}
