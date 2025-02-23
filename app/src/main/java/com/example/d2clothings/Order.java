package com.example.d2clothings;

import java.util.List;

public class Order {
    public Order() {

    }

    public Order(boolean isExpandable) {
        this.isExpandable = false;

    }

    private boolean isExpandable;

    public boolean isExpandable() {
        return isExpandable;
    }

    public void setExpandable(boolean expandable) {
        isExpandable = expandable;
    }

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    private String order_id;

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    private String uid;

    private String location;


}
