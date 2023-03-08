package io.netty.learn.business.auth;

import io.netty.learn.common.OperationResult;

public class AuthOperationResult extends OperationResult {
    private final boolean passAuth;


    public AuthOperationResult(boolean passAuth) {
        this.passAuth = passAuth;
    }

    public Boolean isPass(){
        return passAuth;
    }

    @Override
    public String toString() {
        return "AuthOperationResult{" +
                "passAuth=" + passAuth +
                '}';
    }
}
