package com.xuecheng.order.service;

import org.apache.logging.log4j.CloseableThreadContext;
import sun.jvm.hotspot.oops.Instance;

public class TestService {

    public static void main(String[] args) {
        Integer b = new Integer(1000);
        Integer c = new Integer(1000);

        System.out.println(b == 1000);
        System.out.println(c == 1000);
        System.out.println(c == b);
    }
}
