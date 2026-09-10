package com.mob10.deliveryserver.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "com.mob10.deliveryserver.repository")
public class MongoConfig {
}
