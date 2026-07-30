package com.mediq.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

public class SecurityUtils {

    private SecurityUtils() {
        // Private constructor for utility class
    }

    public static Optional<String> getCurrentUserMemberId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return Optional.of(userDetails.getUsername());
        } else if (principal instanceof String principalString && !principalString.equals("anonymousUser")) {
            return Optional.of(principalString);
        }

        return Optional.empty();
    }
}
