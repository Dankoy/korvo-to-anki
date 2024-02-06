package ru.dankoy.korvotoanki.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.shell.command.annotation.CommandScan;

@Configuration
@CommandScan(value = "ru.dankoy.korvotoanki.core.command")
public class CommandConfig {}
