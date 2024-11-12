package space.forloop.hue.internal.service;

public interface HueApiServiceFactory {
    HueApiService create(String ipAddress);
}
