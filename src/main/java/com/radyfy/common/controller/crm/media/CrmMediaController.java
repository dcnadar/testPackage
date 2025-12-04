package com.radyfy.common.controller.crm.media;

import org.bson.Document;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.radyfy.common.model.crm.grid.table.CrmTable;
import com.radyfy.common.request.CrmMediaCreateRequest;
import com.radyfy.common.request.table.TableRequest;
import com.radyfy.common.service.crm.media.CrmMediaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/api/radyfy/media")
public class CrmMediaController {

  private final CrmMediaService crmMediaService;

  public CrmMediaController(CrmMediaService crmMediaService) {
    this.crmMediaService = crmMediaService;
  }

  @GetMapping()
  public CrmTable getMedia(@RequestParam(value = "payload", required = false) String payload) {
    return crmMediaService.getMedia(TableRequest.fromPayload(payload));
  }

  @PostMapping()
  public Document createMedia(@Valid @RequestBody CrmMediaCreateRequest media) {
    return crmMediaService.createMedia(media);
  }
}
