package org.springframework.cloud.canary.client.track;

/**
 * @Author GuoYl123
 * @Date 2019/10/12
 **/
public class CanaryTrackThreadLocalInfo implements CanaryTrackInfo {
    ThreadLocal<CanaryTrackRequest> canaryTrackRequestThreadLocal = new ThreadLocal<>();

    public CanaryTrackRequest getCanaryTrackRequest() {
        return canaryTrackRequestThreadLocal.get();
    }

    public void setCanaryTrackRequest(CanaryTrackRequest canaryTrackRequest) {
        this.canaryTrackRequestThreadLocal.set(canaryTrackRequest);
    }

    public void removeCanaryTrackRequest() {
        this.canaryTrackRequestThreadLocal.remove();
    }
}
