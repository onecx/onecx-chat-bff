package org.tkit.onecx.chat.bff.rs.mappers;

import java.util.List;
import java.util.Objects;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.openapi.quarkus.onecx_user_profile_svc_v1_yaml.model.UserProfileAbstractCriteria;

import gen.org.tkit.onecx.chat.bff.rs.internal.model.AddParticipantDTO;

@Mapper
public interface UserProfileMapper {

    @Mapping(target = "userIds", source = "userId", qualifiedByName = "stringToList")
    @Mapping(target = "displayNames", source = "userName", qualifiedByName = "stringToList")
    @Mapping(target = "emailAddresses", source = "email", qualifiedByName = "stringToList")
    @Mapping(target = "pageNumber", ignore = true)
    @Mapping(target = "pageSize", ignore = true)
    UserProfileAbstractCriteria mapToCriteria(AddParticipantDTO addParticipantDTO);

    @Named("stringToList")
    default List<String> stringToList(String value) {
        return (Objects.nonNull(value) && !value.isBlank()) ? List.of(value) : null;
    }
}
