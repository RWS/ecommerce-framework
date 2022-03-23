package com.sdl.ecommerce.odata.model;

import com.sdl.ecommerce.api.model.QuerySuggestion;
import com.sdl.odata.api.edm.annotations.EdmComplex;
import com.sdl.odata.api.edm.annotations.EdmProperty;

/**
 * ODataQuerySuggestion
 *
 * @author nic
 */
@EdmComplex(name="QuerySuggestion", namespace = "SDL.ECommerce")
public class ODataQuerySuggestion implements QuerySuggestion {

    @EdmProperty
    private String original;

    @EdmProperty
    private String suggestion;

    @EdmProperty
    private Integer estimatedResults;

    public ODataQuerySuggestion() {}
    public ODataQuerySuggestion(QuerySuggestion querySuggestion) {
        this.original = querySuggestion.getOriginal();
        this.suggestion = querySuggestion.getSuggestion();
        this.estimatedResults = querySuggestion.getEstimatedResults();
    }

    @Override
    public String getOriginal() {
        return this.original;
    }

    @Override
    public String getSuggestion() {
        return this.suggestion;
    }

    @Override
    public Integer getEstimatedResults() { return this.estimatedResults; }
}
