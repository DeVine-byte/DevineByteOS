package io.devinebyte.runtime.plugin;

public interface PluginContractRegistry {
    <T> void registerContract(Class<T> contractType, T implementation);
    <T> T getContract(Class<T> contractType);
}

