package com.xuecheng.test.rabbitmq;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Consumer01 {
    private static final String QUEUE = "helloWord";

    public static void main(String[] args) throws Exception {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");
        connectionFactory.setPort(5672);//端口
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");
        connectionFactory.setVirtualHost("/");

        //建立线连接
        Connection connection =connectionFactory.newConnection();
        //建立会话通道,生产者和mq的所有通信都在channel通道中完成
        Channel channel = connection.createChannel();

        //实现消费方法
        DefaultConsumer defaultConsumer = new DefaultConsumer(channel){
            //当接收到消息后 此方法被调用

            //consumerTag :消费者标签 用来标识消费者的 在监听队列时设置channel.basicConsum
            //envelope：信封
            //properties：消息的属性
            //body：消息内容

            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                //super.handleDelivery(consumerTag, envelope, properties, body);
                //交换机
                String exchange = envelope.getExchange();
                //路由key
                String routingKey = envelope.getRoutingKey();
                //消息id mq在channel中用来标识消息的id，可用于确认消息已接收
                long deliveryTag = envelope.getDeliveryTag();
                String message = new String(body,"utf-8");
                System.out.println("consumer"+message);
            }
        };


        //声明队列
        channel.queueDeclare(QUEUE,true,false,false,null);

        //监听队列
        //String var1, boolean var2, Consumer var3
        //var1:队列名称
        //var2:自动回复 当消费者接收到消息后 告诉mq消息已接受 true自动回复 false通过编程回复
        //Consumer var3：消费方法

        channel.basicConsume(QUEUE,true,defaultConsumer);

    }
}
