package org.springframework.cloud.dtm;

import java.util.HashMap;
import java.util.Map;

import com.huawei.middleware.dtm.client.context.DTMContext;

/**
 * @Author wangqijun
 * @Date 09:54 2019-09-27
 **/
public class DtmContextDTO {
  private String customizedData = "";

  private long globalTxId = -1L;

  private long branchTxId = -1L;

  private String channelKey = "";

  private Map<String, String> globalOptionalData = new HashMap();

  private Map<String, String> branchOptionalData = new HashMap();

  public DtmContextDTO() {
  }

  public static DtmContextDTO fromDtmContext(DTMContext dtmContext) {
    DtmContextDTO dtmContextDTO = new DtmContextDTO();
    dtmContextDTO.setGlobalTxId(dtmContext.getGlobalTxId());
    dtmContextDTO.setBranchTxId(dtmContext.getBranchTxId());
    dtmContextDTO.setChannelKey(dtmContext.getChannelKey());
    dtmContextDTO.setGlobalOptionalData(dtmContext.getGlobalOptionalData());
    dtmContextDTO.setBranchOptionalData(dtmContext.getBranchOptionalData());
    return dtmContextDTO;
  }

  public String getCustomizedData() {
    return this.customizedData;
  }

  public void setCustomizedData(String customizedData) {
    this.customizedData = customizedData;
  }

  public long getGlobalTxId() {
    return this.globalTxId;
  }

  public void setGlobalTxId(long globalTxId) {
    this.globalTxId = globalTxId;
  }

  public long getBranchTxId() {
    return this.branchTxId;
  }

  public void setBranchTxId(long branchTxId) {
    this.branchTxId = branchTxId;
  }

  public String getChannelKey() {
    return this.channelKey;
  }

  public void setChannelKey(String channelKey) {
    this.channelKey = channelKey;
  }

  public Map<String, String> getGlobalOptionalData() {
    return this.globalOptionalData;
  }

  public void setGlobalOptionalData(Map<String, String> globalOptionalData) {
    this.globalOptionalData = globalOptionalData;
  }

  public Map<String, String> getBranchOptionalData() {
    return this.branchOptionalData;
  }

  public void setBranchOptionalData(Map<String, String> branchOptionalData) {
    this.branchOptionalData = branchOptionalData;
  }
}
