package com.xuecheng.api.auth;

import com.xuecheng.framework.domain.ucenter.request.LoginRequest;
import com.xuecheng.framework.domain.ucenter.response.JwtResult;
import com.xuecheng.framework.domain.ucenter.response.LoginResult;
import com.xuecheng.framework.model.response.ResponseResult;

public interface AuthControllerApi {
    //登录
    public LoginResult login(LoginRequest loginRequest);

    //查询userjwt令牌
    public JwtResult userjwt();

    //退出
    public ResponseResult logout();
}
