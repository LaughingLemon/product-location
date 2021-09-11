package org.lemon.product.location.controller;

import org.lemon.product.location.model.Location;
import org.lemon.product.location.model.Product;
import org.lemon.product.location.model.LocationRepository;
import org.lemon.product.location.model.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureMockMvc
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:data.sql")
class ProductControllerTest {
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper mapper;

  @Autowired private ProductRepository repository;
  @Autowired private LocationRepository locationRepository;

  @Test
  @DisplayName("When all products are requested then they are all returned")
  void allProductsRequested() throws Exception {
    mockMvc
            .perform(get("/product"))
            .andExpect(status().is2xxSuccessful())
            .andExpect(jsonPath("$", hasSize((int) repository.count())));
  }

  @Test
  @DisplayName("Return a specific product by it's internal id")
  void getProductById() throws Exception {
    mockMvc
            .perform(get("/product/6"))
            .andExpect(status().is2xxSuccessful())
            .andExpect(jsonPath("$.name").value("Raymond of Amsterdam Hotel"))
            .andExpect(jsonPath("$.location.name").value("Amsterdam"));
  }

  @Test
  @DisplayName("Remove a product using it's internal id")
  void deleteProductById() throws Exception {
    mockMvc
            .perform(delete("/product/6"))
            .andExpect(status().is2xxSuccessful());
    Optional<Product> productDeleted = repository.findById(5L);
    productDeleted.ifPresent(product -> {
      assertTrue(product.isDeleted());
      product.setDeleted(false);
      repository.save(product);
    });
  }

  @Test
  @DisplayName("Find nearest three products to location center")
  void nearestProductsToLocationCenter() throws Exception {
    mockMvc
            .perform(get("/product/nearestToLocation?location=Amsterdam"))
            .andExpect(status().is2xxSuccessful())
            .andExpect(jsonPath("$", hasSize(3)));
  }

  @Test
  @DisplayName("When a product creation is requested then it is persisted")
  void productCreatedCorrectly() throws Exception {
    Location location =
        locationRepository
            .findById(1L)
            .orElseThrow(
                () -> new IllegalStateException("Test dataset does not contain a location with ID 1!"));
    Product newProduct = Product.builder().name("This is a test product").location(location).build();

    Long newProductId =
        mapper
            .readValue(
                mockMvc
                    .perform(
                        post("/product")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(newProduct)))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString(),
                Product.class)
            .getId();

    newProduct.setId(newProductId); // Populate the ID of the product after successful creation

    assertThat(
        repository
            .findById(newProductId)
            .orElseThrow(
                () -> new IllegalStateException("New Product has not been saved in the repository")),
        equalTo(newProduct));
  }
}
