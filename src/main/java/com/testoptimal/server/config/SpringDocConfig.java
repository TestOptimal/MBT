package com.testoptimal.server.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@Configuration
@SecurityScheme(
  type = SecuritySchemeType.HTTP,
  name = "basicAuth",
  scheme = "basic")
public class SpringDocConfig {}