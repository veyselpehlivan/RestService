package com.fourplay;

import com.github.messenger4j.MessengerPlatform;
import com.github.messenger4j.send.MessengerSendClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(basePackages = "com.fourplay.repository")
@SpringBootApplication
public class RestServiceApplication {

	private static final Logger logger = LoggerFactory.getLogger(RestServiceApplication.class);


	@Bean
	public MessengerSendClient messengerSendClient(@Value("${messenger4j.pageAccessToken}") String pageAccessToken) {
		logger.debug("Initializing MessengerSendClient - pageAccessToken: {}", pageAccessToken);
		return MessengerPlatform.newSendClientBuilder(pageAccessToken).build();
	}

	public static void main(String[] args) {
		SpringApplication.run(RestServiceApplication.class, args);
	}
}
