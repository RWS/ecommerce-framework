package com.sdl.ecommerce.hybris.model;

import com.sdl.ecommerce.api.model.QuerySuggestion;
import com.sdl.ecommerce.hybris.api.model.SearchResult;
import com.sdl.ecommerce.hybris.api.model.SpellingSuggestion;

import java.util.StringTokenizer;

/**
 * Hybris Query Suggestion
 *
 * @author nic
 */
public class HybrisQuerySuggestion implements QuerySuggestion {

    private String original;
    private String suggestion;

    public HybrisQuerySuggestion(SearchResult result) {
        this.original = result.getFreeTextSearch();
        this.suggestion = result.getSpellingSuggestion().getSuggestion();
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
    public Integer getEstimatedResults() {
        return null;
    }
}
