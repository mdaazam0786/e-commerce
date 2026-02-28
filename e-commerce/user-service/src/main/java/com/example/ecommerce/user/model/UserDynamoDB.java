package com.example.ecommerce.user.model;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoDBTable(tableName = "users")
public class UserDynamoDB {
    
    @DynamoDBHashKey(attributeName = "email")
    private String email;
    
    @DynamoDBAttribute(attributeName = "userId")
    @DynamoDBIndexHashKey(globalSecondaryIndexName = "userId-index")
    private Long userId;
    
    @DynamoDBAttribute(attributeName = "firstName")
    private String firstName;
    
    @DynamoDBAttribute(attributeName = "lastName")
    private String lastName;
    
    @DynamoDBAttribute(attributeName = "phone")
    @DynamoDBIndexHashKey(globalSecondaryIndexName = "phone-index")
    private String phone;
    
    @DynamoDBAttribute(attributeName = "role")
    private String role;
    
    @DynamoDBAttribute(attributeName = "createdAt")
    private Long createdAt;
    
    @DynamoDBAttribute(attributeName = "updatedAt")
    private Long updatedAt;
    
    @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.BOOL)
    @DynamoDBAttribute(attributeName = "isActive")
    private Boolean isActive;
}
