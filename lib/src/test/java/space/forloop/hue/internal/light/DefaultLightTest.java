package space.forloop.hue.internal.light;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import space.forloop.hue.exception.HueException;
import space.forloop.hue.internal.service.HueApiService;
import space.forloop.hue.internal.service.HueApiServiceFactory;
import space.forloop.hue.model.LightCapabilities;
import space.forloop.hue.model.LightState;

@ExtendWith(MockitoExtension.class)
class DefaultLightTest {

    private static final String LIGHT_ID = "1";
    private static final String USERNAME = "testUser";
    private static final String IP_ADDRESS = "192.168.1.100";

    @Mock private HueApiServiceFactory apiServiceFactory;

    @Mock private HueApiService apiService;

    @Mock private ObjectMapper objectMapper;

    @Mock private Call<JsonNode> getLightCall;

    @Mock private Call<List<JsonNode>> setLightCall;

    @Mock private JsonNode lightData;

    @Mock private JsonNode stateNode;

    @Mock private JsonNode capabilitiesNode;

    private DefaultLight light;

    @BeforeEach
    void setUp() {
        when(apiServiceFactory.create(IP_ADDRESS)).thenReturn(apiService);
        light = new DefaultLight(LIGHT_ID, USERNAME, IP_ADDRESS, apiServiceFactory, objectMapper);
    }

    @Test
    void getId_shouldReturnLightId() {
        assertEquals(LIGHT_ID, light.getId());
    }

    @Test
    void getState_successfulResponse_shouldReturnLightState() throws Exception {
        // Arrange
        when(apiService.getLight(USERNAME, LIGHT_ID)).thenReturn(getLightCall);
        when(getLightCall.execute()).thenReturn(Response.success(lightData));
        when(lightData.get("state")).thenReturn(stateNode);
        when(stateNode.get("on")).thenReturn(stateNode);
        when(stateNode.asBoolean()).thenReturn(true);
        when(stateNode.has("bri")).thenReturn(true);
        when(stateNode.has("ct")).thenReturn(true);
        when(stateNode.get("bri")).thenReturn(stateNode);
        when(stateNode.get("ct")).thenReturn(stateNode);
        when(stateNode.asInt()).thenReturn(254, 370); // First for brightness, second for color temp

        // Act
        LightState state = light.getState();

        // Assert
        assertNotNull(state);
        assertTrue(state.on());
        assertEquals(254, state.brightness());
        assertEquals(370, state.colorTemperature());
    }

    @Test
    void getState_missingOptionalFields_shouldReturnPartialState() throws Exception {
        // Arrange
        when(apiService.getLight(USERNAME, LIGHT_ID)).thenReturn(getLightCall);
        when(getLightCall.execute()).thenReturn(Response.success(lightData));
        when(lightData.get("state")).thenReturn(stateNode);
        when(stateNode.get("on")).thenReturn(stateNode);
        when(stateNode.asBoolean()).thenReturn(true);
        when(stateNode.has("bri")).thenReturn(false);
        when(stateNode.has("ct")).thenReturn(false);

        // Act
        LightState state = light.getState();

        // Assert
        assertNotNull(state);
        assertTrue(state.on());
        assertNull(state.brightness());
        assertNull(state.colorTemperature());
    }

    @Test
    void getState_unsuccessfulResponse_shouldThrowException() throws Exception {
        // Arrange
        when(apiService.getLight(USERNAME, LIGHT_ID)).thenReturn(getLightCall);
        ResponseBody errorBody =
                ResponseBody.create(
                        MediaType.parse("application/json"), "{\"error\":\"Not Found\"}");
        when(getLightCall.execute()).thenReturn(Response.error(404, errorBody));

        // Act & Assert
        HueException exception = assertThrows(HueException.class, () -> light.getState());
        assertEquals("Failed to get light state. Status: 404", exception.getMessage());
    }

    @Test
    void getState_nullResponseBody_shouldThrowException() throws Exception {
        // Arrange
        when(apiService.getLight(USERNAME, LIGHT_ID)).thenReturn(getLightCall);
        when(getLightCall.execute()).thenReturn(Response.success(null));

        // Act & Assert
        HueException exception = assertThrows(HueException.class, () -> light.getState());
        assertEquals("Light data is null.", exception.getMessage());
    }

    @Test
    void getState_ioException_shouldThrowException() throws Exception {
        // Arrange
        when(apiService.getLight(USERNAME, LIGHT_ID)).thenReturn(getLightCall);
        IOException ioException = new IOException("Network error");
        when(getLightCall.execute()).thenThrow(ioException);

        // Act & Assert
        HueException exception = assertThrows(HueException.class, () -> light.getState());
        assertEquals("Failed to get light state: Network error", exception.getMessage());
        assertSame(ioException, exception.getCause());
    }

