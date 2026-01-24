package com.hdtpt.pentachat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot RESTful API Application
 * 
 * Features:
 * - User Authentication (Register, Login)
 * - Wallet Management (Deposit, Withdraw, Transfer, Balance)
 * - Transaction History
 * 
 * Architecture:
 * - Layered Architecture: Controller → Service → Data Access
 * - Business logic is independent of data source (mock or database)
 * - Clean separation of concerns with DataApi interface abstraction
 */
@SpringBootApplication
public class ProjectGaugeApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProjectGaugeApplication.class, args);
	}

}
