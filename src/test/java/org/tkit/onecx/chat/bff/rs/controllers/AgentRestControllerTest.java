package org.tkit.onecx.chat.bff.rs.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.util.List;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.JsonBody;
import org.mockserver.model.MediaType;
import org.openapi.quarkus.onecx.ai.provider.svc.v1.client.model.AgentAbstract;
import org.openapi.quarkus.onecx.ai.provider.svc.v1.client.model.AgentPageResult;
import org.tkit.onecx.chat.bff.rs.AbstractTest;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.chat.bff.rs.internal.model.*;
import gen.org.tkit.onecx.chat.clients.model.ProblemDetailResponse;
import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.keycloak.client.KeycloakTestClient;

@QuarkusTest
@LogService
@TestHTTPEndpoint(AgentRestController.class)
class AgentRestControllerTest extends AbstractTest {

    @InjectMockServerClient
    MockServerClient mockServerClient;

    KeycloakTestClient keycloakTestClient = new KeycloakTestClient();
    static final String MOCK_ID = "MOCK";
    static final String USERNAME_TOKEN = "apm-username";

    @AfterEach
    void resetMockserver() {
        try {
            mockServerClient.clear(MOCK_ID);
        } catch (Exception _) {
            // mockId not existing
        }
    }

    @Test
    void searchAgentTest() {
        AgentAbstract agent1 = new AgentAbstract().id("1").name("agent1").description("desc1");
        AgentAbstract agent2 = new AgentAbstract().id("2").name("agent2").description("desc2");
        AgentAbstract agent3 = new AgentAbstract().id("3").name("agent3").description("desc3");

        AgentPageResult pageResult = new AgentPageResult();
        pageResult.setNumber(0);
        pageResult.setSize(10);
        pageResult.setTotalPages(1L);
        pageResult.setTotalElements(3L);
        pageResult.setStream(List.of(agent1, agent2, agent3));

        mockServerClient.when(
                request().withPath("/v1/agents/search")
                        .withMethod(HttpMethod.POST))
                .withId(MOCK_ID)
                .respond(httpRequest -> response()
                        .withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(pageResult)));

        var criteria = new AgentSearchCriteriaDTO();

        var data = given()
                .when()
                .auth().oauth2(keycloakTestClient.getAccessToken(ADMIN))
                .header(USERNAME_TOKEN, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(criteria)
                .post()
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract()
                .as(AgentPageResultDTO.class);

        assertThat(data).isNotNull();
        assertThat(data.getTotalElements()).isEqualTo(3);
        assertThat(data.getStream()).isNotNull().hasSize(3);
    }

    @Test
    void searchAgentShouldReturnBadRequest() {
        ProblemDetailResponse problemDetailResponse = new ProblemDetailResponse();
        problemDetailResponse.setErrorCode(String.valueOf(BAD_REQUEST.getStatusCode()));
        problemDetailResponse.setDetail("Bad Request");

        mockServerClient.when(request()
                .withPath("/v1/agents/search")
                .withMethod(HttpMethod.POST))
                .withId(MOCK_ID)
                .respond(httpRequest -> response()
                        .withStatusCode(Response.Status.BAD_REQUEST.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(problemDetailResponse)));

        var criteria = new AgentSearchCriteriaDTO();

        var response = given()
                .when()
                .auth().oauth2(keycloakTestClient.getAccessToken(ADMIN))
                .header(USERNAME_TOKEN, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(criteria)
                .post()
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .extract()
                .as(ProblemDetailResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.getDetail()).isEqualTo("Bad Request");
        assertThat(Integer.valueOf(response.getErrorCode())).isEqualTo(BAD_REQUEST.getStatusCode());
    }

    @Test
    void searchAgentWithNoBodyShouldReturnBadRequest() {
        given()
                .when()
                .auth().oauth2(keycloakTestClient.getAccessToken(ADMIN))
                .header(USERNAME_TOKEN, ADMIN)
                .contentType(APPLICATION_JSON)
                .post()
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

}
