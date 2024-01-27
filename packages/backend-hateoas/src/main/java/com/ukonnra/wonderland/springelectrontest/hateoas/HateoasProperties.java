package com.ukonnra.wonderland.springelectrontest.hateoas;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.hateoas")
public record HateoasProperties(String domainName) {}
