package com.oms.adminbff.catalog;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CatalogDealRepository extends JpaRepository<CatalogDeal, String> {
    List<CatalogDeal> findAllByOrderByStoreAscNameAsc();
}
