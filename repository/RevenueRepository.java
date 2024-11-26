package za.co.nharire.order_ms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.co.nharire.order_ms.model.revenue.Revenue;

@Repository
public interface RevenueRepository extends JpaRepository<Revenue, Integer> {
}
