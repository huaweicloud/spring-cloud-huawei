package org.springframework.cloud.canary.core.model;

import java.util.Map;

/**
 * @Author GuoYl123
 * @Date 2019/10/17
 **/
public class RouteItem implements Comparable<RouteItem> {
    private Integer weight;
    /**
     * 负载均衡参数
     */
    private Integer currentWeight = 0;
    /**
     * 为了提高序列化速度设置为Map
     * 固定字段 version
     */
    private Map<String, String> tags;

    private TagItem tagitem;


    public void initTagItem() {
        if (tags != null && tags.containsKey("version")) {
            tagitem = new TagItem(tags);
        } else {
            throw new RuntimeException("version must not be null");
        }
    }

    public void addCurrentWeight() {
        currentWeight += weight;
    }

    public void reduceCurrentWeight(int total) {
        currentWeight -= total;
    }

    public RouteItem() {
    }

    public RouteItem(Integer weight, TagItem tags) {
        this.weight = weight;
        this.tagitem = tags;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public Integer getCurrentWeight() {
        return currentWeight;
    }

    public void setCurrentWeight(Integer currentWeight) {
        this.currentWeight = currentWeight;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public TagItem getTagitem() {
        return tagitem;
    }

    public void setTagitem(TagItem tagitem) {
        this.tagitem = tagitem;
    }

    @Override
    public int compareTo(RouteItem param) {
        if (param.weight == this.weight) {
            return 0;
        }
        return param.weight > this.weight ? 1 : -1;
    }

    @Override
    public String toString() {
        return "RouteItem{" +
                "weight=" + weight +
                ", currentWeight=" + currentWeight +
                ", tags=" + tags +
                ", tagitem=" + tagitem +
                '}';
    }
}
