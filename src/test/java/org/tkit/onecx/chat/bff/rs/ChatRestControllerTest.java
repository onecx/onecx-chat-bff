package org.tkit.onecx.chat.bff.rs;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.util.ArrayList;
import java.util.List;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.HttpHeaders;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.Header;
import org.mockserver.model.JsonBody;
import org.mockserver.model.MediaType;
import org.tkit.onecx.chat.bff.rs.controllers.ChatRestController;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.chat.bff.rs.internal.model.*;
import gen.org.tkit.onecx.chat.clients.model.*;
import gen.org.tkit.onecx.permission.model.ProblemDetailResponse;
import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.keycloak.client.KeycloakTestClient;
import io.restassured.common.mapper.TypeRef;

@QuarkusTest
@LogService
@TestHTTPEndpoint(ChatRestController.class)
public class ChatRestControllerTest extends AbstractTest {

    @InjectMockServerClient
    public MockServerClient mockServerClient;

    KeycloakTestClient keycloakTestClient = new KeycloakTestClient();

    static final String mockId = "MOCK";
    static final String mockIdSecondary = "MOCK_SECONDARY";

    @BeforeEach
    public void resetExpectation() {
        try {
            mockServerClient.clear(mockId);
            mockServerClient.clear(mockIdSecondary);
        } catch (Exception e) {
            // mockid not existing
        }
    }

    @Test
    public void getChatChatByIdTest() {
        var chatId = "id";

        ChatDTO chatDTO = new ChatDTO();
        chatDTO.setType(ChatTypeDTO.HUMAN_DIRECT_CHAT);

        mockServerClient
                .when(request()
                        .withPath("/internal/chats/" + chatId)
                        .withMethod(HttpMethod.GET))
                .withId(mockId)
                .respond(httpRequest -> response().withStatusCode(OK.getStatusCode())
                        .withHeaders(new Header(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON))
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(chatDTO)));

        var res = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .pathParam("id", chatId)
                .get("{id}")
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().as(ChatDTO.class);

