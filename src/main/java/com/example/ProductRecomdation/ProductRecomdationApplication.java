package com.example.ProductRecomdation;


import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ProductRecomdationApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductRecomdationApplication.class, args);
	}

	@Bean
	public JsonMapper jsonMapper() {
		return JsonMapper.builder().build();
	}

}
