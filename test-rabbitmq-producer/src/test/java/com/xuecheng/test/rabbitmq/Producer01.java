package com.xuecheng.test.rabbitmq;


import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created with IntelliJ IDEA.
 *
 * @Description:
 * @param:
 * @return:
 * @Auther: liuyadi
 * @Date: 2020/12/12
 */
public class Producer01 {
    private static final String QUEUE = "helloWord";

    public static void main(String[] args) {
        //通过链接工厂创建新的链接和mq建立链接
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");
        connectionFactory.setPort(5672);//端口
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");
        connectionFactory.setVirtualHost("/");//rabbitmq默认虚拟机名称为“/”，虚拟机相当于一个独立的mq服务器

        try {
            //建立线连接
            Connection connection =connectionFactory.newConnection();
            //建立会话通道,生产者和mq的所有通信都在channel通道中完成
            Channel channel = connection.createChannel();
            //String queue, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments
            //1.queue 队列名
            //2.durable 是否持久化 如果持久化mq重启后队列还在
            //3.exclusive 是否独占链接，队列只允许在该链接中访问 如果connection链接关闭队列则自动删除，如果将此参数设置为true，可用于临时队列的创建
            //4.autoDelete 自动删除 队列不再使用时是否自动删除此队列，如果将此参数和exclusive设置为true就可以实现临时队列（队列不用了就自动删除）
            //5.arguments 参数 可以设置一个队列的扩展参数 比 可如可设置存活时间
            //声明队列
            channel.queueDeclare(QUEUE,true,false,false,null);
            String message = "helloworld"+System.currentTimeMillis();
            //发送消息
            //String var1, String var2, BasicProperties var4, byte[] var5
            //1.var1 交换机 如果不指定将使用mq默认的交换机 设置为“ ”
            //2.var2 路由key 交换机根据路由key来将消息转发到指定的队列 如果使用默认交换机 var2设置为队列的名称
            //3.BasicProperties 消息的属性
            //4.var5 消息内容

            channel.basicPublish("",QUEUE,null,message.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        } finally{

        }


    }

}
