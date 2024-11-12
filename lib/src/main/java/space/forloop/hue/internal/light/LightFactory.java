package space.forloop.hue.internal.light;

import com.google.inject.assistedinject.Assisted;

import space.forloop.hue.api.Light;

public interface LightFactory {
    Light create(
            @Assisted("id") String id,
            @Assisted("username") String username,
            @Assisted("ipAddress") String ipAddress);
}
