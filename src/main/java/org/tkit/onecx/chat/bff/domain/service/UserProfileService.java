package org.tkit.onecx.chat.bff.domain.service;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.openapi.quarkus.onecx_user_profile_svc_v1_yaml.api.UserProfileV1Api;
import org.openapi.quarkus.onecx_user_profile_svc_v1_yaml.model.UserProfileAbstract;
import org.openapi.quarkus.onecx_user_profile_svc_v1_yaml.model.UserProfileAbstractCriteria;
import org.openapi.quarkus.onecx_user_profile_svc_v1_yaml.model.UserProfilePageResult;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class UserProfileService {

    @Inject
    @RestClient
    UserProfileV1Api userProfileClient;

    public Optional<UserProfileAbstract> performSearchRequest(final UserProfileAbstractCriteria criteria) {
        UserProfilePageResult result;
        result = userProfileClient.searchProfileAbstractsByCriteria(criteria);
        if (result.getStream().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(result.getStream().get(0));
    }
}
