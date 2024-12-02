package za.co.nharire.order_ms.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import za.co.nharire.order_ms.model.product.ProductDTO;
import za.co.nharire.order_ms.service.ProductService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j

public class ProductController {

    private final ProductService productService;

    @PostMapping("product/save")
    public ResponseEntity<ProductDTO> saveProducts(@RequestBody ProductDTO productDTO) {

        log.info("SAVING PRODUCT", productDTO.toString());

        ProductDTO productDTO1 = productService.saveProduct(productDTO);
        if (productDTO1 != null) {
            return new ResponseEntity<>(productDTO1, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/product/all")
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        log.info("GETTING ALL PRODUCT IN DB ");

        List<ProductDTO> productDTOList = productService.getALlProducts();

        if (productDTOList != null) {
            return new ResponseEntity<>(productDTOList, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
