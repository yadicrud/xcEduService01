package com.xuecheng.govern.center;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Created with IntelliJ IDEA.
 * 
 * @Description:
 * @param: 
 * @return: 
 * @Auther: liuyadi
 * @Date: 2021/1/23
 */
@EnableEurekaServer //标识为eureka
@SpringBootApplication
public class GoverCenterApplication {
    public static void main(String[] args) {
        SpringApplication.run(GoverCenterApplication.class);
    }
}
