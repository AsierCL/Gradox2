package com.example.gradox2.utils;

import com.example.gradox2.persistence.entities.File;
import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.persistence.entities.enums.FileVisibility;
import com.example.gradox2.persistence.entities.enums.UserRole;

public final class IdentityVisibility {

    private IdentityVisibility() {
    }

    public static boolean canViewContent(File file, User viewer) {
        FileVisibility visibility = file.getVisibilityLevel();
        if (visibility == null) {
            return false;
        }
        return switch (visibility) {
            case PUBLIC -> true;
            case RESTRICTED -> viewer != null;
            case PRIVATE -> isSameUser(file.getUploader(), viewer) || isMaster(viewer);
        };
    }

    public static String resolveDisplayUsername(User owner, User viewer, FileVisibility visibility) {
        if (owner == null) {
            return null;
        }

        if (isSameUser(owner, viewer)) {
            return owner.getUsername();
        }

        return switch (visibility) {
            case PUBLIC -> owner.getUsername();
            case RESTRICTED -> viewer != null ? owner.getUsername() : "anonymous";
            case PRIVATE -> isMaster(viewer) ? owner.getUsername() : "anonymous";
        };
    }

    public static boolean isMaster(User user) {
        return user != null && user.getRole() == UserRole.MASTER;
    }

    public static boolean isSameUser(User left, User right) {
        return left != null && right != null && left.getId() != null && left.getId().equals(right.getId());
    }
}
