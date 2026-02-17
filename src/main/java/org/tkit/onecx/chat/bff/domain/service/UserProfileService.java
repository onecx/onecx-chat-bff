package org.tkit.onecx.chat.bff.domain.service;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.openapi.quarkus.onecx.user.profile.svc.v1.client.api.UserProfileV1Api;
import org.openapi.quarkus.onecx.user.profile.svc.v1.client.model.UserProfileAbstract;
import org.openapi.quarkus.onecx.user.profile.svc.v1.client.model.UserProfileAbstractCriteria;
import org.openapi.quarkus.onecx.user.profile.svc.v1.client.model.UserProfilePageResult;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class UserProfileService {

    @Inject
    @RestClient
    UserProfileV1Api userProfileClient;

    public Optional<UserProfileAbstract> performSearchRequest(UserProfileAbstractCriteria criteria) {
        UserProfilePageResult result;
        try (Response response = userProfileClient.searchProfileAbstractsByCriteria(criteria)) {
            result = response.readEntity(UserProfilePageResult.class);
        }
        if (result.getStream().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(result.getStream().get(0));
    }
}
