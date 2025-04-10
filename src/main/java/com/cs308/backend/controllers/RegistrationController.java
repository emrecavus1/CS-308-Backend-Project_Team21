package com.cs308.backend.controllers;

import com.cs308.backend.services.UserService;
import com.cs308.backend.exception.InvalidTokenException;
import com.cs308.backend.exception.UnknownIdentifierException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email")
public class RegistrationController {

    private final UserService userService;

    @Autowired
    public RegistrationController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/verify")
    public String verifyUser(@RequestParam("token") String token) {
        if (token == null || token.trim().isEmpty()) {
            // Redirect to login with an error message
            return "redirect:/login?message=Invalid+verification+token";
        }
        try {
            userService.verifyUser(token);
            // On success, redirect to login with a success message.
            return "redirect:/login?message=Account+verified+successfully";
        } catch (UnknownIdentifierException | InvalidTokenException | IllegalArgumentException e) {
            // You can log the exception for further analysis
            e.printStackTrace();
            // On error, redirect to login with a failure message.
            return "redirect:/login?message=Verification+failed";
        } catch (Exception e) {
            // Catch any other unexpected exceptions.
            e.printStackTrace();
            return "redirect:/login?message=An+unexpected+error+occurred";
        }
    }
}
