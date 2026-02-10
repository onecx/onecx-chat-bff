package org.tkit.onecx.chat.bff.rs.controllers;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import org.tkit.onecx.chat.bff.rs.helper.MessageEncoder;

import gen.org.tkit.onecx.chat.bff.rs.internal.model.MessageDTO;
import gen.org.tkit.onecx.chat.bff.rs.internal.model.WebsocketHelperDTO;

@ServerEndpoint(value = "/chats/socket/{userName}", encoders = MessageEncoder.class)
@ApplicationScoped
public class ChatSocket {

    private static final Logger LOGGER = Logger.getLogger(ChatSocket.class.getName());

    Map<String, Session> sessions = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("userName") String userName) {
        sessions.put(userName, session);
    }

    @OnClose
    public void onClose(Session session, @PathParam("userName") String userName) {
        sessions.remove(userName);
    }

    public void sendMessage(List<String> userNames, String chatId, MessageDTO messageDTO) {
        WebsocketHelperDTO helperDTO = new WebsocketHelperDTO();
        helperDTO.setChatId(chatId);
        helperDTO.setMessageDTO(messageDTO);

        for (String userName : userNames) {
            Session session = sessions.get(userName);
            if (session != null) {
                session.getAsyncRemote().sendObject(helperDTO, result -> {
                    if (result.getException() != null) {
                        LOGGER.info("Unable to send message: " + result.getException());
                    }
                });
            }
        }
    }
}
