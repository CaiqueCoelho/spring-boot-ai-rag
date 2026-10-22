package com.caique.adventOfSpring;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.validation.annotation.Validated;

import java.net.URI;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

@ConfigurationProperties("my.properties")
@Validated
public record SomeConfiguration(
        @NotNull
        URI url,
        boolean activated,
        @MaxDuration(max = 3600, message = "It's not wokring because {max} is not set properly")
        @DurationUnit(ChronoUnit.SECONDS)
        Duration timeout) {
}
