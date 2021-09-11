package org.lemon.product.location.service.impl;

import org.lemon.product.location.exception.BadRequestException;
import org.lemon.product.location.model.*;
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

  private double distance(double lat1, double long1, double lat2, double long2) {
    double phi1 = lat1 * Math.PI / 180;
    double phi2 = lat2 * Math.PI / 180;
    double delta = (long2 - long1) * Math.PI / 180;
    double r = 6371.00;
    return Math.acos(Math.sin(phi1) * Math.sin(phi2) +
            Math.cos(phi1) * Math.cos(phi2) * Math.cos(delta)) * r;
  }

  @Override
  public List<ProductDTO> getProductsNearestToLocation(String locationName) {
    //look up the location
    List<Location> locations = locationRepository.findByName(locationName);
    if (locations.isEmpty()) {
      throw new BadRequestException("Unable to find a location with that name");
    }
    Location location = locations.get(0);

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
            }).map(ProductMapper.INSTANCE::producttoDTO).limit(3).collect(Collectors.toList());
  }
}
