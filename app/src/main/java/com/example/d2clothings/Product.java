package com.example.d2clothings;

public class Product {
    private String id;
    private String name;
    private String description;
    private Long price;
    private int qty;
    private String imageUrl;

    // Constructor
    public Product(String id, String name, String description, Long price, int qty, String imageUrl) {
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
    public Long getPrice() { return price; }
    public int getQty() { return qty; }
    public String getImageUrl() { return imageUrl; }
}
