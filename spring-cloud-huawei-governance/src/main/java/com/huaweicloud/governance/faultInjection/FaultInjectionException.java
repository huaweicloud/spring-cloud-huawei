package com.huaweicloud.governance.faultInjection;

import org.apache.servicecomb.injection.FaultResponse;

public class FaultInjectionException extends RuntimeException {
    private static final long serialVersionUID = 1675558351029273343L;
    private final FaultResponse faultResponse;

    public FaultInjectionException(FaultResponse faultResponse) {
        super(faultResponse.getErrorMsg());
        this.faultResponse = faultResponse;
    }

    public FaultResponse getFaultResponse() {
        return faultResponse;
    }
}
