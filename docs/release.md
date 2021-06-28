# 版本发布指南

版本发布是将项目的 jar 包发布到 maven 中央库。

## 准备
1. 需要首先申请 maven 中央库的权限，并准备好个人 gpg 秘钥。 具体操作可以参考[说明][osstype guide] 。
2. 每一个版本都对应一个 milestone。 发布版本前，关闭相应的 milestone，将不处理的 issue 
  移动到下一个 milestone。 参考 [github issue][issues]。 
3. 配置好编译环境， maven, gpg等。

## 发布版本前验证

1. clone Spring Cloud Huawei 代码
2. 手工执行 Integration Tests 下面的测试用例：cse-v1等， 详细步骤参考项目下面的README.md文件。

## 发布版本

1. 修改 pom 为目标版本号，并提交 PR 合入。

        mvn versions:set -DgenerateBackupPoms=false -DnewVersion=1.5.0
        
  部分 Dockerfile 里面也引用了版本号，需要排查一定修改。

2. 下载项目代码。

        git clone https://github.com/huaweicloud/spring-cloud-huawei.git
        
3. 切换到需要发布的分支

        git checkout master
        
4. 做一个简单的验证，保证代码正确

        mvn clean install -Pit -Pdocker -Dmaven.javadoc.skip=true -Dcheckstyle.skip=false -Drat.skip=false
        
5. 发布

        mvn clean deploy -Prelease -DskipTests

6. 生成 release notes 。 在 github 页面打上 tag， 书写 release notes。

7. 发布完成，更新 SNAPSHOT 版本，并提交 PR

        mvn versions:set -DgenerateBackupPoms=false -DnewVersion=1.3.1-SNAPSHOT

## 发布版本后验证

发布Staging中央仓库后，使用 [spring-cloud-huawei-samples][spring-cloud-huawei-samples]
项目进行验证，验证通过后在正式推送到中央仓库。 并更新相关示例项目到最新版本。 

1. 下载 spring-cloud-huawei-samples 项目

        git clone https://github.com/huaweicloud/spring-cloud-huawei-samples

2. 修改版本号

        <spring-cloud-huawei.version>1.3.5</spring-cloud-huawei.version>

3. 启动并运行相关示例项目， 详细参考项目的 README。
  * 使用本地微服务引擎进行验证
  * 使用微服务引擎专业版进行验证

[osstype guide]: https://www.cnblogs.com/softidea/p/6743108.html
[spring-cloud-huawei-samples]: https://github.com/huaweicloud/spring-cloud-huawei-samples
[issues]: https://github.com/huaweicloud/spring-cloud-huawei/issues
