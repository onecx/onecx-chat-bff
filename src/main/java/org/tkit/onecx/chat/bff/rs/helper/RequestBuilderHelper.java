package org.tkit.onecx.chat.bff.rs.helper;

import java.util.List;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import org.openapi.quarkus.onecx_user_profile_svc_v1_yaml.model.UserProfileAbstract;
import org.tkit.onecx.chat.bff.rs.mappers.ChatMapper;
import org.tkit.onecx.chat.bff.rs.mappers.ParticipantMapper;

import gen.org.tkit.onecx.chat.bff.rs.internal.model.AddParticipantDTO;
import gen.org.tkit.onecx.chat.bff.rs.internal.model.CreateChatDTO;
import gen.org.tkit.onecx.chat.bff.rs.internal.model.ParticipantDTO;
import gen.org.tkit.onecx.chat.clients.model.AddParticipant;
import gen.org.tkit.onecx.chat.clients.model.CreateChat;

@RequestScoped
public class RequestBuilderHelper {

    @Inject
    ParticipantMapper participantMapper;

    @Inject
    ChatMapper chatMapper;

    public CreateChat getCreateChatDtoRequest(final CreateChatDTO initialRequest,
            final UserProfileAbstract creatorProfile) {
        var participants = initialRequest.getParticipants();
        var updatedParticipants = getUpdatedParticipantsList(participants, creatorProfile);
        initialRequest.setParticipants(updatedParticipants);
        return chatMapper.map(initialRequest);
    }

    public AddParticipant getAddParticipantRequest(final AddParticipantDTO addParticipantDTO,
            final UserProfileAbstract participantProfile) {
        addParticipantDTO.setEmail(participantProfile.getEmailAddress());
        addParticipantDTO.setUserId(participantProfile.getUserId());
        addParticipantDTO.setUserName(participantProfile.getDisplayName());
        return participantMapper.map(addParticipantDTO);
    }

    private List<ParticipantDTO> getUpdatedParticipantsList(final List<ParticipantDTO> participantDTOS,
            final UserProfileAbstract creatorProfile) {
        return participantDTOS.stream().peek(participant -> {
            if (participant.getEmail().equals(creatorProfile.getEmailAddress())) {
                participantMapper.updateFromUserProfile(participant, creatorProfile);
            }
        }).toList();
    }
}
