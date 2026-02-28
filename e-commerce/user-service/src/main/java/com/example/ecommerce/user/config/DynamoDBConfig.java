package com.example.ecommerce.user.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DynamoDBConfig {
    
    @Value("${aws.dynamodb.endpoint}")
    private String endpoint;
    
    @Value("${aws.dynamodb.region}")
    private String region;
    
    @Value("${aws.dynamodb.access-key}")
    private String accessKey;
    
    @Value("${aws.dynamodb.secret-key}")
    private String secretKey;
    
    @Bean
    public AmazonDynamoDB amazonDynamoDB() {
        return AmazonDynamoDBClientBuilder.standard()
            .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, region))
            .withCredentials(new AWSStaticCredentialsProvider(
                new BasicAWSCredentials(accessKey, secretKey)))
            .build();
    }
    
    @Bean
    public DynamoDBMapper dynamoDBMapper(AmazonDynamoDB amazonDynamoDB) {
        return new DynamoDBMapper(amazonDynamoDB, DynamoDBMapperConfig.DEFAULT);
    }
}
