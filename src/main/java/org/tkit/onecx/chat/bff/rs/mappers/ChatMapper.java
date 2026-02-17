package org.tkit.onecx.chat.bff.rs.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.chat.bff.rs.internal.model.*;
import gen.org.tkit.onecx.chat.clients.model.*;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface ChatMapper {
    CreateChat map(CreateChatDTO createChatDTO, String userId);

    @Mapping(target = "removeParticipantsItem", ignore = true)
    CreateChatDTO map(CreateChat createChat);

    Chat map(ChatDTO chatDTO);

    @Mapping(target = "removeParticipantsItem", ignore = true)
    ChatDTO map(Chat chat);

    @Mapping(target = "removeParticipantsItem", ignore = true)
    UpdateChatDTO map(UpdateChat updateChat);

    UpdateChat map(UpdateChatDTO updateChatDTO);

    ChatSearchCriteriaDTO map(ChatSearchCriteria chatSearchCriteria);

    ChatSearchCriteria map(ChatSearchCriteriaDTO chatSearchCriteriaDTO);

    @Mapping(target = "removeStreamItem", ignore = true)
    ChatPageResultDTO map(ChatPageResult chatPageResult);

    ChatPageResult map(ChatPageResultDTO chatPageResultDTO);

    ChatMessageSearchCriteriaDTO map(ChatMessageSearchCriteria chatMessageSearchCriteria);

    ChatMessageSearchCriteria map(ChatMessageSearchCriteriaDTO chatMessageSearchCriteriaDTO);

    @Mapping(target = "removeStreamItem", ignore = true)
    MessagePageResultDTO map(MessagePageResult messagePageResult);

    @Mapping(target = "removeParticipantsItem", ignore = true)
    ChatMessageResponseDTO map(ChatMessageResponse chatMessageResponse);
}
