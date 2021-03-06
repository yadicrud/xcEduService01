package com.xuecheng.auth.controller;

import com.xuecheng.api.auth.AuthControllerApi;
import com.xuecheng.auth.service.AuthService;
import com.xuecheng.framework.domain.ucenter.ext.AuthToken;
import com.xuecheng.framework.domain.ucenter.request.LoginRequest;
import com.xuecheng.framework.domain.ucenter.response.AuthCode;
import com.xuecheng.framework.domain.ucenter.response.JwtResult;
import com.xuecheng.framework.domain.ucenter.response.LoginResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.utils.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author Administrator
 * @version 1.0
 **/
@RestController
//@RequestMapping("/")
public class AuthController implements AuthControllerApi {


    @Value("${auth.clientId}")
    String clientId;
    @Value("${auth.clientSecret}")
    String clientSecret;
    @Value("${auth.cookieDomain}")
    String cookieDomain;
    @Value("${auth.cookieMaxAge}")
    int cookieMaxAge;

    @Autowired
    AuthService authService;

    @Override
    @PostMapping("/userlogin")
    public LoginResult login(LoginRequest loginRequest) {
        if(loginRequest == null || StringUtils.isEmpty(loginRequest.getUsername())){
            ExceptionCast.cast(AuthCode.AUTH_USERNAME_NONE);
        }
        if(loginRequest == null || StringUtils.isEmpty(loginRequest.getPassword())){
            ExceptionCast.cast(AuthCode.AUTH_PASSWORD_NONE);
        }
        //??????
        String username = loginRequest.getUsername();
        //??????
        String password = loginRequest.getPassword();

        //????????????
        AuthToken authToken =  authService.login(username,password,clientId,clientSecret);

        //??????????????????
        String access_token = authToken.getAccess_token();
        //??????????????????cookie
        this.saveCookie(access_token);

        return new LoginResult(CommonCode.SUCCESS,access_token);
    }

    //??????????????????cookie
    private void saveCookie(String token){

        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        //HttpServletResponse response,String domain,String path, String name, String value, int maxAge,boolean httpOnly
        CookieUtil.addCookie(response,cookieDomain,"/","uid",token,cookieMaxAge,false);

    }
    //??????cookie
    private void delCookie(String token){

        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        //HttpServletResponse response,String domain,String path, String name, String value, int maxAge,boolean httpOnly
        CookieUtil.addCookie(response,cookieDomain,"/","uid",token,0,false);

    }
    /**
     * Created with IntelliJ IDEA.
     * 
     * @Description:
     * @param: 
     * @return: 
     * @Auther: liuyadi
     * @Date: 2021/2/23
     */
    @Override
    @GetMapping("/userjwt")
    public JwtResult userjwt() {

        //??????cookie??????????????????
        String access_token = getTokenFormCookie();
        if(access_token == null){
            return new JwtResult(CommonCode.FAIL,null);
        }

        //???????????????redis??????jwt
        AuthToken authToken = authService.getUserToken(access_token);
        if(authToken == null){
            return new JwtResult(CommonCode.FAIL,null);
        }
        return new JwtResult(CommonCode.SUCCESS,authToken.getJwt_token());

    }
    @Override
    public ResponseResult logout() {
        String uid = this.getTokenFormCookie();
        //??????redis??????token
        authService.delToken(uid);
        //??????cookie

        this.delCookie(uid);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * Created with IntelliJ IDEA.
     *
     * @Description: ???cookie?????????????????????
     * @param:
     * @return:
     * @Auther: liuyadi
     * @Date: 2021/2/23
     */
    private String getTokenFormCookie(){
        HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
        Map<String, String> cookieMap = CookieUtil.readCookie(request, "uid");
        if(cookieMap!=null && cookieMap.get("uid")!=null ){
            String access_token = cookieMap.get("uid");
            return access_token;
        }
        return null;
    }


}
