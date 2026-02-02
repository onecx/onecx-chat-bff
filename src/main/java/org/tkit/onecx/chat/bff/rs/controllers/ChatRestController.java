package org.tkit.onecx.chat.bff.rs.controllers;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.chat.bff.rs.mappers.*;

import gen.org.tkit.onecx.chat.bff.rs.internal.ChatsApiService;
import gen.org.tkit.onecx.chat.bff.rs.internal.model.*;
import gen.org.tkit.onecx.chat.clients.api.ChatsInternalApi;
import gen.org.tkit.onecx.chat.clients.model.*;

@ApplicationScoped
public class ChatRestController implements ChatsApiService {

    @Inject
    @RestClient
    ChatsInternalApi client;

    @Inject
    ChatMapper mapper;

    @Inject
    ParticipantMapper participantMapper;

    @Inject
    MessageMapper messageMapper;

    @Inject
    ChatSocket socket;

    @Inject
    ExceptionMapper exceptionMapper;

    List<ChatDTO> chats = new ArrayList<>();

    @Override
    public Response addParticipant(String chatId, AddParticipantDTO addParticipantDTO) {
        try (Response response = client.addParticipant(chatId, participantMapper.map(addParticipantDTO))) {
            Participant p = response.readEntity(Participant.class);
            return Response.status(Response.Status.OK).entity(participantMapper.map(p)).build();
        }
    }

    @Override
    public Response createChat(CreateChatDTO createChatDTO) {
        try (Response response = client.createChat(mapper.map(createChatDTO))) {
            Chat c = response.readEntity(Chat.class);
            return Response.status(Response.Status.OK).entity(mapper.map(c)).build();
        }
    }

    @Override
    public Response createChatMessage(String chatId, CreateMessageDTO createMessageDTO) {
        try (Response response = client.createChatMessage(chatId, messageMapper.map(createMessageDTO))) {
            MessageDTO mDto = messageMapper.mapToMessage(createMessageDTO);

            try (Response r = client.getChatParticipants(chatId)) {
                List<ParticipantDTO> l = participantMapper.map(r.readEntity(new GenericType<List<Participant>>() {
                }));
                List<String> userNames = new ArrayList<>();

                l.forEach(p -> {
                    userNames.add(p.getUserName());
                });
                this.socket.sendMessage(userNames, chatId, mDto);
            }
            return Response.status(Response.Status.OK).entity(mDto).build();
        }
    }

    @Override
    public Response deleteChat(String id) {
        try (Response response = client.deleteChat(id)) {
            return Response.status(response.getStatus()).build();
        }
    }

    @Override
    public Response getChatById(String id) {
        try (Response response = client.getChatById(id)) {
            Chat c = response.readEntity(Chat.class);
            ChatDTO dto = mapper.map(c);
            return Response.status(Response.Status.OK).entity(dto).build();
        }
    }

    @Override
    public Response getChatMessages(String chatId) {
        try (Response response = client.getChatMessages(chatId)) {
            List<Message> m = response.readEntity(new GenericType<>() {
            });
            List<MessageDTO> m2 = messageMapper.map(m);
            return Response.status(Response.Status.OK).entity(m2).build();
        }
    }

    @Override
    public Response getChatParticipants(String chatId) {
        try (Response response = client.getChatParticipants(chatId)) {
            List<Participant> p = response.readEntity(new GenericType<>() {
            });
            List<ParticipantDTO> p2 = participantMapper.map(p);
            return Response.status(Response.Status.OK).entity(p2).build();
        }
    }

    @Override
    public Response getChats(Integer pageNumber, Integer pageSize) {
        try (Response response = client.getChats(pageNumber, pageSize)) {
            ChatPageResult result = response.readEntity(ChatPageResult.class);
            ChatPageResultDTO resultDTOs = mapper.map(result);
            return Response.status(Response.Status.OK).entity(resultDTOs).build();
        }
    }

    @Override
    public Response searchChats(ChatSearchCriteriaDTO chatSearchCriteriaDTO) {
        try (Response response = client.searchChats(mapper.map(chatSearchCriteriaDTO))) {
            ChatPageResult result = response.readEntity(ChatPageResult.class);
            ChatPageResultDTO resultDTO = mapper.map(result);
            return Response.status(Response.Status.OK).entity(resultDTO).build();
        }
    }

    @Override
    public Response updateChat(String id, UpdateChatDTO updateChatDTO) {
        try (Response response = client.updateChat(id, mapper.map(updateChatDTO))) {
            Chat chat = response.readEntity(Chat.class);
            ChatDTO chatDTO = mapper.map(chat);
            return Response.status(response.getStatus()).entity(chatDTO).build();
        }
    }

    @Override
    public Response removeParticipant(String chatId, String participantId) {
        // TODO implement participant removal in SVC
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'removeParticipant'");
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
