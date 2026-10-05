package com.example.ProductRecomdation.Controller;

//package com.example.ProductRecommendation.Controller;

import com.example.ProductRecomdation.Model.ProductMatch;
import com.example.ProductRecomdation.Service.ProductService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // Search products using user query
    @GetMapping("/search")
    public List<ProductMatch> search(
            @RequestParam String query) {

        return productService.search(query);
    }

    // Find similar products
    @GetMapping("/similar/{title}")
    public List<ProductMatch> similarProducts(
            @PathVariable String title) {

        return productService.similarProduct(title);
    }
}
