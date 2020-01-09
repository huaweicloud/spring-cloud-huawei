package com.huaweicloud.config.kie;

import com.huaweicloud.common.exception.ServiceCombRuntimeException;
import com.huaweicloud.config.ServiceCombConfigProperties;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.StringUtils;

/**
 * @Author GuoYl123
 * @Date 2020/1/7
 **/
public class KieUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(KieUtil.class);

  public static Map<String, String> getConfigByLabel(
      ServiceCombConfigProperties serviceCombConfigProperties, KVResponse resp) {
    Map<String, String> resultMap = new HashMap<>();
    List<KVDoc> appList = new ArrayList<>();
    List<KVDoc> serviceList = new ArrayList<>();
    List<KVDoc> versionList = new ArrayList<>();
    for (KVDoc kvDoc : resp.getData()) {
      Map<String, String> labelsMap = kvDoc.getLabels();
      //todo:how to deal env
      if (labelsMap.containsKey("app") && labelsMap.get("app")
          .equals(serviceCombConfigProperties.getAppName())
          && labelsMap.containsKey("env") && labelsMap.get("env")
          .equals(serviceCombConfigProperties.getEnv())) {
        if (!labelsMap.containsKey("service")) {
          appList.add(kvDoc);
        }
        if (labelsMap.containsKey("service") && labelsMap.get("service")
            .equals(serviceCombConfigProperties.getServiceName())) {
          if (!kvDoc.getLabels().containsKey("version")) {
            serviceList.add(kvDoc);
          }
          if (labelsMap.containsKey("version") && labelsMap.get("version")
              .equals(serviceCombConfigProperties.getServiceName())) {
            versionList.add(kvDoc);
          }
        }
      }
    }
    //kv is priority
    for (KVDoc kvDoc : appList) {
      resultMap.putAll(processValueType(kvDoc));
    }
    for (KVDoc kvDoc : serviceList) {
      resultMap.putAll(processValueType(kvDoc));
    }
    for (KVDoc kvDoc : versionList) {
      resultMap.putAll(processValueType(kvDoc));
    }
    return resultMap;
  }


  public static Map<String, String> processValueType(KVDoc kvDoc) {
    ValueType vtype;
    try {
      vtype = ValueType.valueOf(kvDoc.getValueType());
    } catch (IllegalArgumentException e) {
      throw new ServiceCombRuntimeException("value type not support");
    }
    Properties properties = new Properties();
    Map<String, String> kvMap = new HashMap<>();
    try {
      if (vtype == (ValueType.yaml) || vtype == (ValueType.yml)) {
        YamlPropertiesFactoryBean yamlFactory = new YamlPropertiesFactoryBean();
        yamlFactory.setResources(new ByteArrayResource(kvDoc.getValue().getBytes()));
        properties = yamlFactory.getObject();
      } else if (vtype == (ValueType.properties)) {
        properties.load(new StringReader(kvDoc.getValue()));
      } else if (vtype == (ValueType.text) || vtype == (ValueType.string)) {
        kvMap.put(kvDoc.getKey(), kvDoc.getValue());
        return kvMap;
      } else {
        kvMap.put(kvDoc.getKey(), kvDoc.getValue());
        return kvMap;
      }
      kvMap = toMap(kvDoc.getKey(), properties);
      return kvMap;
    } catch (Exception e) {
      LOGGER.error("read config failed");
    }
    return Collections.emptyMap();
  }


  private static Map<String, String> toMap(String prefix, Properties properties) {
    Map<String, String> result = new HashMap<>();
    Enumeration<String> keys = (Enumeration<String>) properties.propertyNames();
    while (keys.hasMoreElements()) {
      String key = keys.nextElement();
      if (!StringUtils.isEmpty(prefix)) {
        key = prefix + "." + key;
      }
      Object value = properties.getProperty(key);
      if (value != null) {
        result.put(key, ((String) value).trim());
      } else {
        result.put(key, null);
      }
    }
    return result;
  }
}
