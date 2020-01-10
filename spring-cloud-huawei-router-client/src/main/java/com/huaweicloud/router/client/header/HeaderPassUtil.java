package com.huaweicloud.router.client.header;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.huaweicloud.common.util.JsonUtils;
import com.huaweicloud.router.client.track.RouterTrackContext;
import com.huaweicloud.router.core.cache.RouterRuleCache;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;

/**
 * @Author GuoYl123
 * @Date 2019/12/12
 **/
public class HeaderPassUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(HeaderPassUtil.class);

  private static final String SERVICECOMB_ROUTER_HEADER = "servicecomb.router.header";

  //cache from config
  private static List<String> passHeader = new ArrayList<>();

  public static String getPassHeaderString(Map<String, String> headers) {
    if (!isHaveHeadersRule()) {
      return null;
    }
    if (!RouterRuleCache.isServerContainRule(RouterTrackContext.getServiceName())) {
      return null;
    }
    if (loadHeaders()) {
      try {
        return JsonUtils.OBJ_MAPPER.writeValueAsString(getHeaderMap(headers));
      } catch (JsonProcessingException e) {
        LOGGER.error("canary context serialization failed : {}", e.getMessage());
      }
    }
    return null;
  }

  private static boolean isHaveHeadersRule() {
    DynamicStringProperty headerStr = DynamicPropertyFactory.getInstance()
        .getStringProperty(SERVICECOMB_ROUTER_HEADER, null);
    if (StringUtils.isEmpty(headerStr)) {
      return false;
    }
    return true;
  }

  private static boolean loadHeaders() {
    if (!CollectionUtils.isEmpty(passHeader)) {
      return true;
    }
    DynamicStringProperty headerStr = DynamicPropertyFactory.getInstance()
        .getStringProperty(SERVICECOMB_ROUTER_HEADER, null, () -> {
          DynamicStringProperty temHeader = DynamicPropertyFactory.getInstance()
              .getStringProperty(SERVICECOMB_ROUTER_HEADER, null);
          if (!addAllHeaders(temHeader.get())) {
            passHeader = new ArrayList<>();
          }
        });
    return addAllHeaders(headerStr.get());
  }

  private static Map<String, String> getHeaderMap(Map<String, String> headers) {
    Map<String, String> headerMap = new HashMap<>();
    passHeader.forEach(headerKey -> {
      String val = headers.get(headerKey);
      if (!StringUtils.isEmpty(val)) {
        headerMap.put(headerKey, headers.get(headerKey));
      }
    });
    return headerMap;
  }

  private static boolean addAllHeaders(String str) {
    if (StringUtils.isEmpty(str)) {
      return false;
    }
    try {
      if (CollectionUtils.isEmpty(passHeader)) {
        Yaml yaml = new Yaml();
        passHeader = yaml.load(str);
      }
    } catch (Exception e) {
      LOGGER.error("route management Serialization failed: {}", e.getMessage());
      return false;
    }
    return true;
  }
}