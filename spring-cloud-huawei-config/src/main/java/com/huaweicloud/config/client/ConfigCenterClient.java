package com.huaweicloud.config.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.huaweicloud.common.exception.RemoteOperationException;
import com.huaweicloud.common.exception.RemoteServerUnavailableException;
import com.huaweicloud.common.transport.HttpTransport;
import com.huaweicloud.common.transport.Response;
import com.huaweicloud.config.ServiceCombConfigProperties;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.HttpStatus;
import org.apache.servicecomb.foundation.common.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author GuoYl123
 * @Date 2020/7/14
 **/
public class ConfigCenterClient extends ServiceCombConfigClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigCenterClient.class);

  public ConfigCenterClient(String urls,
      HttpTransport httpTransport) {
    super(urls, httpTransport);
  }

  public Map<String, String> loadAll(ServiceCombConfigProperties serviceCombConfigProperties,
      String project) throws RemoteOperationException {
    project = project != null && !project.isEmpty() ? project : ConfigConstants.DEFAULT_PROJECT;
    String dimensionsInfo = spliceDimensionsInfo(serviceCombConfigProperties);
    Response response = null;
    Map<String, String> result = new HashMap<>();
    try {
      response = httpTransport.sendGetRequest(
          configCenterConfig.getUrl() + "/" + ConfigConstants.DEFAULT_API_VERSION
              + "/" + project + "/configuration/items?dimensionsInfo="
              + URLEncoder.encode(dimensionsInfo, "UTF-8") + "&revision=" + revision);
      if (response == null) {
        return result;
      }
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        LOGGER.debug(response.getContent());
        Map<String, Map<String, String>> allConfigMap = JsonUtils.OBJ_MAPPER
            .readValue(response.getContent(),
                new TypeReference<Map<String, Map<String, String>>>() {
                });
        if (allConfigMap != null) {
          if (allConfigMap.get(ConfigConstants.REVISION) != null) {
            revision = allConfigMap.get(ConfigConstants.REVISION).get("version");
          }
          if (allConfigMap.get(ConfigConstants.APPLICATION_CONFIG) != null) {
            result.putAll(allConfigMap.get(ConfigConstants.APPLICATION_CONFIG));
          }
          if (dimensionsInfo.contains(ConfigConstants.DEFAULT_SERVICE_SEPARATOR)
              && allConfigMap.get(dimensionsInfo
              .substring(0, dimensionsInfo.indexOf(ConfigConstants.DEFAULT_SERVICE_SEPARATOR)))
              != null) {
            result.putAll(allConfigMap.get(dimensionsInfo
                .substring(0, dimensionsInfo.indexOf(ConfigConstants.DEFAULT_SERVICE_SEPARATOR))));
          }
          if (allConfigMap.get(dimensionsInfo) != null) {
            result.putAll(allConfigMap.get(dimensionsInfo));
          }
        }
        return result;
      } else if (response.getStatusCode() == HttpStatus.SC_NOT_MODIFIED) {
        return null;
      } else if (response.getStatusCode() == HttpStatus.SC_BAD_REQUEST) {
        LOGGER.info(response.getStatusMessage());
        return null;
      } else {
        throw new RemoteOperationException(
            "read response failed. status:"
                + response.getStatusCode()
                + "; message:"
                + response.getStatusMessage()
                + "; content:"
                + response.getContent());
      }
    } catch (RemoteServerUnavailableException e) {
      configCenterConfig.toggle();
      throw new RemoteOperationException(
          "config center address is not available , will retry.", e);
    } catch (IOException e) {
      configCenterConfig.toggle();
      throw new RemoteOperationException("read response failed. " + response, e);
    }
  }


  private String spliceDimensionsInfo(ServiceCombConfigProperties serviceCombConfigProperties) {
    String result =
        serviceCombConfigProperties.getServiceName() + ConfigConstants.DEFAULT_APP_SEPARATOR
            + serviceCombConfigProperties.getAppName();
    if (serviceCombConfigProperties.getVersion() != null && !serviceCombConfigProperties
        .getVersion().isEmpty()) {
      result = result + ConfigConstants.DEFAULT_SERVICE_SEPARATOR + serviceCombConfigProperties
          .getVersion();
    }
    return result;
  }
}
