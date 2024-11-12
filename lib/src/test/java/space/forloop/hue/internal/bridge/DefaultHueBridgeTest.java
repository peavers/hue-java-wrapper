package space.forloop.hue.internal.bridge;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import space.forloop.hue.api.AuthenticatedBridge;
import space.forloop.hue.exception.HueAuthenticationException;
import space.forloop.hue.exception.HueConnectionException;
import space.forloop.hue.internal.bridge.authenticated.AuthenticatedBridgeFactory;
import space.forloop.hue.internal.service.HueApiService;
import space.forloop.hue.internal.service.HueApiServiceFactory;
import space.forloop.hue.model.BridgeAuthentication;

@ExtendWith(MockitoExtension.class)
class DefaultHueBridgeTest {

    private static final String IP_ADDRESS = "192.168.1.100";
    private static final String APPLICATION_NAME = "TestApp";
    private static final String BRIDGE_ID = "ABCD1234";
    private static final String USERNAME = "testUsername";

    @Mock private HueApiServiceFactory apiServiceFactory;

    @Mock private HueApiService hueApiService;

    @Mock private ObjectMapper objectMapper;

    @Mock private AuthenticatedBridgeFactory authenticatedBridgeFactory;

    @Mock private Call<JsonNode> configCall;

    @Mock private Call<List<JsonNode>> authCall;

    @Mock private JsonNode configResponse;

    @Mock private JsonNode successNode;

    @Mock private ObjectNode requestBody;

    @Mock private AuthenticatedBridge authenticatedBridge;

    @InjectMocks private DefaultHueBridge defaultHueBridge;

    @BeforeEach
    void setUp() {
        when(apiServiceFactory.create(IP_ADDRESS)).thenReturn(hueApiService);
        defaultHueBridge =
                new DefaultHueBridge(
                        IP_ADDRESS, apiServiceFactory, objectMapper, authenticatedBridgeFactory);
    }

    @Test
    void getIpAddress_shouldReturnConfiguredIpAddress() {
        assertEquals(IP_ADDRESS, defaultHueBridge.getIpAddress());
    }

    @Test
    void getBridgeId_successfulResponse_shouldReturnBridgeId()
            throws IOException, HueConnectionException {
        // Arrange
        when(hueApiService.getConfig()).thenReturn(configCall);
        when(configCall.execute()).thenReturn(Response.success(configResponse));
        when(configResponse.path("bridgeid")).thenReturn(configResponse);
        when(configResponse.asText()).thenReturn(BRIDGE_ID);

        // Act
        String result = defaultHueBridge.getBridgeId();

        // Assert
        assertEquals(BRIDGE_ID, result);
        verify(hueApiService).getConfig();
    }

    @Test
    void getBridgeId_emptyBridgeId_shouldThrowException() throws IOException {
        // Arrange
        when(hueApiService.getConfig()).thenReturn(configCall);
        when(configCall.execute()).thenReturn(Response.success(configResponse));
        when(configResponse.path("bridgeid")).thenReturn(configResponse);
        when(configResponse.asText()).thenReturn("");

        // Act & Assert
        assertThrows(HueConnectionException.class, () -> defaultHueBridge.getBridgeId());
    }

    @Test
    void getBridgeId_unsuccessfulResponse_shouldThrowException() throws IOException {
        // Arrange
        when(hueApiService.getConfig()).thenReturn(configCall);
        ResponseBody errorResponseBody =
                ResponseBody.create(
                        MediaType.parse("application/json"), "{\"error\":\"Not Found\"}");
        when(configCall.execute()).thenReturn(Response.error(404, errorResponseBody));

        // Act & Assert
        assertThrows(HueConnectionException.class, () -> defaultHueBridge.getBridgeId());
    }

    @Test
    void getBridgeId_ioException_shouldThrowException() throws IOException {
        // Arrange
        when(hueApiService.getConfig()).thenReturn(configCall);
        when(configCall.execute()).thenThrow(new IOException("Network error"));

        // Act & Assert
        assertThrows(HueConnectionException.class, () -> defaultHueBridge.getBridgeId());
    }

    @Test
    void authenticate_withApplicationName_successful()
            throws IOException, HueAuthenticationException {
        // Arrange
        when(objectMapper.createObjectNode()).thenReturn(requestBody);
        when(requestBody.put("devicetype", APPLICATION_NAME)).thenReturn(requestBody);
        when(hueApiService.authenticate(requestBody)).thenReturn(authCall);

        JsonNode successResponse = mock(JsonNode.class);
        when(successResponse.get("success")).thenReturn(successNode);
        when(successNode.get("username")).thenReturn(successNode);
        when(successNode.asText()).thenReturn(USERNAME);

        when(authCall.execute()).thenReturn(Response.success(List.of(successResponse)));

        // Act
        BridgeAuthentication result = defaultHueBridge.authenticate(APPLICATION_NAME);

        // Assert
        assertNotNull(result);
        assertEquals(USERNAME, result.username());
        assertNull(result.clientKey());
    }

    @Test
    void authenticate_withApplicationName_errorResponse() throws IOException {
        // Arrange
        when(objectMapper.createObjectNode()).thenReturn(requestBody);
        when(requestBody.put("devicetype", APPLICATION_NAME)).thenReturn(requestBody);
        when(hueApiService.authenticate(requestBody)).thenReturn(authCall);

        JsonNode errorResponse = mock(JsonNode.class);
        JsonNode errorNode = mock(JsonNode.class);
        JsonNode descriptionNode = mock(JsonNode.class);

        when(errorResponse.has("error")).thenReturn(true);
        when(errorResponse.get("error")).thenReturn(errorNode);
        when(errorNode.get("description")).thenReturn(descriptionNode);
        when(descriptionNode.asText()).thenReturn("Link button not pressed");

        when(authCall.execute()).thenReturn(Response.success(List.of(errorResponse)));

        // Act & Assert
        assertThrows(
                HueAuthenticationException.class,
                () -> defaultHueBridge.authenticate(APPLICATION_NAME));
    }

    @Test
    void authenticate_withApplicationName_ioException() throws IOException {
        // Arrange
        when(objectMapper.createObjectNode()).thenReturn(requestBody);
        when(requestBody.put("devicetype", APPLICATION_NAME)).thenReturn(requestBody);
        when(hueApiService.authenticate(requestBody)).thenReturn(authCall);
        when(authCall.execute()).thenThrow(new IOException("Network error"));

        // Act & Assert
        assertThrows(
                HueAuthenticationException.class,
                () -> defaultHueBridge.authenticate(APPLICATION_NAME));
    }

    @Test
    void authenticate_withCredentials_shouldReturnAuthenticatedBridge() {
        // Arrange
        BridgeAuthentication credentials = new BridgeAuthentication(USERNAME, null);
        when(authenticatedBridgeFactory.create(IP_ADDRESS, credentials))
                .thenReturn(authenticatedBridge);

        // Act
        AuthenticatedBridge result = defaultHueBridge.authenticate(credentials);

        // Assert
        assertSame(authenticatedBridge, result);
        verify(authenticatedBridgeFactory).create(IP_ADDRESS, credentials);
    }
}
