package com.radyfy.common.model.commons;

public class ProductCategory {

    private String categoryId;
    private Type type;
    public ProductCategory() {
        super();
    }

    public ProductCategory(String categoryId, Type type) {
        super();
        this.categoryId = categoryId;
        this.type = type;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public enum Type {
        BRAND, CAST, VEHICLE_PART
    }
}
