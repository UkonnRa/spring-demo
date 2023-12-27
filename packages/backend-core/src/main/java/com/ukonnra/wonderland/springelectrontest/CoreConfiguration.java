package com.ukonnra.wonderland.springelectrontest;

import com.ukonnra.wonderland.springelectrontest.configuration.JpaConfiguration;
import com.ukonnra.wonderland.springelectrontest.configuration.JsonConfiguration;
import com.ukonnra.wonderland.springelectrontest.service.ReadService;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({JpaConfiguration.class, JsonConfiguration.class})
@ComponentScan(basePackageClasses = ReadService.class)
public class CoreConfiguration {}
