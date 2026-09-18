package com.salesportal.security;

import com.salesportal.entity.Classification;
import com.salesportal.entity.DocumentEntity;
import com.salesportal.entity.Role;
import com.salesportal.entity.User;
import org.springframework.stereotype.Component;

@Component("abacEvaluator")
public class AbacEvaluator {

    /**
     * ABAC Evaluation Rule:
     * 1. ADMIN can access any document.
     * 2. Owner can access their own document.
     * 3. Users in the same department can access INTERNAL or PUBLIC documents.
     * 4. CONFIDENTIAL documents can ONLY be accessed by the Owner or ADMIN.
     */
    public boolean canAccessDocument(User user, DocumentEntity document) {
        if (user == null || document == null) {
            return false;
        }

        // Rule 1: Admin override
        if (user.getRole() == Role.ROLE_ADMIN) {
            return true;
        }

        // Rule 2: Owner check (Direct BOLA prevention)
        if (document.getOwner() != null && document.getOwner().getId().equals(user.getId())) {
            return true;
        }

        // Rule 3: Department level check + Classification filter
        if (document.getDepartment() == user.getDepartment()) {
            return document.getClassification() != Classification.CONFIDENTIAL;
        }

        return false;
    }

    public boolean canDeleteDocument(User user, DocumentEntity document) {
        if (user == null || document == null) {
            return false;
        }
        // BFLA Prevention: Only ADMIN or Owner can delete documents
        return user.getRole() == Role.ROLE_ADMIN ||
                (document.getOwner() != null && document.getOwner().getId().equals(user.getId()));
    }
}
