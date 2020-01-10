package com.huaweicloud.common.util;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * @Author GuoYl123
 * @Date 2020/1/10
 **/
public class JsonUtils {

  public static final ObjectMapper OBJ_MAPPER;

  static {
    OBJ_MAPPER = new ObjectMapper();
    OBJ_MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    OBJ_MAPPER.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

    SimpleModule partDeserializeModule = new SimpleModule("partDeserializeModule",
        new Version(0, 0, 1, null, "javax.servlet", "javax.servlet-api")
    );
    //todo: have a look
//    partDeserializeModule.addSerializer(Part.class, new JavaxServletPartSerializer());
//    partDeserializeModule.addDeserializer(Part.class, new JavaxServletPartDeserializer());
    OBJ_MAPPER.registerModule(partDeserializeModule);
  }

}
