package com.sdl.ecommerce.demandware.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

/**
 * Product
 *
 * @author nic
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Product extends StandardBody {

    private String id;
    private String name;
    private String brand;
    private boolean orderable;
    private float price;
    private String currency;
    private String primary_category_id;
    private String short_description;
    private String long_description;
    private List<ImageGroup> image_groups;
    private Map<String,String> variation_values;
    private List<ProductVariant> variants;
    private List<VariationAttribute> variation_attributes;
    private MasterInfo master;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public boolean isOrderable() {
        return orderable;
    }

    public void setOrderable(boolean orderable) {
        this.orderable = orderable;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPrimary_category_id() {
        return primary_category_id;
    }

    public void setPrimary_category_id(String primary_category_id) {
        this.primary_category_id = primary_category_id;
    }

    public String getShort_description() {
        return short_description;
    }

    public void setShort_description(String short_description) {
        this.short_description = short_description;
    }

    public String getLong_description() {
        return long_description;
    }

    public void setLong_description(String long_description) {
        this.long_description = long_description;
    }

    public List<ImageGroup> getImage_groups() {
        return image_groups;
    }

    public void setImage_groups(List<ImageGroup> image_groups) {
        this.image_groups = image_groups;
    }

    public Map<String, String> getVariation_values() {
        return variation_values;
    }

    public List<ProductVariant> getVariants() {
        return variants;
    }

    public List<VariationAttribute> getVariation_attributes() {
        return variation_attributes;
    }

    public MasterInfo getMaster() {
        return master;
    }
}
