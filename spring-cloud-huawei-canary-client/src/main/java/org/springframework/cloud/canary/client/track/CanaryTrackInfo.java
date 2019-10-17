package org.springframework.cloud.canary.client.track;

/**
 * 后期还要兼容hytrix,这里设计为接口
 * @Author GuoYl123
 * @Date 2019/10/12
 **/
public interface CanaryTrackInfo {
    public CanaryTrackRequest getCanaryTrackRequest() ;

    public void setCanaryTrackRequest(CanaryTrackRequest canaryTrackRequest) ;

    public void removeCanaryTrackRequest();
}
