package io.devinebyte.runtime.config;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Singleton;
@Singleton
public final class RuntimeConfigModule {
    private RuntimeConfigModule() {}
    public static ConfigurationManager configurationManager() { 
        return new ConfigurationManager(new ObjectMapper());
    }
}
