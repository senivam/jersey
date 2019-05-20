package org.glassfish.jersey.internal.config;

import org.glassfish.jersey.internal.spi.AutoDiscoverable;

import javax.annotation.Priority;
import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.FeatureContext;

@ConstrainedTo(RuntimeType.CLIENT) //server is configured directly in ResourceConfig
@Priority(AutoDiscoverable.DEFAULT_PRIORITY)
public class ExternalPropertiesAutoDiscoverable implements AutoDiscoverable {
    @Override
    public void configure(FeatureContext context) {
        if (!context.getConfiguration().isRegistered(ExternalPropertiesConfigurationFactoryFeature.class)) {
            ExternalPropertiesConfigurationFactoryFeature.getFactory().configure(context);
            context.register(ExternalPropertiesConfigurationFactoryFeature.getFactory());
        }
    }
}
