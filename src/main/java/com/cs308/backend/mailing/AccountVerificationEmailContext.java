package com.cs308.backend.mailing;

import com.cs308.backend.models.User;
import lombok.Getter;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;

@Getter
public class AccountVerificationEmailContext extends AbstractEmailContext {

    /**
     * -- GETTER --
     *  Getter for token (if needed).
     */
    private String token;

    /**
     * Initializes the email context using the given User object.
     * Combines the user's name and surname, and sets up email details.
     */
    @Override
    public <T> void init(T context) {
        User user = (User) context;
        // Combine first name and surname
        String fullName = user.getName() + " " + user.getSurname();
        put("fullName", fullName);
        // Set the location of the email template
        setTemplateLocation("emailVerification");
        // Set the email subject
        setSubject("Complete your registration");
        // Set the sender email address (customize to your domain)
        setFrom("no-reply@cs308.com");
        // Set the recipient email address
        setTo(user.getEmail());
    }

    /**
     * Sets the token and adds it to the email context.
     */
    public void setToken(String token) {
        this.token = token;
        put("token", token);
    }

    /**
     * Builds a verification URL using the base URL and token,
     * then adds the URL to the email context.
     */
    public void buildVerificationUrl(final String baseURL, final String token) {
        final String url = UriComponentsBuilder.fromHttpUrl(baseURL)
                .path("/account/verify")
                .queryParam("token", token)
                .toUriString();
        put("verificationURL", url);
    }

    /**
     * Adds the token expiration time to the email context.
     */
    public void setExpirationTime(LocalDateTime expirationTime) {
        put("expirationTime", expirationTime);
    }

}
