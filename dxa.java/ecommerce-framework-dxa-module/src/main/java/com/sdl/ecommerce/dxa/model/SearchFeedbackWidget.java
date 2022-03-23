package com.sdl.ecommerce.dxa.model;

import com.sdl.ecommerce.api.model.QuerySuggestion;
import com.sdl.webapp.common.api.localization.Localization;
import com.sdl.webapp.common.api.mapping.semantic.annotations.SemanticEntity;
import com.sdl.webapp.common.api.mapping.semantic.annotations.SemanticProperty;
import com.sdl.webapp.common.api.model.RichText;
import com.sdl.webapp.common.api.model.entity.AbstractEntityModel;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import static com.sdl.webapp.common.api.mapping.semantic.config.SemanticVocabulary.SDL_CORE;

/**
 * Search Feedback Widget
 *
 * @author nic
 */
@SemanticEntity(entityName = "SearchFeedback", vocabulary = SDL_CORE, prefix = "e", public_ = false)
public class SearchFeedbackWidget extends AbstractEntityModel {

    @SemanticProperty("e:spellCheckLabel")
    private RichText spellCheckLabel;

    private List<QuerySuggestion> querySuggestions;

    public List<QuerySuggestion> getQuerySuggestions() {
        return querySuggestions;
    }

    public void setQuerySuggestions(List<QuerySuggestion> querySuggestions) {
        this.querySuggestions = querySuggestions;
    }

    public RichText getSpellCheckLabel() {
        return spellCheckLabel;
    }

    public String getSuggestionUrl(QuerySuggestion suggestion, Localization localization) {
        try {
            return localization.localizePath("/search") + "/" + URLEncoder.encode(suggestion.getSuggestion(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return localization.localizePath("/search") + "/" + suggestion.getSuggestion();
        }
    }

}
