# 版本发布指南

版本发布是将项目的 jar 包发布到 maven 中央库。

## 准备
1. 需要首先申请 maven 中央库的权限，并准备好个人 gpg 秘钥。 具体操作可以参考[说明][osstype guide] 。
2. 每一个版本都对应一个 milestone。 发布版本前，关闭相应的 milestone，将不处理的 issue 
  移动到下一个 milestone。 参考 [github issue][issues]。 
3. 配置好编译环境， maven, gpg等。

## 发布版本

1. 修改 pom 为目标版本号，并提交 PR 合入。

        mvn versions:set -DgenerateBackupPoms=false -DnewVersion=1.5.0
        
2. 下载项目代码。

        git clone https://github.com/huaweicloud/spring-cloud-huawei.git
        
3. 切换到需要发布的分支

        git checkout master
        
4. 做一个简单的验证，保证代码正确

        mvn clean install -Pit -Pdocker -Dmaven.javadoc.skip=true -Dcheckstyle.skip=false -Drat.skip=false
        
5. 发布

        mvn clean deploy -Prelease

6. 生成 release notes 。 在 github 页面打上 tag， 书写 release notes。

7. 发布完成，更新 SNAPSHOT 版本，并提交 PR

        mvn versions:set -DgenerateBackupPoms=false -DnewVersion=1.3.1-SNAPSHOT

## 版本验证

发布到中央仓库后，需要几个小时可以完成同步。仓库同步后，使用 [spring-cloud-huawei-samples][spring-cloud-huawei-samples]
项目进行验证。 并更新相关示例项目到最新版本。 

1. 下载 spring-cloud-huawei-samples 项目

        git clone https://github.com/huaweicloud/spring-cloud-huawei-samples

2. 修改版本号

        <spring-cloud-huawei.version>1.3.5</spring-cloud-huawei.version>

3. 启动并运行相关示例项目， 详细参考项目的 README。




[osstype guide]: https://www.cnblogs.com/softidea/p/6743108.html
[spring-cloud-huawei-samples]: https://github.com/huaweicloud/spring-cloud-huawei-samples
[issues]: https://github.com/huaweicloud/spring-cloud-huawei/issues
