package com.example.ecommerce.product.controller;

import com.example.ecommerce.common.dto.UIBean;
import com.example.ecommerce.common.dto.UIBeanPaginated;
import com.example.ecommerce.product.dto.CreateProductRequest;
import com.example.ecommerce.product.dto.ProductDto;
import com.example.ecommerce.product.dto.UpdateProductRequest;
import com.example.ecommerce.product.dto.UpdateStockRequest;
import com.example.ecommerce.product.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
    
    private final ProductService productService;
    
    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    
    @GetMapping
    public ResponseEntity<UIBeanPaginated<List<ProductDto>>> getAllProducts(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize) {
        
        if (logger.isDebugEnabled()) {
            logger.debug("Fetching all products, page: {}, pageSize: {}", page, pageSize);
        }
        
        try {
            Page<ProductDto> productPage = productService.getAllProducts(page, pageSize);
            
            UIBeanPaginated<List<ProductDto>> response = UIBeanPaginated.success(
                productPage.getContent(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.getNumber(),
                productPage.getSize()
            );
            
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error fetching all products", e);
            UIBeanPaginated<List<ProductDto>> errorResponse = new UIBeanPaginated<>();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Error occurred: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UIBean<ProductDto>> getProductById(@PathVariable Long id) {
        if (logger.isDebugEnabled()) {
            logger.debug("Fetching product by ID: {}", id);
        }
        
        try {
            ProductDto product = productService.getProductById(id);
            UIBean<ProductDto> response = UIBean.success(product, "Product retrieved successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error fetching product by ID: {}", id, e);
            UIBean<ProductDto> errorResponse = new UIBean<>();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Error occurred: " + e.getMessage());
            errorResponse.setResponse("ERROR");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/category/{category}")
    public ResponseEntity<UIBeanPaginated<List<ProductDto>>> getProductsByCategory(
            @PathVariable String category,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize) {
        
        if (logger.isDebugEnabled()) {
            logger.debug("Fetching products by category: {}, page: {}, pageSize: {}", category, page, pageSize);
        }
        
        try {
            Page<ProductDto> productPage = productService.getProductsByCategory(category, page, pageSize);
            
            UIBeanPaginated<List<ProductDto>> response = UIBeanPaginated.success(
                productPage.getContent(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.getNumber(),
                productPage.getSize()
            );
            
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error fetching products by category: {}", category, e);
            UIBeanPaginated<List<ProductDto>> errorResponse = new UIBeanPaginated<>();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Error occurred: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<UIBeanPaginated<List<ProductDto>>> searchProducts(
            @RequestParam String name,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize) {
        
        if (logger.isDebugEnabled()) {
            logger.debug("Searching products by name: {}, page: {}, pageSize: {}", name, page, pageSize);
        }
        
        try {
            Page<ProductDto> productPage = productService.searchProducts(name, page, pageSize);
            
            UIBeanPaginated<List<ProductDto>> response = UIBeanPaginated.success(
                productPage.getContent(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.getNumber(),
                productPage.getSize()
            );
            
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error searching products by name: {}", name, e);
            UIBeanPaginated<List<ProductDto>> errorResponse = new UIBeanPaginated<>();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Error occurred: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @PostMapping
    public ResponseEntity<UIBean<ProductDto>> createProduct(@RequestBody CreateProductRequest request) {
        if (logger.isDebugEnabled()) {
            logger.debug("Creating product: {}", request.getName());
        }
        
        try {
            ProductDto product = productService.createProduct(request);
            UIBean<ProductDto> response = UIBean.success(product, "Product created successfully");
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error creating product", e);
            UIBean<ProductDto> errorResponse = new UIBean<>();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Error occurred: " + e.getMessage());
            errorResponse.setResponse("ERROR");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<UIBean<ProductDto>> updateProduct(
            @PathVariable Long id, 
            @RequestBody UpdateProductRequest request) {
        
        if (logger.isDebugEnabled()) {
            logger.debug("Updating product ID: {}", id);
        }
        
        try {
            ProductDto product = productService.updateProduct(id, request);
            UIBean<ProductDto> response = UIBean.success(product, "Product updated successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error updating product ID: {}", id, e);
            UIBean<ProductDto> errorResponse = new UIBean<>();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Error occurred: " + e.getMessage());
            errorResponse.setResponse("ERROR");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<UIBean<Void>> deleteProduct(@PathVariable Long id) {
        if (logger.isDebugEnabled()) {
            logger.debug("Deleting product ID: {}", id);
        }
        
        try {
            productService.deleteProduct(id);
            UIBean<Void> response = UIBean.success(null, "Product deleted successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error deleting product ID: {}", id, e);
            UIBean<Void> errorResponse = new UIBean<>();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Error occurred: " + e.getMessage());
            errorResponse.setResponse("ERROR");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @PatchMapping("/{id}/stock")
    public ResponseEntity<UIBean<ProductDto>> updateStock(
            @PathVariable Long id, 
            @RequestBody UpdateStockRequest request) {
        
        if (logger.isDebugEnabled()) {
            logger.debug("Updating stock for product ID: {}, quantity: {}", id, request.getQuantity());
        }
        
        try {
            ProductDto product = productService.updateStock(id, request.getQuantity());
            UIBean<ProductDto> response = UIBean.success(product, "Stock updated successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error updating stock for product ID: {}", id, e);
            UIBean<ProductDto> errorResponse = new UIBean<>();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Error occurred: " + e.getMessage());
            errorResponse.setResponse("ERROR");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
