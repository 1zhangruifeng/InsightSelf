package com.insightself.security;

import com.insightself.common.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {
    public AuthenticatedUser requireAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "authentication required");
        }
        return user;
    }

    public void requireUser(Long requestedUserId) {
        AuthenticatedUser user = requireAuthenticatedUser();
        if (requestedUserId == null || !requestedUserId.equals(user.userId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "forbidden");
        }
    }
}
