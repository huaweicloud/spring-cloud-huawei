
## Design thinking of DTM

Under the microservice architecture, multiple requests are invoked at a time, and each service is only called by the interface and is isolated by the interface. Therefore, the consistency of the data is greatly affected.
Two-phase commit (2PC) will affect performance, increased database pressure, and the database is difficult to scale relative to business services.
DTM use TCC distributed transaction design pattern, which has higher performance than the two-phase commit and does not lock the database data. Developers only need to pass
the annotations mark the parameters of the distributed transaction, and the DTM does not need to control the logic of the transaction itself. The DTM is provided to the developer in the form of a service, which reduces the development difficulty.

### Transaction processing

![avatar](https://support.huaweicloud.com/en-us/devg-servicestage/en-us_image_0166738635.png)

Process description:

Step 1. When a global transaction is initiated, the system first applies to the DTM cluster for a global transaction ID. The subsequent process can be continued only after the application is successful.

Step 2.1. The transaction initiator transparently transmits the applied global transaction ID to the called transaction participant.

Step 2.2. The transaction participant uses the obtained global transaction ID to apply for a branch transaction ID from the DTM cluster. The subsequent process of the transaction participant can be continued only after the application is successful.

Step 2.3. The transaction participant completes its own service logic (that is, completes the Try phase in TCC).

Step 2.4. The transaction participant uploads its own service logic result to the DTM cluster, indicating that the branch transaction ends.

Steps 3.1–3.4. Similar to steps 2.1–2.4, the service logic of the remaining transaction participants completes.

Step 4. The transaction initiator initiates TCC phase 2.

Step 5.1. The DTM cluster finds the transaction participant based on the global transaction ID and initiates TCC phase 2.

Step 5.2. Similar to step 5.1, TCC phase 2 of the remaining transaction participants completes.

Step 6. The DTM cluster notifies the transaction initiator of ending of a global transaction.

[更多文档](https://support.huaweicloud.com/devg-servicestage/cse_dtm_0002.html)