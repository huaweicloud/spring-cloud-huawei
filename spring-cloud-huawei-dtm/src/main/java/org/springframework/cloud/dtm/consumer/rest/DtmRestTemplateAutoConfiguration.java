package org.springframework.cloud.dtm.consumer.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

/**
 * @Author wangqijun
 * @Date 12:45 2019-09-18
 **/
@Configuration
public class DtmRestTemplateAutoConfiguration {
  private static final Logger LOGGER = LoggerFactory.getLogger(DtmRestTemplateAutoConfiguration.class);

  @Autowired(required = false)
  private Collection<RestTemplate> restTemplates;

  @Autowired
  private DtmRestTemplateInterceptor dtmRestTemplateInterceptor;

  @Bean
  public DtmRestTemplateInterceptor restTemplateForDtmInterceptor() {
    return new DtmRestTemplateInterceptor();
  }

  @PostConstruct
  public void init() {
    LOGGER.debug("init restTemplate for dtm..");
    if (this.restTemplates != null) {
      for (RestTemplate restTemplate : restTemplates) {
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<ClientHttpRequestInterceptor>(
            restTemplate.getInterceptors());
        interceptors.add(dtmRestTemplateInterceptor);
        restTemplate.setInterceptors(interceptors);
      }
    }
  }
}
