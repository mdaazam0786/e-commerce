package com.example.ecommerce.cart.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableMongoAuditing
@EnableMongoRepositories(basePackages = {
    "com.example.ecommerce.cart.repository"
})
public class MongoConfig extends AbstractMongoClientConfiguration {
    
    @Value("${spring.data.mongodb.database:cartdb}")
    private String databaseName;
    
    @Value("${spring.data.mongodb.uri:mongodb://localhost:27017/cartdb}")
    private String mongoUri;
    
    @Value("${spring.data.mongodb.min-pool-size:10}")
    private int minPoolSize;
    
    @Value("${spring.data.mongodb.max-pool-size:100}")
    private int maxPoolSize;
    
    @Value("${spring.data.mongodb.max-wait-time:120000}")
    private int maxWaitTime;
    
    @Value("${spring.data.mongodb.max-connection-idle-time:60000}")
    private int maxConnectionIdleTime;
    
    @Value("${spring.data.mongodb.max-connection-life-time:0}")
    private int maxConnectionLifeTime;
    
    @Value("${spring.data.mongodb.connect-timeout:10000}")
    private int connectTimeout;
    
    @Value("${spring.data.mongodb.socket-timeout:0}")
    private int socketTimeout;
    
    @Value("${spring.data.mongodb.server-selection-timeout:30000}")
    private int serverSelectionTimeout;
    
    @Override
    protected String getDatabaseName() {
        return databaseName;
    }
    
    @Override
    protected Collection<String> getMappingBasePackages() {
        return Collections.singleton("com.example.ecommerce.cart.model");
    }
    
    @Override
    public MongoClient mongoClient() {
        ConnectionString connectionString = new ConnectionString(mongoUri);
        
        MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
            .applyConnectionString(connectionString)
            .applyToConnectionPoolSettings(builder -> builder
                .minSize(minPoolSize)
                .maxSize(maxPoolSize)
                .maxWaitTime(maxWaitTime, TimeUnit.MILLISECONDS)
                .maxConnectionIdleTime(maxConnectionIdleTime, TimeUnit.MILLISECONDS)
                .maxConnectionLifeTime(maxConnectionLifeTime, TimeUnit.MILLISECONDS))
            .applyToSocketSettings(builder -> builder
                .connectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
                .readTimeout(socketTimeout, TimeUnit.MILLISECONDS))
            .applyToClusterSettings(builder -> builder
                .serverSelectionTimeout(serverSelectionTimeout, TimeUnit.MILLISECONDS))
            .build();
        
        return MongoClients.create(mongoClientSettings);
    }
    
    @Bean
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongoClient(), getDatabaseName());
    }
    
    @Override
    protected boolean autoIndexCreation() {
        return true;
    }
}
