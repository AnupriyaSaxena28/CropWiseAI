package com.cropwise.backend.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

/** Helper to read the authenticated userId (set by JwtAuthFilter) inside controllers. */
public final class CurrentUser {
    private CurrentUser() {}

    public static String id() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null || "anonymousUser".equals(auth.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return auth.getPrincipal().toString();
    }
}
