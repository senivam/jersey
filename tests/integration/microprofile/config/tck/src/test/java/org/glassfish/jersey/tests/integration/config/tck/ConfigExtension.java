package org.glassfish.jersey.tests.integration.config.tck;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.arquillian.core.spi.LoadableExtension;

public class ConfigExtension implements LoadableExtension {

    public ConfigExtension() {
    }

    @Override
    @ConfigProperty
    public void register(ExtensionBuilder extensionBuilder) {

    }
}
