package org.tkit.onecx.chat.bff.domain.exception;

import org.openapi.quarkus.onecx_user_profile_svc_v1_yaml.model.UserProfileAbstractCriteria;

public class NoUserProfileException extends RuntimeException {
    public NoUserProfileException(String message) {
        super(message);
    }

    public NoUserProfileException(UserProfileAbstractCriteria criteria) {
        super(buildMessageFromCriteria(criteria));
    }

    private static String buildMessageFromCriteria(UserProfileAbstractCriteria criteria) {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("Cannot find profile with criteria: ");
        if (!criteria.getUserIds().isEmpty()) {
            messageBuilder.append(String.join(", ", criteria.getUserIds()));
        }
        if (!criteria.getEmailAddresses().isEmpty()) {
            messageBuilder.append(String.join(", ", criteria.getEmailAddresses()));
        }
        if (!criteria.getDisplayNames().isEmpty()) {
            messageBuilder.append(String.join(", ", criteria.getDisplayNames()));
        }
        return messageBuilder.toString();
    }
}
