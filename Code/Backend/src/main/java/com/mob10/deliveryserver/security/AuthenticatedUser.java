package com.mob10.deliveryserver.security;

import com.mob10.deliveryserver.domain.Role;

public record AuthenticatedUser(Long id, String username, Role role) {}
