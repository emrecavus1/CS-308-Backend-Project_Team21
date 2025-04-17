package com.cs308.backend.models;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.*;
import com.cs308.backend.models.*;
import com.cs308.backend.models.CartItem;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "cart")
public class Cart {
    @Id
    private String cartId;
    private String userId;

    /** Now each entry has both productId and quantity */
    private List<CartItem> items = new ArrayList<>();
}
