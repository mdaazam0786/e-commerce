package com.example.ecommerce.product.service;

import com.example.ecommerce.common.constants.PaginationConstants;
import com.example.ecommerce.common.exception.InvalidArgumentException;
import com.example.ecommerce.common.exception.ResourceNotFoundException;
import com.example.ecommerce.product.dto.CreateProductRequest;
import com.example.ecommerce.product.dto.ProductDto;
import com.example.ecommerce.product.dto.UpdateProductRequest;
import com.example.ecommerce.product.mapper.ProductMapper;
import com.example.ecommerce.product.model.Product;
import com.example.ecommerce.product.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

@Service
public class ProductService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    
    private final ProductRepository productRepository;
    
    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    
    public Page<ProductDto> getAllProducts(Integer page, Integer pageSize) {
        if (logger.isDebugEnabled()) {
            logger.debug("Fetching all products, page: {}, pageSize: {}", page, pageSize);
        }
        
        int size = pageSize != null ? Math.min(pageSize, PaginationConstants.MAX_PAGE_SIZE) 
            : PaginationConstants.DEFAULT_PAGE_SIZE;
        int pageNumber = page != null ? page : PaginationConstants.DEFAULT_PAGE;
        
        Sort sort = Sort.by("id").descending();
        Pageable pageable = PageRequest.of(pageNumber, size, sort);
        
        Page<Product> productPage = productRepository.findAll(pageable);
        return productPage.map(ProductMapper::toDto);
    }
    
    public ProductDto getProductById(Long id) {
        if (logger.isDebugEnabled()) {
            logger.debug("Fetching product by ID: {}", id);
        }
        
        if (id == null || id <= 0) {
            throw new InvalidArgumentException("Invalid product ID: " + id);
        }
        
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        
        return ProductMapper.toDto(product);
    }
    
    public Page<ProductDto> getProductsByCategory(String category, Integer page, Integer pageSize) {
        if (logger.isDebugEnabled()) {
            logger.debug("Fetching products by category: {}, page: {}, pageSize: {}", category, page, pageSize);
        }
        
        if (!StringUtils.hasText(category)) {
            throw new InvalidArgumentException("Category cannot be null or empty");
        }
        
        int size = pageSize != null ? Math.min(pageSize, PaginationConstants.MAX_PAGE_SIZE) 
            : PaginationConstants.DEFAULT_PAGE_SIZE;
        int pageNumber = page != null ? page : PaginationConstants.DEFAULT_PAGE;
        
        Sort sort = Sort.by("id").descending();
        Pageable pageable = PageRequest.of(pageNumber, size, sort);
        
        Page<Product> productPage = productRepository.findByCategory(category, pageable);
        return productPage.map(ProductMapper::toDto);
    }
    
    public Page<ProductDto> searchProducts(String name, Integer page, Integer pageSize) {
        if (logger.isDebugEnabled()) {
            logger.debug("Searching products by name: {}, page: {}, pageSize: {}", name, page, pageSize);
        }
        
        if (!StringUtils.hasText(name)) {
            throw new InvalidArgumentException("Search name cannot be null or empty");
        }
        
        int size = pageSize != null ? Math.min(pageSize, PaginationConstants.MAX_PAGE_SIZE) 
            : PaginationConstants.DEFAULT_PAGE_SIZE;
        int pageNumber = page != null ? page : PaginationConstants.DEFAULT_PAGE;
        
        Sort sort = Sort.by("id").descending();
        Pageable pageable = PageRequest.of(pageNumber, size, sort);
        
        Page<Product> productPage = productRepository.findByNameContainingIgnoreCase(name, pageable);
        return productPage.map(ProductMapper::toDto);
    }
    
    @Transactional
    public ProductDto createProduct(CreateProductRequest request) {
        if (logger.isDebugEnabled()) {
            logger.debug("Creating product: {}", request.getName());
        }
        
        validateCreateProductRequest(request);
        
        Product product = ProductMapper.toEntity(request);
        Product savedProduct = productRepository.save(product);
        
        logger.info("Product created successfully with ID: {}", savedProduct.getId());
        
        return ProductMapper.toDto(savedProduct);
    }
    
    @Transactional
    public ProductDto updateProduct(Long id, UpdateProductRequest request) {
        if (logger.isDebugEnabled()) {
            logger.debug("Updating product ID: {}", id);
        }
        
        if (id == null || id <= 0) {
            throw new InvalidArgumentException("Invalid product ID: " + id);
        }
        
        validateUpdateProductRequest(request);
        
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        
        ProductMapper.updateEntity(product, request);
        Product updatedProduct = productRepository.save(product);
        
        logger.info("Product updated successfully with ID: {}", id);
        
        return ProductMapper.toDto(updatedProduct);
    }
    
    @Transactional
    public void deleteProduct(Long id) {
        if (logger.isDebugEnabled()) {
            logger.debug("Deleting product ID: {}", id);
        }
        
        if (id == null || id <= 0) {
            throw new InvalidArgumentException("Invalid product ID: " + id);
        }
        
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with ID: " + id);
        }
        
        productRepository.deleteById(id);
        logger.info("Product deleted successfully with ID: {}", id);
    }
    
    @Transactional
    public ProductDto updateStock(Long id, Integer quantity) {
        if (logger.isDebugEnabled()) {
            logger.debug("Updating stock for product ID: {}, quantity: {}", id, quantity);
        }
        
        if (id == null || id <= 0) {
            throw new InvalidArgumentException("Invalid product ID: " + id);
        }
        
        if (quantity == null) {
            throw new InvalidArgumentException("Quantity cannot be null");
        }
        
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        
        int newStock = product.getStock() + quantity;
        if (newStock < 0) {
            throw new InvalidArgumentException("Insufficient stock. Current stock: " + product.getStock() + ", requested: " + quantity);
        }
        
        product.setStock(newStock);
        Product updatedProduct = productRepository.save(product);
        
        logger.info("Stock updated successfully for product ID: {}, new stock: {}", id, newStock);
        
        return ProductMapper.toDto(updatedProduct);
    }
    
    // Validation methods
    private void validateCreateProductRequest(CreateProductRequest request) {
        if (request == null) {
            throw new InvalidArgumentException("Product request cannot be null");
        }
        
        if (!StringUtils.hasText(request.getName())) {
            throw new InvalidArgumentException("Product name cannot be null or empty");
        }
        
        if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidArgumentException("Product price must be greater than zero");
        }
        
        if (request.getStock() != null && request.getStock() < 0) {
            throw new InvalidArgumentException("Product stock cannot be negative");
        }
    }
    
    private void validateUpdateProductRequest(UpdateProductRequest request) {
        if (request == null) {
            throw new InvalidArgumentException("Product update request cannot be null");
        }
        
        if (request.getName() != null && !StringUtils.hasText(request.getName())) {
            throw new InvalidArgumentException("Product name cannot be empty");
        }
        
        if (request.getPrice() != null && request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidArgumentException("Product price must be greater than zero");
        }
        
        if (request.getStock() != null && request.getStock() < 0) {
            throw new InvalidArgumentException("Product stock cannot be negative");
        }
    }
}
