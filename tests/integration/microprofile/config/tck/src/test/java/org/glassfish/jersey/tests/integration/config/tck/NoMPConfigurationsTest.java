package org.glassfish.jersey.tests.integration.config.tck;

import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.internal.config.ExternalPropertiesConfigurationFactory;
import org.glassfish.jersey.spi.ExternalConfigurationModel;
import org.junit.Before;
import org.junit.Test;

public class NoMPConfigurationsTest {

    @Before
    public void setUp() {
        System.setProperty(CommonProperties.ALLOW_SYSTEM_PROPERTIES_PROVIDER, Boolean.TRUE.toString());

        System.setProperty("jersey.config.server.provider.scanning.recursive", "PASSED");
        System.setProperty(CommonProperties.JSON_PROCESSING_FEATURE_DISABLE, "1");
        System.setProperty("jersey.config.client.readTimeout", "10");
    }


    @Test
    public void noMPConfigurationsTest() {

        final ExternalConfigurationModel model = ExternalPropertiesConfigurationFactory.getFactory().getConfig();
        System.out.println(model.as("jersey.config.client.readTimeout", String.class));

    }

}
