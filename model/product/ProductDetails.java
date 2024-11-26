package za.co.nharire.order_ms.model.product;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@RequiredArgsConstructor
@Entity
@Table(name = "productdetails")
public class ProductDetails {

    @Id
    private String productID;
    private String name;
    private int price;
    private String category;

}
