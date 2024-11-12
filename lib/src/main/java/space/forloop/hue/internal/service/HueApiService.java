package space.forloop.hue.internal.service;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import retrofit2.Call;
import retrofit2.http.*;

public interface HueApiService {

    @GET("api")
    Call<JsonNode> validateConnection();

    @GET("api/config")
    Call<JsonNode> getConfig();

    @POST("api")
    Call<List<JsonNode>> authenticate(@Body JsonNode body);

    @GET("api/{username}/lights")
    Call<Map<String, JsonNode>> getLights(@Path("username") String username);

    @PUT("api/{username}/lights/{lightId}/state")
    Call<List<JsonNode>> setLightState(
            @Path("username") String username,
            @Path("lightId") String lightId,
            @Body JsonNode body);

    @GET("api/{username}/lights/{lightId}")
    Call<JsonNode> getLight(@Path("username") String username, @Path("lightId") String lightId);
}
