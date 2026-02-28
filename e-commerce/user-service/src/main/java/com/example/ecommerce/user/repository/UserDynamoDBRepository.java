package com.example.ecommerce.user.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.example.ecommerce.user.model.UserDynamoDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class UserDynamoDBRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(UserDynamoDBRepository.class);
    
    private final DynamoDBMapper dynamoDBMapper;
    
    @Autowired
    public UserDynamoDBRepository(DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;
    }
    
    public UserDynamoDB save(UserDynamoDB user) {
        if (logger.isDebugEnabled()) {
            logger.debug("Saving user to DynamoDB: {}", user.getEmail());
        }
        
        try {
            dynamoDBMapper.save(user);
            logger.info("User saved to DynamoDB successfully: {}", user.getEmail());
            return user;
        } catch (Exception e) {
            logger.error("Error saving user to DynamoDB: {}", user.getEmail(), e);
            throw new RuntimeException("Failed to save user to DynamoDB", e);
        }
    }
    
    public UserDynamoDB findByEmail(String email) {
        if (logger.isDebugEnabled()) {
            logger.debug("Finding user in DynamoDB by email: {}", email);
        }
        
        try {
            UserDynamoDB user = dynamoDBMapper.load(UserDynamoDB.class, email);
            
            if (user != null) {
                logger.info("User found in DynamoDB: {}", email);
            } else {
                logger.debug("User not found in DynamoDB: {}", email);
            }
            
            return user;
        } catch (Exception e) {
            logger.error("Error finding user in DynamoDB: {}", email, e);
            return null;
        }
    }
    
    public UserDynamoDB findByUserId(Long userId) {
        if (logger.isDebugEnabled()) {
            logger.debug("Finding user in DynamoDB by userId: {}", userId);
        }
        
        try {
            Map<String, AttributeValue> eav = new HashMap<>();
            eav.put(":val1", new AttributeValue().withN(String.valueOf(userId)));
            
            DynamoDBQueryExpression<UserDynamoDB> queryExpression = new DynamoDBQueryExpression<UserDynamoDB>()
                .withIndexName("userId-index")
                .withConsistentRead(false)
                .withKeyConditionExpression("userId = :val1")
                .withExpressionAttributeValues(eav);
            
            PaginatedQueryList<UserDynamoDB> queryList = dynamoDBMapper.query(UserDynamoDB.class, queryExpression);
            
            if (queryList != null && !queryList.isEmpty()) {
                logger.info("User found in DynamoDB by userId: {}", userId);
                return queryList.get(0);
            }
            
            logger.debug("User not found in DynamoDB by userId: {}", userId);
            return null;
        } catch (Exception e) {
            logger.error("Error finding user in DynamoDB by userId: {}", userId, e);
            return null;
        }
    }
    
    public UserDynamoDB findByPhone(String phone) {
        if (logger.isDebugEnabled()) {
            logger.debug("Finding user in DynamoDB by phone: {}", phone);
        }
        
        try {
            Map<String, AttributeValue> eav = new HashMap<>();
            eav.put(":val1", new AttributeValue().withS(phone));
            
            DynamoDBQueryExpression<UserDynamoDB> queryExpression = new DynamoDBQueryExpression<UserDynamoDB>()
                .withIndexName("phone-index")
                .withConsistentRead(false)
                .withKeyConditionExpression("phone = :val1")
                .withExpressionAttributeValues(eav);
            
            PaginatedQueryList<UserDynamoDB> queryList = dynamoDBMapper.query(UserDynamoDB.class, queryExpression);
            
            if (queryList != null && !queryList.isEmpty()) {
                logger.info("User found in DynamoDB by phone: {}", phone);
                return queryList.get(0);
            }
            
            logger.debug("User not found in DynamoDB by phone: {}", phone);
            return null;
        } catch (Exception e) {
            logger.error("Error finding user in DynamoDB by phone: {}", phone, e);
            return null;
        }
    }
    
    public void deleteByEmail(String email) {
        if (logger.isDebugEnabled()) {
            logger.debug("Deleting user from DynamoDB: {}", email);
        }
        
        try {
            UserDynamoDB user = new UserDynamoDB();
            user.setEmail(email);
            dynamoDBMapper.delete(user);
            logger.info("User deleted from DynamoDB successfully: {}", email);
        } catch (Exception e) {
            logger.error("Error deleting user from DynamoDB: {}", email, e);
        }
    }
}