    @Test
    void setState_successfulResponse_shouldSetState() throws Exception {
        // Arrange
        LightState state = LightState.builder().on(true).brightness(254).build();
        JsonNode stateJson = mock(JsonNode.class);
        when(objectMapper.valueToTree(state)).thenReturn(stateJson);
        when(apiService.setLightState(USERNAME, LIGHT_ID, stateJson)).thenReturn(setLightCall);
        when(setLightCall.execute()).thenReturn(Response.success(List.of()));

        // Act
        light.setState(state);

        // Assert
        verify(apiService).setLightState(USERNAME, LIGHT_ID, stateJson);
        verify(setLightCall).execute();
    }

    @Test
    void setState_unsuccessfulResponse_shouldThrowException() throws Exception {
        // Arrange
        LightState state = LightState.builder().on(true).build();
        JsonNode stateJson = mock(JsonNode.class);
        when(objectMapper.valueToTree(state)).thenReturn(stateJson);
        when(apiService.setLightState(USERNAME, LIGHT_ID, stateJson)).thenReturn(setLightCall);
        ResponseBody errorBody =
                ResponseBody.create(
                        MediaType.parse("application/json"), "{\"error\":\"Bad Request\"}");
        when(setLightCall.execute()).thenReturn(Response.error(400, errorBody));

        // Act & Assert
        HueException exception = assertThrows(HueException.class, () -> light.setState(state));
        assertEquals("Failed to set light state. Status: 400", exception.getMessage());
    }

    @Test
    void setState_ioException_shouldThrowException() throws Exception {
        // Arrange
        LightState state = LightState.builder().on(true).build();
        JsonNode stateJson = mock(JsonNode.class);
        when(objectMapper.valueToTree(state)).thenReturn(stateJson);
        when(apiService.setLightState(USERNAME, LIGHT_ID, stateJson)).thenReturn(setLightCall);
        IOException ioException = new IOException("Network error");
        when(setLightCall.execute()).thenThrow(ioException);

        // Act & Assert
        HueException exception = assertThrows(HueException.class, () -> light.setState(state));
        assertEquals("Failed to set light state: Network error", exception.getMessage());
        assertSame(ioException, exception.getCause());
    }

    @Test
    void getCapabilities_successfulResponse_shouldReturnCapabilities() throws Exception {
        // Arrange
        LightCapabilities expectedCapabilities = new LightCapabilities(false, false, null, null);
        when(apiService.getLight(USERNAME, LIGHT_ID)).thenReturn(getLightCall);
        when(getLightCall.execute()).thenReturn(Response.success(lightData));
        when(lightData.get("capabilities")).thenReturn(capabilitiesNode);
        when(objectMapper.treeToValue(capabilitiesNode, LightCapabilities.class))
                .thenReturn(expectedCapabilities);

        // Act
        LightCapabilities result = light.getCapabilities();

        // Assert
        assertSame(expectedCapabilities, result);
    }

    @Test
    void getCapabilities_noCapabilitiesNode_shouldReturnNull() throws Exception {
        // Arrange
        when(apiService.getLight(USERNAME, LIGHT_ID)).thenReturn(getLightCall);
        when(getLightCall.execute()).thenReturn(Response.success(lightData));
        when(lightData.get("capabilities")).thenReturn(null);

        // Act
        LightCapabilities result = light.getCapabilities();

        // Assert
        assertNull(result);
    }

    @Test
    void getCapabilities_unsuccessfulResponse_shouldThrowException() throws Exception {
        // Arrange
        when(apiService.getLight(USERNAME, LIGHT_ID)).thenReturn(getLightCall);
        ResponseBody errorBody =
                ResponseBody.create(
                        MediaType.parse("application/json"), "{\"error\":\"Not Found\"}");
        when(getLightCall.execute()).thenReturn(Response.error(404, errorBody));

        // Act & Assert
        HueException exception = assertThrows(HueException.class, () -> light.getCapabilities());
        assertEquals("Failed to get light capabilities. Status: 404", exception.getMessage());
    }

    @Test
    void getCapabilities_nullResponseBody_shouldThrowException() throws Exception {
        // Arrange
        when(apiService.getLight(USERNAME, LIGHT_ID)).thenReturn(getLightCall);
        when(getLightCall.execute()).thenReturn(Response.success(null));

        // Act & Assert
        HueException exception = assertThrows(HueException.class, () -> light.getCapabilities());
        assertEquals("Light data is null.", exception.getMessage());
    }

    @Test
    void getCapabilities_ioException_shouldThrowException() throws Exception {
        // Arrange
        when(apiService.getLight(USERNAME, LIGHT_ID)).thenReturn(getLightCall);
        IOException ioException = new IOException("Network error");
        when(getLightCall.execute()).thenThrow(ioException);

        // Act & Assert
        HueException exception = assertThrows(HueException.class, () -> light.getCapabilities());
        assertEquals("Failed to get light capabilities: Network error", exception.getMessage());
        assertSame(ioException, exception.getCause());
    }
}
