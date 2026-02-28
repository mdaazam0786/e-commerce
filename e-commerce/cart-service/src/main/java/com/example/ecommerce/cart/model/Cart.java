package com.example.ecommerce.cart.model;

import com.example.ecommerce.common.entity.AbstractMongoDocument;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "carts")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Cart extends AbstractMongoDocument {
    private String userEmail;
    private List<CartItem> items = new ArrayList<>();
    
    public BigDecimal getTotal() {
        return items.stream()
            .map(CartItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
