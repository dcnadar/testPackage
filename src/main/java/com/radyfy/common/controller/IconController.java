package com.radyfy.common.controller;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.radyfy.common.service.CurrentUserSession;
import com.radyfy.common.service.filestore.FileStorageService;
import com.radyfy.common.utils.Utils;

@RestController
public class IconController {
  private static final Logger logger = LoggerFactory.getLogger(IconController.class);

  private final CurrentUserSession currentUserSession;
  private final FileStorageService fileStorageService;

  // @Autowired
  // public WebsitePageController(WebsitePageService websitePageService){
  // this.websitePageService = websitePageService;
  // }

  @Autowired
  public IconController(CurrentUserSession currentUserSession, FileStorageService fileStorageService) {
    this.currentUserSession = currentUserSession;
    this.fileStorageService = fileStorageService;
  }

  @GetMapping("/favicon.ico")
  public ResponseEntity<byte[]> favicon(
      HttpServletRequest httpServletRequest) {
    if (currentUserSession.getApp() != null && currentUserSession.getApp().getSeo() != null) {
      String fileName = currentUserSession.getApp().getSeo().getString("favicon");
      if (!Utils.isNotEmpty(fileName)) {
        return ResponseEntity.notFound().build();
      }
      // removing this from fileName "/api/public/downloadFile
      if(fileName.startsWith("/api/public/downloadFile/")){
        fileName = fileName.substring("/api/public/downloadFile/".length());
      }

      byte[] fileBytes = fileStorageService.downloadS3File(fileName);
      
      // Check if file was found
      if (fileBytes == null || fileBytes.length == 0) {
        logger.warn("Favicon file not found: {}", fileName);
        return ResponseEntity.notFound().build();
      }

      // create response entity object with file bytes and headers
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(Utils.getMediaType(fileName));
      headers.setContentLength(fileBytes.length);
      headers.setCacheControl("max-age=31536000");

      return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);
    }
    return ResponseEntity.notFound().build();
  }

}
