package org.lemon.product.location.model;

import lombok.Data;

@Data
public class ProductDTO {
    private String name;
    private String address;
    private double latitude;
    private double longitude;
}
