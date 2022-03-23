package com.sdl.ecommerce.fredhopper.model;

import com.sdl.ecommerce.api.model.QuerySuggestion;

/**
 * Fredhopper Query Suggestion
 *
 * @author nic
 */
public class FredhopperQuerySuggestion implements QuerySuggestion {

    private com.fredhopper.webservice.client.QuerySuggestion querySuggestion;

    public FredhopperQuerySuggestion(com.fredhopper.webservice.client.QuerySuggestion querySuggestion) {
        this.querySuggestion = querySuggestion;
    }

    @Override
    public String getOriginal() {
        return this.querySuggestion.getOriginal();
    }

    @Override
    public String getSuggestion() {
        return this.querySuggestion.getValue().getValue();
    }

    @Override
    public Integer getEstimatedResults() {
        return this.querySuggestion.getEstimatedResults();
    }
}
