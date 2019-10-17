package org.springframework.cloud.canary.common.model;

import java.util.Map;

/**
 * @Author GuoYl123
 * @Date 2019/10/17
 **/
public class Matcher {
    //服务级别
    private String source;
    //服务级别 -- 这个暂时不考虑
    private Map<String, String> sourceTags;
    //invoke级别
    private Map<String, HeaderRule> headers;
    //这个暂时不考虑
    private String refer;

    public Matcher() {
    }

    public boolean filte(String sourcName, Map<String, String> sourceTags) {
        if (sourcName != null && !sourcName.equals(source)) {
            return false;
        }
        return true;
    }

    public boolean match(Map<String, String> realHeaders) {
        if (headers == null) {
            return true;
        }
        for (Map.Entry<String, HeaderRule> entry : headers.entrySet()) {
            if (realHeaders.containsKey(entry.getKey()) && !entry.getValue().match(realHeaders.get(entry.getKey()))) {
                return false;
            }
        }
        return true;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Map<String, String> getSourceTags() {
        return sourceTags;
    }

    public void setSourceTags(Map<String, String> sourceTags) {
        this.sourceTags = sourceTags;
    }

    public Map<String, HeaderRule> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, HeaderRule> headers) {
        this.headers = headers;
    }

    public String getRefer() {
        return refer;
    }

    public void setRefer(String refer) {
        this.refer = refer;
    }

    @Override
    public String toString() {
        return "Matcher{" +
                "source='" + source + '\'' +
                ", sourceTags=" + sourceTags +
                ", headers=" + headers +
                ", refer='" + refer + '\'' +
                '}';
    }
}
