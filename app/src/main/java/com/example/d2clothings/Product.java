package com.example.d2clothings;

public class Product {
    private String id;
    private String name;
    private String description;
    private String price;
    private String qty;
    private String imageUrl;

    // Constructor
    public Product(String id, String name, String description, String price, String qty, String imageUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.qty = qty;
        this.imageUrl = imageUrl;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getPrice() { return price; }
    public String getQty() { return qty; }
    public String getImageUrl() { return imageUrl; }
}
