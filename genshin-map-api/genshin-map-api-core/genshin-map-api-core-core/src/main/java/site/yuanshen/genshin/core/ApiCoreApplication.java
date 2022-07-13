package site.yuanshen.genshin.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class ApiCoreApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiCoreApplication.class, args);
    }
}
