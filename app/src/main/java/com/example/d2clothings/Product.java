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
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }
    public String getQty() { return qty; }
    public void setQty(String qty) { this.qty = qty; }
}
