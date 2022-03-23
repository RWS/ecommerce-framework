package com.sdl.ecommerce.demandware.model;

import com.sdl.ecommerce.api.model.ProductPrice;

import java.text.DecimalFormat;
import java.util.Currency;

/**
 * Demandware Price
 *
 * @author nic
 */
public class DemandwarePrice implements ProductPrice {

    // TODO: This one should be used as a standard implementation of price

    private float price;
    private String currency;

    public DemandwarePrice(float price, String currency) {
        this.price = price;
        this.currency = currency;
    }

    @Override
    public String getCurrency() {
        return this.currency;
    }

    @Override
    public float getPrice() {
        return this.price;
    }

    @Override
    public String getFormattedPrice() {

        // TODO: Improve this implementation so so dot gets in the correct format for various countries etc
        //
        try {
            Currency currency = Currency.getInstance(this.currency);

            //DecimalFormat format = new DecimalFormat(currency.getSymbol() + "###.##");
            //return format.format(this.price);
            return String.format("%s%.2f", currency.getSymbol(), this.price);
        }
        catch ( Exception e ) {
            // In case the currency could not be find -> skip the currency in the formatted price
            //
            return  String.format("%.2f", this.price);
        }
    }

}
