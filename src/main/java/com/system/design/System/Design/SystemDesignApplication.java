package com.system.design.System.Design;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Main Application Class for System Design Problems and Solutions
 * 
 * This application contains multiple system design implementations:
 * - Parking Lot Management System
 * - Zepto (Quick Commerce) System
 * - URL Shortener Service
 * - Chat System
 * - Food Delivery Platform
 * - And many more...
 * 
 * Switch between designs using spring.profiles.active property
 * 
 * @author Shailender Kumar
 * @version 1.0.0
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.system.design")
@EnableJpaRepositories(basePackages = "com.system.design")
@EnableTransactionManagement
@EnableCaching
@EnableAsync
@EnableScheduling
public class SystemDesignApplication {

	public static void main(String[] args) {
		SpringApplication.run(SystemDesignApplication.class, args);
		
		System.out.println("\n" +
			"=".repeat(60) + "\n" +
			"    System Design Application Started Successfully!\n" +
			"=".repeat(60) + "\n" +
			"📚 Documentation: http://localhost:8080/swagger-ui.html\n" +
			"🗄️  H2 Database: http://localhost:8080/h2-console\n" +
			"📊 Actuator: http://localhost:8080/actuator\n" +
			"📖 API Docs: http://localhost:8080/api-docs\n" +
			"=".repeat(60) + "\n" +
			"Switch profiles in application.properties to run different designs:\n" +
			"- parking-lot: Parking Lot Management System\n" +
			"- zepto: Quick Commerce Platform\n" +
			"- url-shortener: URL Shortening Service\n" +
			"- chat-system: Real-time Chat System\n" +
			"- food-delivery: Food Delivery Platform\n" +
			"=".repeat(60) + "\n"
		);
	}

}
