package com.radyfy.common.controller.crm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.radyfy.common.model.crm.grid.GridRequestParams;
import com.radyfy.common.model.crm.page.EntityActionMeta;
import com.radyfy.common.service.crm.page.PageService;

@RestController
@RequestMapping(value = "/api/radyfy/entity")
public class EntityController {

    private static final Logger logger = LoggerFactory.getLogger(PageController.class);

    private final PageService pageService;

    @Autowired
    public EntityController(
            PageService pageService
    ){
        this.pageService = pageService;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    private void deleteEntity(
            @PathVariable("id") String entityId,
            @RequestParam(value = "search", required = false) String search
    ) {
        pageService.deleteEntity(new EntityActionMeta(entityId, GridRequestParams.fromSearch(search)));
    }

}
