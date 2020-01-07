package com.huaweicloud.config.kie;

import com.huaweicloud.config.ServiceCombConfigProperties;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author GuoYl123
 * @Date 2020/1/7
 **/
public class KieUtil {

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
    //todo :priority
    for (KVDoc kvDoc : appList) {
      resultMap.put(kvDoc.getKey(), kvDoc.getValue());
    }
    for (KVDoc kvDoc : serviceList) {
      resultMap.put(kvDoc.getKey(), kvDoc.getValue());
    }
    for (KVDoc kvDoc : versionList) {
      resultMap.put(kvDoc.getKey(), kvDoc.getValue());
    }
    return resultMap;
  }
}
