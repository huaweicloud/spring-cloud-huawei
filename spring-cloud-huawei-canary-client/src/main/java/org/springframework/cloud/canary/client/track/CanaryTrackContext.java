package org.springframework.cloud.canary.client.track;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author GuoYl123
 * @Date 2019/10/12
 **/
public class CanaryTrackContext {
    private static CanaryTrackInfo canaryTrackInfo;
    private static Map<CanaryTrackRequest, String> serviceNameMap = new ConcurrentHashMap<>();

    private static Map<CanaryTrackRequest, Map<String, String>> requestHeaderMap = new ConcurrentHashMap<>();

    public static void removeRequestInfo() {
        preCheck();
        serviceNameMap.remove(canaryTrackInfo.getCanaryTrackRequest());
        requestHeaderMap.remove(canaryTrackInfo.getCanaryTrackRequest());
        canaryTrackInfo.removeCanaryTrackRequest();
    }

    public static void setRequestInfo(CanaryTrackRequest canaryTrackRequest){
        canaryTrackInfo.setCanaryTrackRequest(canaryTrackRequest);
    }
    public static String getServiceName() {
        preCheck(serviceNameMap);
        return serviceNameMap.get(canaryTrackInfo.getCanaryTrackRequest());
    }

    public static void setServiceName(String serviceName) {
        preCheck();
        serviceNameMap.put(canaryTrackInfo.getCanaryTrackRequest(), serviceName);
    }

    public static Map<String, String> getRequestHeader() {
        preCheck(requestHeaderMap);
        return requestHeaderMap.get(canaryTrackInfo.getCanaryTrackRequest());
    }

    public static void setRequestHeader(Map<String, String> requestHeader) {
        preCheck();
        requestHeaderMap.put(canaryTrackInfo.getCanaryTrackRequest(), requestHeader);
    }

    private static void preCheck() {
        if (canaryTrackInfo.getCanaryTrackRequest() == null) {
            throw new RuntimeException("is null");
        }
    }

    private static void preCheck(Map map) {
        preCheck();
        if (!map.containsKey(canaryTrackInfo.getCanaryTrackRequest())) {
            throw new RuntimeException("map key is null");
        }
    }

    public static void setCanaryTrackInfo(CanaryTrackInfo canaryTrackInfo) {
        CanaryTrackContext.canaryTrackInfo = canaryTrackInfo;
    }
}
