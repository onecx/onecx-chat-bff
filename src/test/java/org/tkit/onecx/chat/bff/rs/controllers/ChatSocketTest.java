package org.tkit.onecx.chat.bff.rs.controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import jakarta.websocket.RemoteEndpoint;
import jakarta.websocket.SendHandler;
import jakarta.websocket.SendResult;
import jakarta.websocket.Session;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gen.org.tkit.onecx.chat.bff.rs.internal.model.MessageDTO;
import gen.org.tkit.onecx.chat.bff.rs.internal.model.WebsocketHelperDTO;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class ChatSocketTest {

    private ChatSocket chatSocket;
    private Session session;
    private RemoteEndpoint.Async asyncRemote;

    @BeforeEach
    void setUp() {
        chatSocket = new ChatSocket();
        session = mock(Session.class);
        asyncRemote = mock(RemoteEndpoint.Async.class);
        when(session.getAsyncRemote()).thenReturn(asyncRemote);
        chatSocket.sessions.put("user1", session);
    }

    @Test
    void testOnOpenAndOnClose() {
        String userName = "user1";
        chatSocket.onOpen(session, userName);
        assertTrue(chatSocket.sessions.containsKey(userName));
        chatSocket.onClose(session, userName);
        assertFalse(chatSocket.sessions.containsKey(userName));
    }

    @Test
    void testSendMessageSuccess() {
        String userName = "user2";
        chatSocket.sessions.put(userName, session);
        MessageDTO messageDTO = new MessageDTO();
        chatSocket.sendMessage(List.of(userName), "chatId", messageDTO);
        verify(asyncRemote, times(1))
                .sendObject(any(WebsocketHelperDTO.class), any());
    }

    @Test
    void testSendMessageNoSession() {
        MessageDTO messageDTO = new MessageDTO();
        // No session for user4
        chatSocket.sendMessage(List.of("user4"), "chatId", messageDTO);
        // Should not throw
    }

    @Test
    void testSendMessageWithExceptionBranch() {
        // Przechwyć SendHandler i wywołaj z mockiem SendResult z wyjątkiem
        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            SendHandler handler = (SendHandler) args[1];
            SendResult sendResult = mock(SendResult.class);
            when(sendResult.getException()).thenReturn(new RuntimeException("fail"));
            handler.onResult(sendResult);
            return null;
        }).when(asyncRemote).sendObject(any(WebsocketHelperDTO.class), any(SendHandler.class));

        MessageDTO messageDTO = new MessageDTO();
        chatSocket.sendMessage(List.of("user1"), "chatId", messageDTO);

    }
}
