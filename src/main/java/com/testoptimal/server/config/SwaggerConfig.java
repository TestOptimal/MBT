package com.testoptimal.server.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@ComponentScan("com.testoptimal.server.controller")
@Configuration

// PROBLEM is here
//@EnableSwagger2
public class SwaggerConfig 
{
//	@Bean
//	public Docket api() 
//	{
//        return new Docket(DocumentationType.SWAGGER_2) 
//                .select()
//                .apis(RequestHandlerSelectors.any())
////                .apis(Predicates.not(RequestHandlerSelectors.basePackage("org.springframework.boot")))              
//                .paths(PathSelectors.any())
//                .build();
//	}
}
