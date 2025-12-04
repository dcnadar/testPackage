package com.radyfy.common.controller.crm;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.radyfy.common.model.crm.grid.GridRequestParams;
import com.radyfy.common.model.crm.page.FormSaveMeta;
import com.radyfy.common.model.crm.page.GetPageMeta;
import com.radyfy.common.service.CurrentUserSession;
import com.radyfy.common.service.common.MemoryService;
import com.radyfy.common.service.crm.page.PageService;

@RestController
@RequestMapping(value = "/api/radyfy/page")
public class PageController {

    private static final Logger logger = LoggerFactory.getLogger(PageController.class);
    private final PageService pageService;
    private final CurrentUserSession currentUserSession;
    private final MemoryService memoryService;


    @Autowired
    public PageController(PageService pageService, CurrentUserSession currentUserSession,MemoryService memoryService) {
        this.pageService = pageService;
        this.currentUserSession=currentUserSession;
        this.memoryService = memoryService;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    private Object getPageWithData(@PathVariable("id") String pageId,
            @RequestParam(value = "parentSlug", required = false) String parentSlug,
            @RequestParam(value = "slug", required = false) String slug,
            @RequestParam(value = "breadcrumb", required = false) String breadcrumb,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "payload", required = false) String payload,
            @RequestParam(value = "preview", required = false, defaultValue = "false") boolean preview
    ) {
        currentUserSession.getRequestSession().setPreviewData(preview);
        
        return pageService.getPageWithData(new GetPageMeta(pageId, parentSlug, slug, breadcrumb,
                GridRequestParams.fromSearch(search), payload));
    }


    @RequestMapping(value = "/{id}", method = RequestMethod.POST)
    private Object postPageWithData(@PathVariable("id") String pageId,
            @RequestParam(value = "slug") String slug,
            @RequestParam(value = "search", required = false) String search,
            @RequestBody Document document) {
                Object response =  pageService
                .postPage(new FormSaveMeta(pageId, document, GridRequestParams.fromSearch(search)));
                memoryService.invalidateAll();
return response;
    }


    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    private Object putPageWithData(@PathVariable("id") String pageId,
            @RequestParam(value = "slug") String slug,
            @RequestParam(value = "search", required = false) String search,
            @RequestBody Document document) {
                Object response = pageService
                .putPage(new FormSaveMeta(pageId, document, GridRequestParams.fromSearch(search)));
                memoryService.invalidateAll();
                return response;
    }


    @RequestMapping(value = "/{id}", method = RequestMethod.PATCH)
    private Object patchPageWithData(@PathVariable("id") String pageId,
            @RequestParam(value = "slug") String slug,
            @RequestParam(value = "search", required = false) String search,
            @RequestBody Document document) {
                Object response =  pageService.patchPage(
                new FormSaveMeta(pageId, document, GridRequestParams.fromSearch(search)));
                memoryService.invalidateAll();;
                return response;
    }


    @RequestMapping(value = "/{id}/params", method = RequestMethod.GET)
    private Object getPageParams(@PathVariable("id") String pageId) {
        return pageService.getPageParams(pageId);
    }


}
