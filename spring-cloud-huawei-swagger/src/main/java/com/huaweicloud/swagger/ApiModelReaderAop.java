package com.huaweicloud.swagger;

import java.util.Map;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.util.CollectionUtils;
import springfox.documentation.schema.Model;
import springfox.documentation.spring.web.scanners.ApiListingScanningContext;

/**
 * @Author GuoYl123
 * @Date 2019/12/27
 **/
@Aspect
public class ApiModelReaderAop {

  @AfterReturning(value = "execution(* springfox.documentation.spring.web.scanners.ApiModelReader.read(..))", returning = "result")
  public void afterDefReturning(Object result) {
    Map<String, Model> res = (Map<String, Model>) result;
    if (!CollectionUtils.isEmpty(res)) {
      res.forEach((k, v) -> {
        DefinitionCache.setDefinition(k, v.getQualifiedType());
      });
    }
  }

//  @Pointcut(value="execution(* springfox.documentation.spring.web.scanners.ApiListingScanner.scan(..)) && args(args)",argNames="args")
//  public void pointcut(ApiListingScanningContext args){}

  @Before(value = "execution(* springfox.documentation.spring.web.scanners.ApiListingScanner.scan(..)) && args(args)", argNames = "args")
  public void beforeParseSchema(ApiListingScanningContext args) {
    args.getRequestMappingsByResourceGroup().forEach((k, v) -> {
      DefinitionCache.setSchemaClass(k.getGroupName(), k.getControllerClass().get().getName());
    });
  }
}
