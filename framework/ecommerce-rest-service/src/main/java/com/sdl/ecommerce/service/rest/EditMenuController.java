package com.sdl.ecommerce.service.rest;

import com.sdl.ecommerce.api.ProductCategoryService;
import com.sdl.ecommerce.api.ProductQueryService;
import com.sdl.ecommerce.api.Query;
import com.sdl.ecommerce.api.edit.EditMenu;
import com.sdl.ecommerce.api.edit.EditService;
import com.sdl.ecommerce.service.model.ErrorMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ecommerce.svc/rest/v1/editmenu")
@Slf4j
public class EditMenuController {

    @Autowired(required = false)
    private EditService editService;

    @Autowired
    private ProductQueryService queryService;

    @Autowired
    private ProductCategoryService categoryService;

    @RequestMapping(value="/inContextMenuItems", method = RequestMethod.GET)
    public ResponseEntity getInContextMenuItems(@RequestParam(required = false) String categoryId, @RequestParam(required = false) String searchPhrase)
    {
        if ( this.editService == null ) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new ErrorMessage(""));
        }

        Query query = queryService.newQuery();
        if ( categoryId != null)
        {
            query.category(categoryService.getCategoryById(categoryId));
        }
        if ( searchPhrase != null)
        {
            query.searchPhrase(searchPhrase);
        }

        return new ResponseEntity<>(this.editService.getInContextMenuItems(query), HttpStatus.OK);
    }
}
