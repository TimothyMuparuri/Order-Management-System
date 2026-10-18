package com.oms.adminbff.catalog;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CatalogStoreRepository extends JpaRepository<CatalogStore, String> {
    List<CatalogStore> findAllByOrderByNameAsc();
}
