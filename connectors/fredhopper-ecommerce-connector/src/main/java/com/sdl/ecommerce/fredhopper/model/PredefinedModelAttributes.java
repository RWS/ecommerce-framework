package com.sdl.ecommerce.fredhopper.model;

import java.util.Arrays;

/**
 * Predefined Model Attributes
 */
public enum PredefinedModelAttributes {
    masterId,
    variantId,
    name,
    description,
    price,
    thumbnailUrl,
    primaryImageUrl;

    static boolean isDefined(String attributeValue) {
        // As DXA 1.x us using Spring 3.1 we can't use JDK8 features.
        //
        //return Arrays.stream(PredefinedModelAttributes.values())
        //        .anyMatch(value -> value.name().equals(attributeValue));
        for ( PredefinedModelAttributes value : PredefinedModelAttributes.values() ) {
            if ( value.name().equals(attributeValue) ) {
                return true;
            }
        }
        return false;
    }
}

