package org.tkit.onecx.chat.bff.domain.exception;

import java.util.Objects;

import org.openapi.quarkus.onecx_user_profile_svc_v1_yaml.model.UserProfileAbstractCriteria;
import org.tkit.onecx.chat.bff.domain.model.ErrorCodeEnum;

public class NoUserProfileException extends RuntimeException {

    public final ErrorCodeEnum errorCode = ErrorCodeEnum.NO_PROFILE_FOUND;

    public NoUserProfileException(String message) {
        super(message);
    }

    public NoUserProfileException(UserProfileAbstractCriteria criteria) {
        super(buildMessageFromCriteria(criteria));
    }

    private static String buildMessageFromCriteria(UserProfileAbstractCriteria criteria) {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("Cannot find profile with criteria: ");
        if (Objects.nonNull(criteria.getUserIds()) && !criteria.getUserIds().isEmpty()) {
            messageBuilder.append(String.join(", ", criteria.getUserIds()));
            messageBuilder.append(", ");
        }
        if (Objects.nonNull(criteria.getEmailAddresses()) && !criteria.getEmailAddresses().isEmpty()) {
            messageBuilder.append(String.join(", ", criteria.getEmailAddresses()));
            messageBuilder.append(", ");
        }
        if (Objects.nonNull(criteria.getDisplayNames()) && !criteria.getDisplayNames().isEmpty()) {
            messageBuilder.append(String.join(", ", criteria.getDisplayNames()));
            messageBuilder.append(", ");
        }
        return messageBuilder.toString();
    }
}
