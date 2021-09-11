package org.lemon.product.location.service;

import org.lemon.product.location.model.Product;
import org.lemon.product.location.model.ProductDTO;

import java.util.List;

public interface ProductService {
  List<Product> getAllProducts();

  List<Product> getProductsByLocation(Long locationId);

  Product createNewProduct(Product product);

  Product getProductById(Long productId);

  void deleteProductLogicallyById(Long productId);

  List<ProductDTO> getProductsNearestToLocation(String locationName);
}
