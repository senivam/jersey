/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.jersey.tests.integration.jersey1829;

import jakarta.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.external.ExternalTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests JERSEY issue 1744. Custom status reason phrase returned from the resource method was not propagated out of the
 * servlet container.
 *
 * @author Miroslav Fuksa
 */
public class ApplicationHandlerITCase extends JerseyTest {

    @Override
    protected ResourceConfig configure() {
        return new ResourceConfig(Jersey1829.class);
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new ExternalTestContainerFactory();
    }

    @Test
    public void testCustomResponse428() {
        final Response response = target().path("resource/428").request().get();
        Assert.assertEquals(428, response.getStatusInfo().getStatusCode());
        Assert.assertEquals("my-phrase", response.getStatusInfo().getReasonPhrase());
    }

    @Test
    public void testCustomResponse428WithEntity() {
        final Response response = target().path("resource/428-entity").request().get();
        Assert.assertEquals(428, response.getStatusInfo().getStatusCode());
        Assert.assertEquals("my-phrase", response.getStatusInfo().getReasonPhrase());

    }
}
