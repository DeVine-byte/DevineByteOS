package io.devinebyte.runtime.plugin;

public interface RuntimeServices {
    <T> T getService(Class<T> serviceType);
}

