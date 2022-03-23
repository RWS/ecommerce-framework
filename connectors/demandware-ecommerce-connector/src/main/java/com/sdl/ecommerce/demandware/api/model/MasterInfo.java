package com.sdl.ecommerce.demandware.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * MasterInfo
 *
 * @author nic
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MasterInfo {

   private String master_id;
   private boolean orderable;
   private float price;

    public String getMaster_id() {
        return master_id;
    }

    public boolean isOrderable() {
        return orderable;
    }

    public float getPrice() {
        return price;
    }
}
