package org.tkit.onecx.chat.bff.rs.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.openapi.quarkus.onecx.ai.provider.svc.v1.client.api.AgentApi;
import org.openapi.quarkus.onecx.ai.provider.svc.v1.client.model.AgentPageResult;
import org.openapi.quarkus.onecx.ai.provider.svc.v1.client.model.AgentSearchCriteria;
import org.tkit.onecx.chat.bff.rs.mappers.AgentMapper;
import org.tkit.onecx.chat.bff.rs.mappers.ExceptionMapper;

import gen.org.tkit.onecx.chat.bff.rs.internal.AgentApiService;
import gen.org.tkit.onecx.chat.bff.rs.internal.model.AgentSearchCriteriaDTO;
import gen.org.tkit.onecx.chat.bff.rs.internal.model.ProblemDetailResponseDTO;

@ApplicationScoped
public class AgentRestController implements AgentApiService {

    @Inject
    @RestClient
    AgentApi client;

    @Inject
    AgentMapper mapper;

    @Inject
    ExceptionMapper exceptionMapper;

    @Override
    public Response findAgentBySearchCriteria(AgentSearchCriteriaDTO agentSearchCriteriaDTO) {
        AgentSearchCriteria agentSearchCriteria = mapper.mapCriteria(agentSearchCriteriaDTO);

        try (Response searchResponse = client.findAgentBySearchCriteria(agentSearchCriteria)) {
            AgentPageResult agentPageResult = searchResponse.readEntity(AgentPageResult.class);
            return Response.status(searchResponse.getStatus()).entity(mapper.mapPageResult(agentPageResult)).build();
        }
    }

    @ServerExceptionMapper
    public Response exception(ClientWebApplicationException ex) {
        return exceptionMapper.clientException(ex);
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> constraint(ConstraintViolationException ex) {
        return exceptionMapper.constraint(ex);
    }
}
