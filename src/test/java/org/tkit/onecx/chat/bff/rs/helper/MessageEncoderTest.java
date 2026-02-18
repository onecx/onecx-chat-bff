package org.tkit.onecx.chat.bff.rs.helper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.OffsetDateTime;

import jakarta.websocket.EncodeException;

import org.junit.jupiter.api.Test;

import gen.org.tkit.onecx.chat.bff.rs.internal.model.MessageDTO;
import gen.org.tkit.onecx.chat.bff.rs.internal.model.WebsocketHelperDTO;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class MessageEncoderTest {

    @Test
    void encodeTestWithValidObject() throws EncodeException {

        MessageDTO message = new MessageDTO();
        message.setText("Hello");
        message.setUserId("user1");
        message.setCreationDate(OffsetDateTime.parse("2023-01-01T10:00:00Z"));
        WebsocketHelperDTO dto = new WebsocketHelperDTO();
        dto.setChatId("chat123");
        dto.setMessageDTO(message);

        MessageEncoder encoder = new MessageEncoder();
        String json = encoder.encode(dto);

        assertTrue(json.contains("\"chatId\":\"chat123\""));
        assertTrue(json.contains("\"text\":\"Hello\""));
        assertTrue(json.contains("\"userId\":\"user1\""));
    }

    @Test
    void encodeTestWithInvalidObject() throws EncodeException {
        MessageEncoder encoder = new MessageEncoder();
        // Tworzymy mocka, który rzuca wyjątek przy serializacji
        WebsocketHelperDTO dto = mock(WebsocketHelperDTO.class);
        when(dto.getChatId()).thenThrow(new RuntimeException("Serialization error"));
        String json = encoder.encode(dto);
        assertEquals("", json);
    }

    @Test
    void encodeTestWithNull() throws EncodeException {
        MessageEncoder encoder = new MessageEncoder();
        String json = encoder.encode(null);
        assertEquals("null", json.trim());
    }
}
