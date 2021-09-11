package org.lemon.product.location.controller;

import org.lemon.product.location.model.Product;
import org.lemon.product.location.model.ProductDTO;
import org.lemon.product.location.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product")
public class ProductController {
  private final ProductService productService;

  @Autowired
  public ProductController(ProductService productService) {
    this.productService = productService;
  }

  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  public List<Product> getAllProducts() {
    return productService.getAllProducts();
  }

  @GetMapping("/{id}")
  @ResponseStatus(HttpStatus.OK)
  public Product getProductById(@PathVariable(name = "id") Long productId) {
    return productService.getProductById(productId);
  }

  @GetMapping("/nearestToLocation")
  @ResponseStatus(HttpStatus.OK)
  public List<ProductDTO> getProductsNearestToLocation(@RequestParam(name = "location") String locationName) {
    return productService.getProductsNearestToLocation(locationName);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.OK)
  public void deleteProductById(@PathVariable(name = "id") Long productId) {
    productService.deleteProductLogicallyById(productId);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Product createProduct(@RequestBody Product product) {
    return productService.createNewProduct(product);
  }
}
