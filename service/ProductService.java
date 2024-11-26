package za.co.nharire.order_ms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import za.co.nharire.order_ms.model.product.ProductDTO;
import za.co.nharire.order_ms.model.product.ProductDetails;
import za.co.nharire.order_ms.repository.ProductRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j

public class ProductService {

    private ProductRepository productRepository;

    public ProductDTO saveProduct(ProductDTO productDTO) {

        ProductDetails product = new ProductDetails();
        BeanUtils.copyProperties(productDTO, product);

        log.info(" Save Product to DB");
        ProductDetails product1 = productRepository.save(product);

        BeanUtils.copyProperties(product1, productDTO);
        return productDTO;

    }

    public List<ProductDTO> getALlProducts() {

        log.info("IN SERVICE ");

        List<ProductDTO> productDTOList = new ArrayList<>();

        List<ProductDetails> productList = productRepository.findAll();
        for (ProductDetails productDetails : productList) {
            ProductDTO productDTO = new ProductDTO();
            BeanUtils.copyProperties(productDetails, productDTO);
            productDTOList.add(productDTO);
        }
        return productDTOList;
    }
}
