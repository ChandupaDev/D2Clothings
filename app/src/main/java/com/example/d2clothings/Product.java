package com.example.d2clothings;

public class Product {
    private String id;
    private String title;
    private String description;
    private Long price;
    private String qty;
    private String imageUrl;

    public Product(String id, String title, String description, Long price, String qty, String imageUrl) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
        this.qty = qty;
        this.imageUrl = imageUrl;
    }


    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Long getPrice() {
        return price;
    }

    public String getQty() {
        return qty;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
