package com.ukonnra.wonderland.springelectrontest.desktop;

import com.ukonnra.wonderland.springelectrontest.CoreConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.shell.command.annotation.EnableCommand;

@Configuration
@Import(CoreConfiguration.class)
@EnableCommand(JournalController.class)
public class EndpointDesktopConfiguration {}