        assertThat(res).isNotNull();
        assertThat(res.getType()).isEqualTo((ChatTypeDTO.HUMAN_DIRECT_CHAT));
    }

    @Test
    public void getChatById_shouldReturnBadRequest() {
        var chatId = "id";

        ProblemDetailResponse problemDetailResponse = new ProblemDetailResponse();
        problemDetailResponse.setErrorCode(String.valueOf(BAD_REQUEST.getStatusCode()));
        problemDetailResponse.setDetail("Bad Request");

        mockServerClient.when(request()
                .withPath("/internal/chats/" + chatId)
                .withMethod(HttpMethod.GET))
                .withId(mockId)
                .respond(httpRequest -> response().withStatusCode(BAD_REQUEST.getStatusCode())
                        .withBody(JsonBody.json(problemDetailResponse)));

        var response = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .pathParam("id", chatId)
                .get("{id}")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.getDetail()).isEqualTo("Bad Request");
        assertThat(Integer.valueOf(response.getErrorCode())).isEqualTo(BAD_REQUEST.getStatusCode());
    }

    @Test
    public void addParticipantTest() {
        var chatId = "id";
        AddParticipant addParticipant = new AddParticipant();
        addParticipant.setUserId("userId");
        addParticipant.setType(ParticipantType.HUMAN);

        mockServerClient.when(request()
                .withPath("/internal/chats/" + chatId + "/participants")
                .withMethod(HttpMethod.POST))
                .withId(mockId)
                .respond(httpRequest -> response().withStatusCode(OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(addParticipant)));

        var res = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .pathParam("id", chatId)
                .body(addParticipant)
                .post("/{id}/participants")
                .then()
                .statusCode(OK.getStatusCode())
                .extract();

        assertThat(res).isNotNull();
    }

    @Test
    public void addParticipantShouldReturnBadRequest() {
        var chatId = "id";

        AddParticipant addParticipant = new AddParticipant();
        addParticipant.setUserId("userId");
        addParticipant.setType(ParticipantType.HUMAN);

        ProblemDetailResponse problemDetailResponse = new ProblemDetailResponse();
        problemDetailResponse.setErrorCode(String.valueOf(BAD_REQUEST.getStatusCode()));
        problemDetailResponse.setDetail("Bad Request");

        mockServerClient.when(request()
                .withPath("/internal/chats/" + chatId + "/participants")
                .withMethod(HttpMethod.POST))
                .withId(mockId)
                .respond(httpRequest -> response().withStatusCode(BAD_REQUEST.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(problemDetailResponse)));

        var response = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .pathParam("id", chatId)
                .body(addParticipant)
                .post("/{id}/participants")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.getDetail()).isEqualTo("Bad Request");
        assertThat(Integer.valueOf(response.getErrorCode())).isEqualTo(BAD_REQUEST.getStatusCode());
    }

    @Test
    public void createChatTest() {

        CreateChatDTO createChatDTO = new CreateChatDTO();
        createChatDTO.setType(ChatTypeDTO.AI_CHAT);
        createChatDTO.setId("chat-id");
        createChatDTO.setAppId("app-2");

        Chat chat = new Chat();
        chat.setType(ChatType.AI_CHAT);
        chat.setId("chat-id");
        chat.setAppId("app-2");

        mockServerClient.when(request()
                .withPath("/internal/chats")
                .withMethod(HttpMethod.POST))
                .withId(mockId)
                .respond(httpRequest -> response().withStatusCode(OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(chat)));

        var res = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(createChatDTO)
                .post()
                .then()
                .statusCode(OK.getStatusCode())
                .extract()
                .body().as(ChatDTO.class);

        assertThat(res).isNotNull();
        assertThat(res.getType()).isEqualTo(ChatTypeDTO.AI_CHAT);
        assertThat(res.getId()).isEqualTo("chat-id");
        assertThat(res.getAppId()).isEqualTo("app-2");
    }

    @Test
    public void createChatShouldReturnBadRequest() {
        CreateChatDTO createChatDTO = new CreateChatDTO();
        createChatDTO.setType(ChatTypeDTO.AI_CHAT);
        createChatDTO.setId("chat-id");
        createChatDTO.setAppId("app-2");

        ProblemDetailResponse problemDetailResponse = new ProblemDetailResponse();
        problemDetailResponse.setErrorCode(String.valueOf(BAD_REQUEST.getStatusCode()));
        problemDetailResponse.setDetail("Bad Request");

        mockServerClient.when(request()
                .withPath("/internal/chats")
                .withMethod(HttpMethod.POST))
                .withId(mockId)
                .respond(httpRequest -> response().withStatusCode(BAD_REQUEST.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(problemDetailResponse)));

        var response = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(createChatDTO)
                .post()
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract()
                .body().as(ProblemDetailResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.getDetail()).isEqualTo("Bad Request");
        assertThat(Integer.valueOf(response.getErrorCode())).isEqualTo(BAD_REQUEST.getStatusCode());
    }

    @Test
    public void createChatMessageTest() {
        var chatId = "id";

        CreateMessageDTO createMessageDTO = new CreateMessageDTO();
        createMessageDTO.setType(MessageTypeDTO.HUMAN);

        Participant participant = new Participant();
        participant.setType(ParticipantType.HUMAN);
        participant.setUserName("user1");
        Participant participant2 = new Participant();
        participant2.setType(ParticipantType.ASSISTANT);
        participant2.setUserName("user2");
        List<Participant> participants = new ArrayList<>();
        participants.add(participant);
        participants.add(participant2);

        mockServerClient.when(request()
                .withPath("/internal/chats/" + chatId + "/messages")
                .withMethod(HttpMethod.POST))
                .withId(mockId)
                .respond(httpRequest -> response().withStatusCode(CREATED.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON));

        mockServerClient.when(request()
                .withPath("/internal/chats/" + chatId + "/participants")
                .withMethod(HttpMethod.GET))
                .withId(mockIdSecondary)
                .respond(httpRequest -> response().withStatusCode(OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(participants)));

        var res = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .pathParam("id", chatId)
                .body(createMessageDTO)
                .post("/{id}/messages")
                .then()
                .statusCode(OK.getStatusCode())
                .extract()
                .body()
                .as(MessageDTO.class);

        assertThat(res).isNotNull();
    }

    @Test
    public void createChatMessageShouldReturnBadRequest() {

        var chatId = "id";

        CreateMessageDTO createMessageDTO = new CreateMessageDTO();
        createMessageDTO.setType(MessageTypeDTO.HUMAN);

        ProblemDetailResponse problemDetailResponse = new ProblemDetailResponse();
        problemDetailResponse.setErrorCode(String.valueOf(BAD_REQUEST.getStatusCode()));
        problemDetailResponse.setDetail("Bad Request");

        mockServerClient.when(request()
                .withPath("/internal/chats/" + chatId + "/messages")
                .withMethod(HttpMethod.POST))
                .withId(mockId)
                .respond(httpRequest -> response().withStatusCode(BAD_REQUEST.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(problemDetailResponse)));

        var response = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .pathParam("id", chatId)
                .body(createMessageDTO)
                .post("/{id}/messages")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract()
                .body().as(ProblemDetailResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.getDetail()).isEqualTo("Bad Request");
        assertThat(Integer.valueOf(response.getErrorCode())).isEqualTo(BAD_REQUEST.getStatusCode());
    }

    @Test
    public void getChatMessagesShouldReturnBadRequestWhenCallingGetParticipants() {
        var chatId = "id";

        CreateMessageDTO createMessageDTO = new CreateMessageDTO();
        createMessageDTO.setType(MessageTypeDTO.HUMAN);

        ProblemDetailResponse problemDetailResponse = new ProblemDetailResponse();
        problemDetailResponse.setErrorCode(String.valueOf(BAD_REQUEST.getStatusCode()));
        problemDetailResponse.setDetail("Bad Request");

        mockServerClient.when(request()
                .withPath("/internal/chats/" + chatId + "/messages")
                .withMethod(HttpMethod.POST))
                .withId(mockId)
                .respond(httpRequest -> response().withStatusCode(CREATED.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON));

        mockServerClient.when(request()
                .withPath("/internal/chats/" + chatId + "/participants")
                .withMethod(HttpMethod.GET))
                .withId(mockIdSecondary)
                .respond(httpRequest -> response().withStatusCode(BAD_REQUEST.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(problemDetailResponse)));

        var response = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .pathParam("id", chatId)
                .body(createMessageDTO)
                .post("/{id}/messages")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract()
                .body().as(ProblemDetailResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.getDetail()).isEqualTo("Bad Request");
        assertThat(Integer.valueOf(response.getErrorCode())).isEqualTo(BAD_REQUEST.getStatusCode());
    }

    @Test
    public void deleteChatTest() {
        var chatId = "id";

        mockServerClient.when(request()
                .withPath("/internal/chats/" + chatId)
                .withMethod(HttpMethod.DELETE))
                .withId(mockId)
                .respond(httpRequest -> response().withStatusCode(NO_CONTENT.getStatusCode()));

        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .pathParam("id", chatId)
                .delete("{id}")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());
    }

    @Test
    public void deleteChatShouldReturnBadRequest() {

        var chatId = "id";

        ProblemDetailResponse problemDetailResponse = new ProblemDetailResponse();
        problemDetailResponse.setErrorCode(String.valueOf(BAD_REQUEST.getStatusCode()));
        problemDetailResponse.setDetail("Bad Request");

        mockServerClient.when(request()
                .withPath("/internal/chats/" + chatId)
                .withMethod(HttpMethod.DELETE))
                .withId(mockId)
                .respond(httpRequest -> response().withStatusCode(BAD_REQUEST.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(problemDetailResponse)));

        var response = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .pathParam("id", chatId)
                .delete("{id}")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract()
                .body().as(ProblemDetailResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.getDetail()).isEqualTo("Bad Request");
        assertThat(Integer.valueOf(response.getErrorCode())).isEqualTo(BAD_REQUEST.getStatusCode());
    }

    @Test
    public void getChatMessagesTest() {
        var chatId = "id";

        Message message = new Message();
        message.setType(MessageType.HUMAN);
        message.setId("msg1");
        message.setText("message1");

        Message message2 = new Message();
        message2.setType(MessageType.ASSISTANT);
        message2.setId("msg2");
        message2.setText("message2");

        List<Message> messages = new ArrayList<>();
        messages.add(message);
        messages.add(message2);

        mockServerClient.when(request()
                .withPath("/internal/chats/" + chatId + "/messages")
                .withMethod(HttpMethod.GET))
                .withId(mockId)
                .respond(httpRequest -> response().withStatusCode(OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(messages)));

        var res = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .pathParam("id", chatId)
                .get("/{id}/messages")
                .then()
                .statusCode(OK.getStatusCode())
                .extract()
                .body()
                .as(new TypeRef<List<MessageDTO>>() {
                });

        assertThat(res).isNotNull();
        assertThat(res).isNotNull().isNotEmpty().hasSize(2);
        assertThat(res.get(0).getType()).isEqualTo(MessageTypeDTO.HUMAN);
        assertThat(res.get(1).getType()).isEqualTo(MessageTypeDTO.ASSISTANT);
    }

    @Test
    public void getChatMessagesShouldReturnBadRequest() {

        var chatId = "id";

        ProblemDetailResponse problemDetailResponse = new ProblemDetailResponse();
        problemDetailResponse.setErrorCode(String.valueOf(BAD_REQUEST.getStatusCode()));
        problemDetailResponse.setDetail("Bad Request");

        mockServerClient.when(request()
                .withPath("/internal/chats/" + chatId + "/messages")
                .withMethod(HttpMethod.GET))
                .withId(mockId)
                .respond(httpRequest -> response().withStatusCode(BAD_REQUEST.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(problemDetailResponse)));

        var response = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .pathParam("id", chatId)
                .get("/{id}/messages")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract()
                .body().as(ProblemDetailResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.getDetail()).isEqualTo("Bad Request");
        assertThat(Integer.valueOf(response.getErrorCode())).isEqualTo(BAD_REQUEST.getStatusCode());
    }

    @Test
    public void getChatParticipantsTest() {
        var chatId = "id";

        Participant participant = new Participant();
        participant.setType(ParticipantType.HUMAN);

        Participant participant2 = new Participant();
        participant2.setType(ParticipantType.ASSISTANT);

        List<Participant> participants = new ArrayList<>();
        participants.add(participant);
        participants.add(participant2);

        mockServerClient.when(request()
                .withPath("/internal/chats/" + chatId + "/participants")
                .withMethod(HttpMethod.GET))
                .withId(mockId)
                .respond(httpRequest -> response().withStatusCode(OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(participants)));

        var res = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .pathParam("id", chatId)
                .get("/{id}/participants")
                .then()
                .statusCode(OK.getStatusCode())
                .extract()
                .body().as(new TypeRef<List<ParticipantDTO>>() {
                });

        assertThat(res).isNotNull();
        assertThat(res).isNotNull().isNotEmpty().hasSize(2);
        assertThat(res.get(0).getType()).isEqualTo(ParticipantTypeDTO.HUMAN);
        assertThat(res.get(1).getType()).isEqualTo(ParticipantTypeDTO.ASSISTANT);
    }

    @Test
    public void getChatParticipantsShouldReturnBadRequest() {

        var chatId = "id";

        ProblemDetailResponse problemDetailResponse = new ProblemDetailResponse();
        problemDetailResponse.setErrorCode(String.valueOf(BAD_REQUEST.getStatusCode()));
        problemDetailResponse.setDetail("Bad Request");

        mockServerClient.when(request()
                .withPath("/internal/chats/" + chatId + "/participants")
                .withMethod(HttpMethod.GET))
                .withId(mockId)
                .respond(httpRequest -> response().withStatusCode(BAD_REQUEST.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(problemDetailResponse)));

        var response = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .pathParam("id", chatId)
                .get("/{id}/participants")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract()
                .body().as(ProblemDetailResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.getDetail()).isEqualTo("Bad Request");
        assertThat(Integer.valueOf(response.getErrorCode())).isEqualTo(BAD_REQUEST.getStatusCode());
    }

    @Test
    public void getChatsTest() {

        ChatDTO chatDTO = new ChatDTO();
        chatDTO.setType(ChatTypeDTO.HUMAN_DIRECT_CHAT);

        List<ChatDTO> chatDTOList = new ArrayList<>();
        chatDTOList.add(chatDTO);

        ChatPageResultDTO chatPageResultDTO = new ChatPageResultDTO();
        chatPageResultDTO.setNumber(2);
        chatPageResultDTO.setSize(5);
        chatPageResultDTO.setStream(chatDTOList);

        mockServerClient.when(request()
                .withPath("/internal/chats")
                .withMethod(HttpMethod.GET))
                .withId(mockId)
                .respond(httpRequest -> response().withStatusCode(OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(chatPageResultDTO)));

        var response = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .queryParam("pageNumber", 2)
                .queryParam("pageSize", 5)
                .get()
                .then()
                .statusCode(OK.getStatusCode())
                .extract()
                .body().as(ChatPageResultDTO.class);

        assertThat(response).isNotNull();
        assertThat(response.getNumber()).isEqualTo(2);
        assertThat(response.getSize()).isEqualTo(5);
        assertThat(response.getStream()).isNotNull().hasSize(1);

    }

    @Test
    void getChatsShouldReturnBadRequest() {
        ProblemDetailResponse problemDetailResponse = new ProblemDetailResponse();
        problemDetailResponse.setErrorCode(String.valueOf(BAD_REQUEST.getStatusCode()));
        problemDetailResponse.setDetail("Bad Request");

        ChatPageResultDTO chatPageResultDTO = new ChatPageResultDTO();
        chatPageResultDTO.setNumber(2);
        chatPageResultDTO.setSize(5);

        mockServerClient.when(request()
                .withPath("/internal/chats")
                .withMethod(HttpMethod.GET))
                .withId(mockId)
                .respond(httpRequest -> response().withStatusCode(BAD_REQUEST.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(problemDetailResponse)));

        var response = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .queryParam("pageNumber", 2)
                .queryParam("pageSize", 5)
                .get()
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract()
                .body().as(ProblemDetailResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.getDetail()).isEqualTo("Bad Request");
        assertThat(Integer.valueOf(response.getErrorCode())).isEqualTo(BAD_REQUEST.getStatusCode());

    }

    @Test
    public void searchChatsTest() {
        ChatDTO chatDTO = new ChatDTO();
        chatDTO.setType(ChatTypeDTO.HUMAN_DIRECT_CHAT);

        List<ChatDTO> chatDTOList = new ArrayList<>();
        chatDTOList.add(chatDTO);

        ChatPageResultDTO chatPageResultDTO = new ChatPageResultDTO();
        chatPageResultDTO.setNumber(2);
        chatPageResultDTO.setSize(5);
        chatPageResultDTO.setStream(chatDTOList);

        ChatSearchCriteriaDTO chatSearchCriteriaDTO = new ChatSearchCriteriaDTO();
        chatSearchCriteriaDTO.setType(ChatTypeDTO.HUMAN_DIRECT_CHAT);

        mockServerClient.when(request()
                .withPath("/internal/chats/search")
                .withMethod(HttpMethod.POST))
                .withId(mockId)
                .respond(httpRequest -> response().withStatusCode(OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(chatPageResultDTO)));

        var response = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(chatSearchCriteriaDTO)
                .post("/search")
                .then()
                .statusCode(OK.getStatusCode())
                .extract()
                .body().as(ChatPageResultDTO.class);

        assertThat(response).isNotNull();
        assertThat(response.getStream()).isNotNull().hasSize(1);
        assertThat(response.getStream().get(0).getType()).isEqualTo(ChatTypeDTO.HUMAN_DIRECT_CHAT);

    }

    @Test
    void searchChatsShouldReturnBadRequest() {
        ProblemDetailResponse problemDetailResponse = new ProblemDetailResponse();
        problemDetailResponse.setErrorCode(String.valueOf(BAD_REQUEST.getStatusCode()));
        problemDetailResponse.setDetail("Bad Request");

        ChatSearchCriteriaDTO chatSearchCriteriaDTO = new ChatSearchCriteriaDTO();
        chatSearchCriteriaDTO.setType(ChatTypeDTO.HUMAN_DIRECT_CHAT);

        mockServerClient.when(request()
                .withPath("/internal/chats/search")
                .withMethod(HttpMethod.POST))
                .withId(mockId)
                .respond(httpRequest -> response().withStatusCode(BAD_REQUEST.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(problemDetailResponse)));

        var response = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(chatSearchCriteriaDTO)
                .post("/search")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract()
                .body().as(ProblemDetailResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.getDetail()).isEqualTo("Bad Request");
        assertThat(Integer.valueOf(response.getErrorCode())).isEqualTo(BAD_REQUEST.getStatusCode());
    }

    @Test
    public void updateChatTest() {
        var chatId = "id";

        UpdateChatDTO updateChatDTO = new UpdateChatDTO();
        updateChatDTO.setType(ChatTypeDTO.HUMAN_DIRECT_CHAT);

        Chat chat = new Chat();
        chat.setType(ChatType.HUMAN_DIRECT_CHAT);

        mockServerClient.when(request()
                .withPath("/internal/chats/" + chatId)
                .withMethod(HttpMethod.PUT))
                .withId(mockId)
                .respond(httpRequest -> response().withStatusCode(OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(chat)));

        var res = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .pathParam("id", chatId)
                .body(updateChatDTO)
                .put("{id}")
                .then()
                .statusCode(OK.getStatusCode())
                .extract()
                .body()
                .as(ChatDTO.class);

        assertThat(res).isNotNull();
        assertThat(res.getType()).isEqualTo(ChatTypeDTO.HUMAN_DIRECT_CHAT);
    }

    @Test
    public void updateChatShouldReturnBadRequest() {
        var chatId = "id";

        UpdateChatDTO updateChatDTO = new UpdateChatDTO();
        updateChatDTO.setType(ChatTypeDTO.HUMAN_DIRECT_CHAT);

        ProblemDetailResponse problemDetailResponse = new ProblemDetailResponse();
        problemDetailResponse.setErrorCode(String.valueOf(BAD_REQUEST.getStatusCode()));
        problemDetailResponse.setDetail("Bad Request");

        mockServerClient.when(request()
                .withPath("/internal/chats/" + chatId)
                .withMethod(HttpMethod.PUT))
                .withId(mockId)
                .respond(httpRequest -> response().withStatusCode(BAD_REQUEST.getStatusCode())
                        .withBody(JsonBody.json(problemDetailResponse)));

        var response = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .pathParam("id", chatId)
                .body(updateChatDTO)
                .put("{id}")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract()
                .body().as(ProblemDetailResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.getDetail()).isEqualTo("Bad Request");
        assertThat(Integer.valueOf(response.getErrorCode())).isEqualTo(BAD_REQUEST.getStatusCode());
    }
}
