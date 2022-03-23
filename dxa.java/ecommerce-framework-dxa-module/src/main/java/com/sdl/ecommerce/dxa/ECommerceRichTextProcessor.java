package com.sdl.ecommerce.dxa;

import com.sdl.ecommerce.api.ECommerceLinkResolver;
import com.sdl.ecommerce.api.ProductCategoryService;
import com.sdl.ecommerce.api.ProductDetailResult;
import com.sdl.ecommerce.api.ProductDetailService;
import com.sdl.ecommerce.api.model.Category;
import com.sdl.webapp.common.api.localization.Localization;
import com.sdl.webapp.common.api.model.RichText;
import com.sdl.webapp.tridion.mapping.DefaultRichTextProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Primary
@Component
public class ECommerceRichTextProcessor extends DefaultRichTextProcessor {

    private static Pattern PRODUCT_URI_REGEX = Pattern.compile("ecom:product:([^:]+):uri");
    private static Pattern CATEGORY_URI_REGEX = Pattern.compile("ecom:category:([^:]+):uri");

    @Autowired
    private ProductCategoryService categoryService;

    @Autowired
    private ProductDetailService productDetailService;

    @Autowired
    private ECommerceLinkResolver linkResolver;

    @Override
    public RichText processRichText(String xhtml, Localization localization) {

        xhtml = processCategoryLinks(xhtml);
        xhtml = processProductLinks(xhtml);
        return super.processRichText(xhtml, localization);
    }

    private String processCategoryLinks(String xhtml) {
        Matcher matcher = CATEGORY_URI_REGEX.matcher(xhtml);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String categoryId = matcher.group(1);
            Category category = categoryService.getCategoryById(categoryId);
            if ( category != null ) {
                matcher.appendReplacement(sb, linkResolver.getCategoryLink(category));
            }
        }
        if ( sb.length() > 0 ) {
            matcher.appendTail(sb);
            xhtml = sb.toString();
        }
        return xhtml;
    }

    private String processProductLinks(String xhtml) {
        Matcher matcher = PRODUCT_URI_REGEX.matcher(xhtml);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String productId = matcher.group(1);
            ProductDetailResult result = productDetailService.getDetail(productId);
            if ( result != null && result.getProductDetail() != null ) {
                matcher.appendReplacement(sb, linkResolver.getProductDetailLink(result.getProductDetail()));
            }
        }
        if ( sb.length() > 0 ) {
            matcher.appendTail(sb);
            xhtml = sb.toString();
        }

        return xhtml;
    }
}
