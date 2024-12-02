package za.co.nharire.order_ms.model.product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "productdetails")
public class ProductDetails {

    @Id
    @Column(name = "productID")
    private String productID;
    @Column(name = "name")
    private String name;
    @Column(name = "price")
    private int price;
    @Column(name = "category")
    private String category;

}
