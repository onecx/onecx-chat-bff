package org.tkit.onecx.chat.bff.rs.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.openapi.quarkus.onecx.ai.provider.svc.v1.client.model.AgentPageResult;
import org.openapi.quarkus.onecx.ai.provider.svc.v1.client.model.AgentSearchCriteria;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.chat.bff.rs.internal.model.*;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface AgentMapper {

    AgentSearchCriteria mapCriteria(AgentSearchCriteriaDTO agentSearchCriteriaDTO);

    @Mapping(target = "removeStreamItem", ignore = true)
    AgentPageResultDTO mapPageResult(AgentPageResult agentPageResult);
}
