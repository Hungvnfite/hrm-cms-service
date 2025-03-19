package com.example.cms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients // Kích hoạt Feign Clients
public class HrmServiceCmsApplication {

	public static void main(String[] args) {
		SpringApplication.run(HrmServiceCmsApplication.class, args);
	}

}
