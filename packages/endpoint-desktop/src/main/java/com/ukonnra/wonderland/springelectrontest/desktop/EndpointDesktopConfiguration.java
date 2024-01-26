package com.ukonnra.wonderland.springelectrontest.desktop;

import com.ukonnra.wonderland.springelectrontest.hateoas.HateoasConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({HateoasConfiguration.class})
public class EndpointDesktopConfiguration {}
