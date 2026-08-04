package io.devinebyte.runtime.event.storage;

import com.google.inject.BindingAnnotation; // ADD
import jakarta.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target; // ADD
import static java.lang.annotation.ElementType.*; // ADD

@Qualifier
@BindingAnnotation // ADD THIS
@Target({FIELD, PARAMETER, METHOD}) // ADD THIS
@Retention(RetentionPolicy.RUNTIME)
public @interface TenantBasePath {}
