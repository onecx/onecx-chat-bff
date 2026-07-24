package org.tkit.onecx.chat.bff.rs.controllers;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.openapi.quarkus.onecx.ai.provider.svc.v1.client.api.AgentApi;
import org.openapi.quarkus.onecx.ai.provider.svc.v1.client.model.AgentPageResult;
import org.openapi.quarkus.onecx.ai.provider.svc.v1.client.model.AgentSearchCriteria;
import org.tkit.onecx.chat.bff.rs.mappers.AgentMapper;

import gen.org.tkit.onecx.chat.bff.rs.internal.AgentApiService;
import gen.org.tkit.onecx.chat.bff.rs.internal.model.AgentSearchCriteriaDTO;

public class AgentRestController implements AgentApiService {

    @Inject
    @RestClient
    AgentApi client;

    @Inject
    AgentMapper mapper;

    @Override
    public Response findAgentBySearchCriteria(@Valid @NotNull AgentSearchCriteriaDTO agentSearchCriteriaDTO) {
        AgentSearchCriteria agentSearchCriteria = mapper.mapCriteria(agentSearchCriteriaDTO);

        try (Response searchResponse = client.findAgentBySearchCriteria(agentSearchCriteria)) {
            AgentPageResult agentPageResult = searchResponse.readEntity(AgentPageResult.class);
            return Response.status(searchResponse.getStatus()).entity(mapper.mapPageResult(agentPageResult)).build();
        }
    }

}
