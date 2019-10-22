package org.springframework.cloud.servicecomb.discovery.discovery;

import org.springframework.cloud.servicecomb.discovery.client.model.Microservice;
import org.springframework.cloud.servicecomb.discovery.client.model.MicroserviceInstance;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author GuoYl123
 * @Date 2019/10/17
 **/
public class MicroserviceCache {
    private static Map<String, MicroserviceInstance> microserviceList = new ConcurrentHashMap<>();

    public static void initService(List<Microservice> list) {
        list.forEach(item ->
                item.getInstances().forEach(ins -> {
                    ins.setServiceName(item.getServiceName());
                    ins.getEndpoints().forEach(ep -> {
                        microserviceList.put(ep.replaceAll("http(?s)://", ""), ins);
                    });
                })
        );
    }

    public static void initInsList(List<MicroserviceInstance> list, String serviceName) {
        list.forEach(ins -> {
            ins.setServiceName(serviceName);
            ins.getEndpoints().forEach(ep -> {
                microserviceList.put(ep.replaceAll("http(?s)://", ""), ins);
            });
        });
    }

    public static MicroserviceInstance getMicroserviceIns(String insId) {
        return microserviceList.get(insId);
    }
}
