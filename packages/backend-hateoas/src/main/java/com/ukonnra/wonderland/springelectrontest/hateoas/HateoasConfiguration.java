package com.ukonnra.wonderland.springelectrontest.hateoas;

import com.ukonnra.wonderland.springelectrontest.CoreConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
  CoreConfiguration.class,
})
@ComponentScan(basePackageClasses = HateoasConfiguration.class)
public class HateoasConfiguration {}
