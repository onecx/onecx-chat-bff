package org.tkit.onecx.chat.bff.domain.service;

import java.util.List;
import java.util.Objects;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.openapi.quarkus.onecx_user_profile_svc_v1_yaml.api.UserProfileV1Api;
import org.openapi.quarkus.onecx_user_profile_svc_v1_yaml.model.UserProfileAbstract;
import org.openapi.quarkus.onecx_user_profile_svc_v1_yaml.model.UserProfileAbstractCriteria;
import org.openapi.quarkus.onecx_user_profile_svc_v1_yaml.model.UserProfilePageResult;
import org.tkit.onecx.chat.bff.domain.exception.NoUserProfileException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestScoped
public class UserProfileService {

    @Inject
    @RestClient
    UserProfileV1Api userProfileClient;

    public UserProfileAbstract getUserAbstractById(final String id) {
        final UserProfileAbstractCriteria criteria = new UserProfileAbstractCriteria();
        criteria.setUserIds(List.of(id));
        return performSearchRequest(criteria);
    }

    public UserProfileAbstract getUserAbstractByEmailOrUsername(final String email, final String username) {
        final UserProfileAbstractCriteria criteria = new UserProfileAbstractCriteria();
        if (Objects.nonNull(email) && !email.isBlank()) {
            criteria.setEmailAddresses(List.of(email));
        }
        if (Objects.nonNull(username) && !username.isBlank()) {
            criteria.setDisplayNames(List.of(username));
        }
        return performSearchRequest(criteria);
    }

    private UserProfileAbstract performSearchRequest(final UserProfileAbstractCriteria criteria) {
        UserProfilePageResult result;
        try {
            result = userProfileClient.searchProfileAbstractsByCriteria(criteria);
        } catch (RuntimeException e) {
            log.error("Exception occurred during profile retrieval", e);
            throw new RuntimeException(e);
        }
        if (result.getStream().isEmpty()) {
            throw new NoUserProfileException(criteria);
        }
        return result.getStream().get(0);
    }
}
