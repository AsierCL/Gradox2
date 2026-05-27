package com.example.gradox2.utils;

import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.persistence.entities.enums.UserRole;

public final class IdentityVisibility {

    private IdentityVisibility() {
    }

    public static String resolveDisplayUsername(User owner, User viewer, boolean anonymous) {
        if (owner == null) {
            return null;
        }

        if (viewer == null) {
            return "anonymous";
        }

        if (isMaster(viewer) || isSameUser(owner, viewer)) {
            return owner.getUsername();
        }

        return anonymous ? "anonymous" : owner.getUsername();
    }

    public static boolean isMaster(User user) {
        return user != null && user.getRole() == UserRole.MASTER;
    }

    public static boolean isSameUser(User left, User right) {
        return left != null && right != null && left.getId() != null && left.getId().equals(right.getId());
    }
}