package com.example.ecommerce.common.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.Date;

@Data
public abstract class AbstractMongoDocument implements Serializable, Persistable<String> {
    
    @Id
    private String id;
    
    @CreatedDate
    @Field("created_at")
    private Date createdAt;
    
    @LastModifiedDate
    @Field("updated_at")
    private Date updatedAt;
    
    @Override
    public String getId() {
        return this.id;
    }
    
    @Override
    public boolean isNew() {
        return this.id == null;
    }
}
