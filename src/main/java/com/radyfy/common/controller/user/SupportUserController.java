package com.radyfy.common.controller.user;

import java.util.Objects;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.radyfy.common.commons.Constants;
import com.radyfy.common.service.CurrentUserSession;
import com.radyfy.common.service.UserService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/radyfy/public/user/support")
public class SupportUserController {

    @Value("${radyfy.support.api_key}")
    private String RADYFY_SUPPORT_API_KEY;

    private UserService userService;
    CurrentUserSession currentUserSession;

    public SupportUserController(UserService userService, CurrentUserSession currentUserSession) {
        this.userService = userService;
        this.currentUserSession = currentUserSession;
    }

    @PostMapping
    public ResponseEntity<String> createUser(@RequestBody Document user,
            @RequestHeader(Constants.RADYFY_SUPPORT_API_KEY_HEADER) String apiKey) {
        if (Objects.equals(apiKey, RADYFY_SUPPORT_API_KEY)) {
            currentUserSession.getRequestSession().setRadyfySupport(true);
            userService.createUser(user);
            return ResponseEntity.ok("User Created");
        }
        log.info("User Creation Failed");
        return ResponseEntity.badRequest().body("User Creation Failed");
    }

    @PostMapping("/invitation")
    public ResponseEntity<String> inviteUser(@RequestParam String email,
            @RequestHeader(Constants.RADYFY_SUPPORT_API_KEY_HEADER) String apiKey) {
        if (Objects.equals(apiKey, RADYFY_SUPPORT_API_KEY)) {
            ResponseEntity<String> sendPasswordEmail = userService.sendPasswordEmail(email);
            return sendPasswordEmail;
        }
        return ResponseEntity.badRequest().body("Invitation Failed");
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteUser(@RequestParam String email,
            @RequestHeader(Constants.RADYFY_SUPPORT_API_KEY_HEADER) String apiKey) {
        if (Objects.equals(apiKey, RADYFY_SUPPORT_API_KEY)) {
            userService.deleteUser(email);
            return ResponseEntity.ok("User Deleted Successfully");
        }
        return ResponseEntity.badRequest().body("User Deletion Failed");
    }
}
