package org.lemon.product.location.service.impl;

import org.lemon.product.location.exception.BadRequestException;
import org.lemon.product.location.model.Location;
import org.lemon.product.location.model.Product;
import org.lemon.product.location.model.LocationRepository;
import org.lemon.product.location.model.ProductRepository;
import org.lemon.product.location.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
class DefaultProductService implements ProductService {
  private final ProductRepository productRepository;

  @Autowired
  LocationRepository locationRepository;

  @Autowired
  DefaultProductService(ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  @Override
  public List<Product> getAllProducts() {
    return productRepository.findAll();
  }

  @Override
  public List<Product> getProductsByLocation(Long locationId) {
    return productRepository.findAll().stream()
        .filter((product) -> locationId.equals(product.getLocation().getId()))
        .collect(Collectors.toList());
  }

  @Override
  public Product createNewProduct(Product product) {
    if (product.getId() != null) {
      throw new BadRequestException("The ID must not be provided when creating a new Product");
    }

    return productRepository.save(product);
  }

  @Override
  public Product getProductById(Long productId) {
    return productRepository.findById(productId)
            .filter(product -> !product.isDeleted())
            .orElseThrow(() -> new BadRequestException("Unable to find a product with id:" + productId));
  }

  @Override
  public void deleteProductLogicallyById(Long productId) {
    Optional<Product> productToDelete = productRepository.findById(productId);
    productToDelete.ifPresent(product -> {
      product.setDeleted(true);
      productRepository.save(product);
    });
  }

  private Double distance(Double lat1, Double long1, Double lat2, Double long2) {
    Double phi1 = lat1 * Math.PI / 180;
    Double phi2 = lat2 * Math.PI / 180;
    Double delta = (long2 - long1) * Math.PI / 180;
    Double r = 6371.00;
    return Math.acos(Math.sin(phi1) * Math.sin(phi2) +
            Math.cos(phi1) * Math.cos(phi2) * Math.cos(delta)) * r;
  }

  @Override
  public List<Product> getProductsNearestToLocation(String locationName) {
    //look up the location
    List<Location> cities = locationRepository.findByName(locationName);
    if (cities.isEmpty()) {
      throw new BadRequestException("Unable to find a location with that name");
    }
    Location location = cities.get(0);

    return productRepository.findAll().stream().filter(product -> !product.isDeleted())
            .filter(product -> product.getLocation().getName().equalsIgnoreCase(locationName))
            .sorted((h1, h2) -> {
              Double h1dist =
                      distance(h1.getLatitude(),
                              h1.getLongitude(),
                              location.getLatitude(),
                              location.getLongitude());
              Double h2dist =
                      distance(h2.getLatitude(),
                              h2.getLongitude(),
                              location.getLatitude(),
                              location.getLongitude());
              return h1dist.compareTo(h2dist);
            }).limit(3).collect(Collectors.toList());
  }
}
