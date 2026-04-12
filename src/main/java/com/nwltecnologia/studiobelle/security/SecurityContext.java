package com.nwltecnologia.studiobelle.security;

public final class SecurityContext {

    private static final ThreadLocal<AuthenticatedUser> AUTH_USER = new ThreadLocal<>();

    private SecurityContext() {
    }

    public static void set(AuthenticatedUser user) {
        AUTH_USER.set(user);
    }

    public static AuthenticatedUser get() {
        return AUTH_USER.get();
    }

    public static void clear() {
        AUTH_USER.remove();
    }
}
