package com.example.d2clothings;

public class Product {
    private String name, imageUrl, price;

    // Empty constructor for Firebase
    public Product() {}

    public Product(String name, String imageUrl, String price) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.price = price;
    }

    public String getName() { return name; }
    public String getImageUrl() { return imageUrl; }
    public String getPrice() { return price; }
}
