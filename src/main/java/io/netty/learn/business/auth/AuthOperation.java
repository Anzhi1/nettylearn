package io.netty.learn.business.auth;

import io.netty.learn.common.Operation;
import io.netty.learn.common.OperationResult;

public class AuthOperation extends Operation {

    private final String userName;
    private final String passWord;

    public AuthOperation(String userName, String passWord) {
        this.userName = userName;
        this.passWord = passWord;
    }

    @Override
    public OperationResult execute() {
        if("admin".equals(this.userName)){
            return new AuthOperationResult(true);
        }
        return new AuthOperationResult(false);
    }

}
