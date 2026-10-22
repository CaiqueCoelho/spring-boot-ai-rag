package com.caique.adventOfSpring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;

@SpringBootApplication
@EnableConfigurationProperties(SomeConfiguration.class)
public class AdventOfSpringApplication {

	@Value("${my.properties.url}")
	private URI url;

	private static final Logger logger = LoggerFactory.getLogger(AdventOfSpringApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(AdventOfSpringApplication.class, args);
	}

	@Bean
	@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix = "app.rag", name = "enabled", havingValue = "true")
	CommandLineRunner commandLineRunner(SomeConfiguration someConfiguration, RagService ragService) {
		return args -> {
			logger.info("This url is {}", someConfiguration.url());
			logger.info("This url with @Value is {}", url);
			logger.info("This activation state is {}", someConfiguration.activated());
			logger.info("This timeout is {}", someConfiguration.timeout().toSeconds());

			String question = "What is the best developer conference?";
			logger.info("Question {}", question);
			logger.info(ragService.ask(question));
		};
	}

	@Bean
	@org.springframework.boot.autoconfigure.condition.ConditionalOnBean(ProductServiceInterface.class)
	CommandLineRunner commandLineRunner2(ProductServiceInterface productService) {
		return  args -> {
			var before = Instant.now();
			var first = productService.getProductById(1);
			var middle = Instant.now();
			var second = productService.getProductById(1);
			var end = Instant.now();

			Duration withoutCache = Duration.between(before, middle);
			Duration withCache = Duration.between(middle, end);

			System.out.println("It takes " + withoutCache.toMillis() + " ms without cache.");
			System.out.println("It takes " + withCache.toMillis() + " ms with cache.");
			System.out.println(productService.getProductById(1));
		};
	}
}
