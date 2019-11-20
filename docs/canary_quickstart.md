##  Quick Start

### step 1 add jar 

import spring-cloud-starter-huawei-router,for canary release

        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-huawei-router</artifactId>
        </dependency>

  
### step 2 config route management rule
Define route management rule through configuration file (application.yml)

[rule info](https://docs.go-chassis.com/user-guides/router.html)

#### main process：

Match：
1. Read all rules under the target service (provider) whose service name matches.
2. Determine the rules according to the priority and the header in match.
3. After the match, select the unique route rule to enter the next stage.
(if there is no match, the gray level will not take effect.)

Distribute:
1. Allocate traffic to instances of different versions and tags according to the proportion of weight.
(no matching tags go to the latest version)
2. Select the qualified server to call.

*tips:* config tags for instance：

    instance_description:
      properties:
        tags:
          tag_key: tag_value

Start the service, and the traffic can be allocated according to the established rules.