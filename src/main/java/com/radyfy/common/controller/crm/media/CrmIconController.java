package com.radyfy.common.controller.crm.media;

import org.bson.Document;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.radyfy.common.model.crm.grid.table.CrmTable;
import com.radyfy.common.request.CrmIconRequest;
import com.radyfy.common.request.table.TableRequest;
import com.radyfy.common.service.crm.media.CrmIconService;
import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/api/radyfy/icon")
public class CrmIconController {

    private final CrmIconService crmIconService;

    public CrmIconController(CrmIconService crmIconService) {
        this.crmIconService = crmIconService;
    }


    @GetMapping()
    public CrmTable getIcons(@RequestParam(value = "payload", required = false) String payload) {

        return crmIconService.getIcons(TableRequest.fromPayload(payload));
    }

    @PostMapping()
    public Document createIcon(@Valid @RequestBody CrmIconRequest iconRequest) {
        return crmIconService.createIcon(iconRequest);
    }

}
