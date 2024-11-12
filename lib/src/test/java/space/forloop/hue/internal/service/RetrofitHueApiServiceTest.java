package space.forloop.hue.internal.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.JsonNode;

import retrofit2.Call;
import retrofit2.Retrofit;

@ExtendWith(MockitoExtension.class)
class RetrofitHueApiServiceTest {

    private static final String IP_ADDRESS = "192.168.1.100";
    private static final String USERNAME = "testUser";
    private static final String LIGHT_ID = "1";

    @Mock private Retrofit.Builder retrofitBuilder;

    @Mock private Retrofit retrofit;

    @Mock private HueApiService delegateService;

    @Mock private Call<JsonNode> jsonNodeCall;

    @Mock private Call<List<JsonNode>> jsonNodeListCall;

    @Mock private Call<Map<String, JsonNode>> jsonNodeMapCall;

    @Mock private JsonNode requestBody;

    private RetrofitHueApiService apiService;

    @BeforeEach
    void setUp() {
        when(retrofitBuilder.baseUrl("http://" + IP_ADDRESS + "/")).thenReturn(retrofitBuilder);
        when(retrofitBuilder.build()).thenReturn(retrofit);
        when(retrofit.create(HueApiService.class)).thenReturn(delegateService);

        apiService = new RetrofitHueApiService(IP_ADDRESS, retrofitBuilder);

        // Verify the initialization chain
        verify(retrofitBuilder).baseUrl("http://" + IP_ADDRESS + "/");
        verify(retrofitBuilder).build();
        verify(retrofit).create(HueApiService.class);
    }

    @Test
    void validateConnection_shouldDelegateCall() {
        // Arrange
        when(delegateService.validateConnection()).thenReturn(jsonNodeCall);

        // Act
        Call<JsonNode> result = apiService.validateConnection();

        // Assert
        assertSame(jsonNodeCall, result);
        verify(delegateService).validateConnection();
    }

    @Test
    void getConfig_shouldDelegateCall() {
        // Arrange
        when(delegateService.getConfig()).thenReturn(jsonNodeCall);

        // Act
        Call<JsonNode> result = apiService.getConfig();

        // Assert
        assertSame(jsonNodeCall, result);
        verify(delegateService).getConfig();
    }

    @Test
    void authenticate_shouldDelegateCall() {
        // Arrange
        when(delegateService.authenticate(requestBody)).thenReturn(jsonNodeListCall);

        // Act
        Call<List<JsonNode>> result = apiService.authenticate(requestBody);

        // Assert
        assertSame(jsonNodeListCall, result);
        verify(delegateService).authenticate(requestBody);
    }

    @Test
    void getLights_shouldDelegateCall() {
        // Arrange
        when(delegateService.getLights(USERNAME)).thenReturn(jsonNodeMapCall);

        // Act
        Call<Map<String, JsonNode>> result = apiService.getLights(USERNAME);

        // Assert
        assertSame(jsonNodeMapCall, result);
        verify(delegateService).getLights(USERNAME);
    }

    @Test
    void setLightState_shouldDelegateCall() {
        // Arrange
        when(delegateService.setLightState(USERNAME, LIGHT_ID, requestBody))
                .thenReturn(jsonNodeListCall);

        // Act
        Call<List<JsonNode>> result = apiService.setLightState(USERNAME, LIGHT_ID, requestBody);

        // Assert
        assertSame(jsonNodeListCall, result);
        verify(delegateService).setLightState(USERNAME, LIGHT_ID, requestBody);
    }

    @Test
    void getLight_shouldDelegateCall() {
        // Arrange
        when(delegateService.getLight(USERNAME, LIGHT_ID)).thenReturn(jsonNodeCall);

        // Act
        Call<JsonNode> result = apiService.getLight(USERNAME, LIGHT_ID);

        // Assert
        assertSame(jsonNodeCall, result);
        verify(delegateService).getLight(USERNAME, LIGHT_ID);
    }

    @Test
    void constructor_invalidIpAddress_shouldThrowException() {
        // Arrange
        when(retrofitBuilder.baseUrl(anyString()))
                .thenThrow(new IllegalArgumentException("Invalid URL"));

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> new RetrofitHueApiService("invalid ip", retrofitBuilder));
    }

    @Test
    void constructor_nullIpAddress_shouldThrowException() {
        // Act & Assert
        assertThrows(
                NullPointerException.class, () -> new RetrofitHueApiService(null, retrofitBuilder));
    }
}
