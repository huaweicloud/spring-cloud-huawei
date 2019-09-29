package org.springframework.cloud.dtm;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.huawei.middleware.dtm.client.context.DTMContext;

/**
 * @Author wangqijun
 * @Date 10:34 2019-09-29
 **/
public class DtmContextDTOTest {

  @Test
  public void fromDtmContext() {
    DTMContext dtmContext = DTMContext.getDTMContext();
    dtmContext.setGlobalTxId(100);
    dtmContext.setBranchTxId(200);
    dtmContext.setParentTxId(300);
    dtmContext.setChannelKey("ck");
    dtmContext.setCustomizedData("cz");
    DtmContextDTO dto = DtmContextDTO.fromDtmContext(dtmContext);
    assertEquals(dto.getGlobalTxId(), dtmContext.getGlobalTxId());
    assertEquals(dto.getBranchTxId(), dtmContext.getBranchTxId());
    assertEquals(dto.getChannelKey(), dtmContext.getChannelKey());
    assertEquals(dto.getCustomizedData(), dtmContext.getCustomizedData());
    assertEquals(dto.getGlobalOptionalData(), dtmContext.getGlobalOptionalData());
    assertEquals(dto.getBranchOptionalData(), dtmContext.getBranchOptionalData());
  }
}