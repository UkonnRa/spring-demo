package com.ukonnra.wonderland.springelectrontest.desktop;

import com.ukonnra.wonderland.springelectrontest.CoreConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(CoreConfiguration.class)
public class EndpointDesktopConfiguration {}
