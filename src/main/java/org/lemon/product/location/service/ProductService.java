package org.lemon.product.location.service;

import org.lemon.product.location.model.Product;

import java.util.List;

public interface ProductService {
  List<Product> getAllProducts();

  List<Product> getProductsByLocation(Long locationId);

  Product createNewProduct(Product product);

  Product getProductById(Long productId);

  void deleteProductLogicallyById(Long productId);

  List<Product> getProductsNearestToLocation(String locationName);
}
