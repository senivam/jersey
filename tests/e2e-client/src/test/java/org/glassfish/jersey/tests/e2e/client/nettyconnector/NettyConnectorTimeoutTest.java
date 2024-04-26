/*
 * Copyright (c) 2024 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.client.nettyconnector;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.netty.connector.NettyConnectorProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class NettyConnectorTimeoutTest {

    private static Server server;

    private static final Integer portNumber = 9997;

    private static final Integer timeOutValue = 9997;

    @BeforeAll
    static void serverStart() throws Exception {
        server = new Server(portNumber);
        server.setHandler(new TimeoutTestHandler());
        server.start();
    }

    private static Client client;
    @BeforeEach
    void beforeEach() {
        final ClientConfig config = new ClientConfig();
        this.configureClient(config);
        client = ClientBuilder.newClient(config);
    }

    private Client client() {
        return client;
    }

    public WebTarget target(String path) {
        return client().target(String.format("http://localhost:%d", portNumber)).path(path);
    }
    protected void configureClient(ClientConfig config) {
        config.property(ClientProperties.READ_TIMEOUT, timeOutValue)
                .connectorProvider(new NettyConnectorProvider());
    }

    private static final String RESOURCE_PATH = "timeout";

    @Test
    public void testNettyConnectorTimeout() {

        final Response response =  target(RESOURCE_PATH).queryParam("number", 1).request().get();
        assertEquals(200, response.getStatus());
        assertThrows(ProcessingException.class, () -> target(RESOURCE_PATH).request().get());
    }

    static class TimeoutTestHandler extends AbstractHandler {

        @Override
        public void handle(String s, Request baseRequest,
                           HttpServletRequest request,
                           HttpServletResponse response) throws IOException, ServletException {

            final String parameterValue = baseRequest.getParameter("number");
            if (parameterValue != null && parameterValue.equals("1")) {
                response.getWriter().println();
                response.flushBuffer();
                baseRequest.setHandled(true);

            } else {
                request.getReader().lines().forEach(System.out::println);
                while (true) {
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }



        }
    }

}
