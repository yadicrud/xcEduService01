package com.xuecheng.govern.gateway.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.exception.ZuulException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;

@Component
public class LoginFilter extends ZuulFilter {
    @Override
    //过滤器类型
    public String filterType() {
        //pre
        //routing
        //post在routing与error之后使用
        //error 处理请求时发生错误调用
        return "pre";
    }

    @Override
    //过滤器序号 越小粤北有限执行
    public int filterOrder() {
        return 0;
    }


    //是否需要执行 false不执行 true执行
    @Override
    public boolean shouldFilter() {
        return true;
    }

    //过滤器内容
    @Override
    public Object run() throws ZuulException {
        return null;
    }
}
