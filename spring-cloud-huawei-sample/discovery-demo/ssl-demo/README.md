### ssl使用指南

本demo使用自签发的CA证书、信任证书作为例子，展示如何使用自定义证书对注册中心、配置中信进行通信加密

目录结构：
sdk端需要的证书文件配置：

    src
    |- resources
    |  |- server.p12  PKC12格式的身份证书
    |  |- trust.jks  keystore文件，作为信任证书

service center端需要的证书文件配置：

    sc_ssl_file
    |- etc
    |  |- ssl
    |  |  |- cert_pwd 解密私钥密码
    |  |  |- server.cer  身份证书
    |  |  |- server_key.pem  证书私钥
    |  |  |- trust.cer   CA机构证书

具体配置方法参考：
https://docs.servicecomb.io/service-center/zh_CN/security-tls.html