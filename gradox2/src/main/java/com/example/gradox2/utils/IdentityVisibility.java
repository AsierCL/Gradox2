package com.example.gradox2.utils;

import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.persistence.entities.enums.FileVisibility;
import com.example.gradox2.persistence.entities.enums.UserRole;

public final class IdentityVisibility {

    private IdentityVisibility() {
    }

    public static String resolveDisplayUsername(User owner, User viewer, FileVisibility visibility) {
        if (owner == null) {
            return null;
        }

        if (isSameUser(owner, viewer)) {
            return owner.getUsername();
        }

        if (isMaster(viewer)) {
            return owner.getUsername();
        }

        return switch (visibility) {
            case PUBLIC -> owner.getUsername();
            case RESTRICTED -> (viewer != null && viewer.getRole() == UserRole.USER)
                    ? owner.getUsername()
                    : "anonymous";
            case PRIVATE -> "anonymous";
        };
    }

    public static boolean isMaster(User user) {
        return user != null && user.getRole() == UserRole.MASTER;
    }

    public static boolean isSameUser(User left, User right) {
        return left != null && right != null && left.getId() != null && left.getId().equals(right.getId());
    }
}