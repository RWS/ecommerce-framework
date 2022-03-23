package com.sdl.ecommerce.service.rest;

import com.sdl.ecommerce.api.ECommerceException;
import com.sdl.ecommerce.api.ProductCategoryService;
import com.sdl.ecommerce.api.model.Category;
import com.sdl.ecommerce.service.model.ErrorMessage;
import com.sdl.ecommerce.service.model.RestCategory;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * Category Controller
 *
 * @author nic
 */
@RestController
@RequestMapping("/ecommerce.svc/rest/v1/category")
@Slf4j
public class CategoryController {

    private static final Logger LOG = LoggerFactory.getLogger(CategoryController.class);

    @Autowired
    private ProductCategoryService categoryService;

    @RequestMapping(value="/", method = RequestMethod.GET)
    public ResponseEntity list() throws ECommerceException {
        log.debug("Get all categories...");

        List<Category> topLevelCategories = this.categoryService.getTopLevelCategories();
        return toRest(topLevelCategories);
    }

    @RequestMapping(value="/{id}", method = RequestMethod.GET)
    public ResponseEntity listById(@PathVariable String id, @RequestParam(required = false) boolean single) throws ECommerceException {
        Category category = this.categoryService.getCategoryById(id);
        if ( category == null ) {
            // Try by path
            //
            category = this.categoryService.getCategoryByPath(id);
        }
        if ( category == null ) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorMessage("Category with ID/Path '" + id + "' was not found."));
        }
        return toRest(category, single);
    }

    @RequestMapping(value="/**", method = RequestMethod.GET)
    public ResponseEntity listByPath(HttpServletRequest request, @RequestParam(required = false) boolean single) throws ECommerceException {
        String path = new AntPathMatcher()
                .extractPathWithinPattern( "category/v1/**", request.getRequestURI() ).replace("v1/", "/");
        Category category = this.categoryService.getCategoryByPath(path);
        if ( category == null ) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorMessage("Category with Path '" + path + "' was not found."));
        }
        return toRest(category, single);
    }

    private ResponseEntity toRest(Category category, boolean single) {
        if ( single ) {
            return toRest(category);
        }
        else {
            return toRest(category.getCategories());
        }
    }

    private ResponseEntity<Category> toRest(Category category) {
        return new ResponseEntity(new RestCategory(category), HttpStatus.OK);
    }

    private ResponseEntity<List<Category>> toRest(List<Category> categories) {
        List<Category> responseList = new ArrayList<>();
        categories.forEach(category ->  responseList.add(new RestCategory(category)));
        return new ResponseEntity(responseList, HttpStatus.OK);
    }

    @ExceptionHandler({ ECommerceException.class })
    public final ResponseEntity<String> handleAllExceptions(Exception ex) {
        LOG.error("Exception when querying categories", ex);
        return new ResponseEntity(new ErrorMessage(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
