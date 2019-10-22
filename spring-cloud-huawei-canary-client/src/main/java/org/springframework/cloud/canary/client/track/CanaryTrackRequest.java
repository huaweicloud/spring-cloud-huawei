package org.springframework.cloud.canary.client.track;

/**
 * @Author GuoYl123
 * @Date 2019/10/12
 **/
public class CanaryTrackRequest {
    private String uniqueId;

    public CanaryTrackRequest(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }
}
