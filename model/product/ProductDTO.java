package za.co.nharire.order_ms.model.product;

import lombok.Data;

import java.io.Serializable;

@Data
public class ProductDTO implements Serializable {

    private String productID;
    private String name;
    private int price;
    private String category;
}
