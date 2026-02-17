package org.tkit.onecx.chat.bff.rs.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.openapi.quarkus.onecx.user.profile.svc.v1.client.model.UserProfileAbstract;

import gen.org.tkit.onecx.chat.bff.rs.internal.model.AddParticipantDTO;
import gen.org.tkit.onecx.chat.bff.rs.internal.model.ParticipantDTO;
import gen.org.tkit.onecx.chat.clients.model.AddParticipant;
import gen.org.tkit.onecx.chat.clients.model.Participant;

@Mapper
public interface ParticipantMapper {
    ParticipantDTO map(Participant participant);

    Participant map(ParticipantDTO participantDTO);

    AddParticipantDTO map(AddParticipant addParticipant);

    AddParticipant map(AddParticipantDTO addParticipantDTO);

    List<ParticipantDTO> map(List<Participant> participants);

    @Mapping(source = "displayName", target = "userName")
    @Mapping(source = "emailAddress", target = "email")
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    void updateFromUserProfile(@MappingTarget ParticipantDTO participantDTO, UserProfileAbstract user);

    @Mapping(source = "displayName", target = "userName")
    @Mapping(source = "emailAddress", target = "email")
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    void updateFromUserProfile(@MappingTarget AddParticipantDTO participantDTO, UserProfileAbstract user);

    default List<ParticipantDTO> updateParticipantWithUserProfile(List<ParticipantDTO> participants,
            UserProfileAbstract userProfile) {
        return participants.stream().map(participant -> {
            if (participant.getEmail().equals(userProfile.getEmailAddress())) {
                updateFromUserProfile(participant, userProfile);
            }
            return participant;
        }).toList();
    }
}
