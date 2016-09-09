package com.sdl.ecommerce.dxa;

import com.sdl.ecommerce.api.LocalizationService;
import com.sdl.webapp.common.api.WebRequestContext;
import com.tridion.ambientdata.AmbientDataContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Map;

/**
 * Localization Service Implementation.
 * Hides the DXA localization from the E-Commerce connectors
 *
 * @author nic
 */
@Component
public class LocalizationServiceImpl implements LocalizationService {

    @Autowired
    private WebRequestContext webRequestContext;

    @Override
    public String getLocale() {
        return this.webRequestContext.getLocalization().getCulture();
    }

    @Override
    public String getLocalizedConfigProperty(String name) {
        return this.webRequestContext.getLocalization().getConfiguration("e-commerce." + name);
    }

    @Override
    public Map<URI, Object> getAllClaims() {
        return AmbientDataContext.getCurrentClaimStore().getAll();
    }
}
