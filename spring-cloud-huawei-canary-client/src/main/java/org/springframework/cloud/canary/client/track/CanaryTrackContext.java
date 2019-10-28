package org.springframework.cloud.canary.client.track;

import java.util.Map;

/**
 * @Author GuoYl123
 * @Date 2019/10/12
 **/
public class CanaryTrackContext {
    private static ThreadLocal<String> serviceNameThreadLocal = new ThreadLocal<>();

    private static ThreadLocal<Map<String, String>> requestHeaderThreadLocal = new ThreadLocal<>();

    public static void remove() {
        serviceNameThreadLocal.remove();
        requestHeaderThreadLocal.remove();
    }

    public static String getServiceName() {
        return serviceNameThreadLocal.get();
    }

    public static void setServiceName(String serviceName) {
        serviceNameThreadLocal.set(serviceName);
    }

    public static Map<String, String> getRequestHeader() {
        return requestHeaderThreadLocal.get();
    }

    public static void setRequestHeader(Map<String, String> requestHeader) {
        requestHeaderThreadLocal.set(requestHeader);
    }
}
