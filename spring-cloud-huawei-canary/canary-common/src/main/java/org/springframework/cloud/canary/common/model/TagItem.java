package org.springframework.cloud.canary.common.model;

import java.util.Map;
import java.util.Objects;

/**
 * @Author GuoYl123
 * @Date 2019/10/17
 **/
public class TagItem {
    private String version;
    private Map<String, String> param;

    public TagItem() {
    }

    public TagItem(String version, Map<String, String> param) {
        this.version = version;
        this.param = param;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Map<String, String> getParam() {
        return param;
    }

    public void setParam(Map<String, String> param) {
        this.param = param;
    }

    /**
     * map在匹配key调用
     *
     * @return
     */
    @Override
    public int hashCode() {
        int result = Objects.hash(version);
        if (param != null) {
            result = 31 * result + param.hashCode();
        }
        return result;
    }

    /**
     * all match
     * map在匹配key调用
     *
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj != null || !(obj instanceof TagItem)) {
            return false;
        }
        TagItem item = (TagItem) obj;
        if (this.param == null && item.getParam() == null) {
            return this.version.equals(item.getVersion());
        }
        if (this.param == null || item.getParam() == null) {
            return false;
        }
        return this.version.equals(item.getVersion()) && this.param.equals(item.getParam());
    }


    /**
     * 返回匹配的个数
     *
     * @param item
     * @return
     */
    public int matchNum(TagItem item) {
        int cnt = 0;
        if (!version.equals(item.getVersion())) {
            return 0;
        }
        for (Map.Entry<String, String> entry : param.entrySet()) {
            if (item.getParam().containsKey(entry.getKey()) &&
                    !item.getParam().get(entry.getKey()).equals(entry.getValue())) {
                return 0;
            }
            cnt++;
        }
        return cnt;
    }
}
