package space.forloop.hue.internal.service;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import space.forloop.hue.model.BridgeDiscoveryResponse;

public interface DiscoveryApiService {
    @GET("/")
    Call<List<BridgeDiscoveryResponse>> discoverBridges();
}
