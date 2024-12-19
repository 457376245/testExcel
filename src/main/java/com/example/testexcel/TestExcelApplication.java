package com.example.testexcel;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;
import java.net.Proxy;

@SpringBootApplication
public class TestExcelApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestExcelApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("10.8.0.6", 8081)); // 例如："proxy.example.com", 8080
        factory.setProxy(proxy);
        factory.setConnectTimeout(5000); // 连接超时时间
        factory.setReadTimeout(5000);
        return new RestTemplate(factory);
    }
    

}
