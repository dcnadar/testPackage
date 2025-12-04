package com.radyfy.common.controller;

import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.radyfy.common.commons.Api;
import com.radyfy.common.model.crm.grid.CrmForm;
import com.radyfy.common.model.dynamic.Option;
import com.radyfy.common.service.crm.CrmApiService;
import com.radyfy.common.service.crm.CrmSearchService;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(value = "/api/io")
public class CrmController {

    private final CrmSearchService crmSearchService;
    private final CrmApiService crmApiService;

    public CrmController(CrmSearchService crmSearchService, CrmApiService crmApiService) {
        this.crmSearchService = crmSearchService;
        this.crmApiService = crmApiService;
    }

    @RequestMapping(value = "/crm/search/model/{id}", method = RequestMethod.GET)
    private List<Option> searchModelData(HttpServletRequest httpServletRequest,
            @PathVariable("id") String modelId) {
        return crmSearchService.searchModelData(httpServletRequest, modelId);
    }

    @RequestMapping(value = {"/crm/inner-form/{id}"}, method = RequestMethod.GET)
    private CrmForm searchInnerCrmForm(HttpServletRequest httpServletRequest,
            @PathVariable("id") String formId) {
        return crmApiService.getInnerCrmForm(httpServletRequest, formId, false);
    }

    @Deprecated
    @RequestMapping(value = "/crm/slug/**", method = RequestMethod.GET)
    private Object slug(HttpServletRequest httpServletRequest) {
        String path = httpServletRequest.getRequestURI();
        String slug = path.substring(Api.crmSlugBaseURL.length());
        return crmApiService.getDataFromApi(slug, httpServletRequest);
    }

    @RequestMapping(value = "/crm/**", method = RequestMethod.GET)
    private Object crmApiGet(HttpServletRequest httpServletRequest) {
        String path = httpServletRequest.getRequestURI();
        String slug = path.substring(Api.crmBaseURL.length());
        return crmApiService.getDataFromApi(slug, httpServletRequest);
    }
    

    @RequestMapping(value = "/crm/**", method = RequestMethod.POST)
    private Object crmApiPOst(HttpServletRequest httpServletRequest, @RequestBody String body) {
        String path = httpServletRequest.getRequestURI();
        String slug = path.substring(Api.crmBaseURL.length());
        return crmApiService.postDataFromApi(slug, body, httpServletRequest);
    }


    @RequestMapping(value = "/crm/**", method = RequestMethod.PUT)
    private Object crmApiPut(HttpServletRequest httpServletRequest, @RequestBody String body) {
        String path = httpServletRequest.getRequestURI();
        String slug = path.substring(Api.crmBaseURL.length());
        return crmApiService.putDataFromApi(slug, body, httpServletRequest);
    }


    @RequestMapping(value = "/crm/**", method = RequestMethod.PATCH)
    private Object crmApiPatch(HttpServletRequest httpServletRequest, @RequestBody String body) {
        String path = httpServletRequest.getRequestURI();
        String slug = path.substring(Api.crmBaseURL.length());
        return crmApiService.patchDataFromApi(slug, body, httpServletRequest);
    }


    @RequestMapping(value = "/crm/**", method = RequestMethod.DELETE)
    private void crmApiDelete(HttpServletRequest httpServletRequest) {
        String path = httpServletRequest.getRequestURI();
        String slug = path.substring(Api.crmBaseURL.length());
        crmApiService.deleteDataFromApi(slug, httpServletRequest);
    }
}
