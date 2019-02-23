package org.fkjava.identity.controller;

import org.fkjava.identity.UserHolder;
import org.fkjava.identity.domain.User;
import org.fkjava.identity.service.IdentityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/rest/identity/profile")
public class ProfileRestController {

    private final IdentityService identityService;

    @Autowired
    public ProfileRestController(IdentityService identityService) {
        this.identityService = identityService;
    }


    @GetMapping(produces = {"application/json", "application/xml", "text/xml"})
    @ResponseBody
    public User info() {
        User user = UserHolder.get();
        if (user != null) {
            String userId = user.getId();
            user = this.identityService.findUserById(userId);
        }
        return user;
    }
}
