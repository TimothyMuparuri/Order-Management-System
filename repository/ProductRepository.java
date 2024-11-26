package za.co.nharire.order_ms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.co.nharire.order_ms.model.product.ProductDetails;

@Repository
public interface ProductRepository extends JpaRepository<ProductDetails, String> {
}
