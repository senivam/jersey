/*
 * Copyright (c) 2016, 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.helloworld.netty;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.InvocationCallback;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jetty.connector.JettyHttp2ConnectorProvider;
import org.glassfish.jersey.netty.connector.NettyConnectorProvider;
import org.glassfish.jersey.netty.connector.http2.NettyHttp2ConnectorProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.glassfish.jersey.test.netty.NettyTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.glassfish.jersey.test.util.runner.ConcurrentRunner;
import org.glassfish.jersey.test.util.runner.RunSeparately;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(ConcurrentRunner.class)
public class HelloWorldTest extends JerseyTest {

    @Override
    protected ResourceConfig configure() {
        enable(TestProperties.LOG_TRAFFIC);
        set(TestProperties.RECORD_LOG_LEVEL, 10);
        set("org.eclipse.jetty.http2", "DEBUG");
        // enable(TestProperties.DUMP_ENTITY);
        return new ResourceConfig(HelloWorldResource.class);
    }

    @Override
    protected void configureClient(ClientConfig clientConfig) {
        clientConfig.connectorProvider(new JettyHttp2ConnectorProvider());
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new NettyTestContainerFactory();
    }

    @Test
    @Ignore("not compatible with test framework (doesn't use client())")
    public void testHelloWorld() throws Exception {
        URL getUrl = UriBuilder.fromUri(getBaseUri()).path(App.ROOT_PATH).build().toURL();
        HttpURLConnection connection = (HttpURLConnection) getUrl.openConnection();
        try {
            connection.setDoOutput(true);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "text/plain");
            assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
        } finally {
            connection.disconnect();
        }
    }

    @Test
//    @Ignore
    public void testConnection() {
        Response response = target().path(App.ROOT_PATH).request("text/plain").get();
        assertEquals(200, response.getStatus());
    }

    @Test
    @Ignore
    public void testClientStringResponse() {
        String s = target().path(App.ROOT_PATH).request().get(String.class);
        assertEquals(HelloWorldResource.CLICHED_MESSAGE, s);
    }

    @Test
    @Ignore
    public void testAsyncClientRequests() throws InterruptedException {
        final int REQUESTS = 10;
        final CountDownLatch latch = new CountDownLatch(REQUESTS);
        final long tic = System.currentTimeMillis();
        for (int i = 0; i < REQUESTS; i++) {
            target().path(App.ROOT_PATH).request().async().get(new InvocationCallback<Response>() {
                @Override
                public void completed(Response response) {
                    try {
                        final String result = response.readEntity(String.class);
                        assertEquals(HelloWorldResource.CLICHED_MESSAGE, result);
                    } finally {
                        System.out.println(latch.getCount());
                        latch.countDown();
                    }
                }

                @Override
                public void failed(Throwable error) {

                    try {
                        error.printStackTrace();
                    } finally {
                        System.out.println(latch.getCount());
                        latch.countDown();
                    }
                }
            });
        }
        latch.await(10 * getAsyncTimeoutMultiplier(), TimeUnit.SECONDS);
        System.out.println(latch.getCount());
        final long toc = System.currentTimeMillis();
        Logger.getLogger(HelloWorldTest.class.getName()).info("Executed in: " + (toc - tic));
    }

    @Test
    @Ignore
    public void testHead() {
        Response response = target().path(App.ROOT_PATH).request().head();
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.TEXT_PLAIN_TYPE, response.getMediaType());
    }

    @Test
    @Ignore
    public void testFooBarOptions() {
        Response response = target().path(App.ROOT_PATH).request().header("Accept", "foo/bar").options();
        assertEquals(200, response.getStatus());
        final String allowHeader = response.getHeaderString("Allow");
        _checkAllowContent(allowHeader);
        assertEquals("foo/bar", response.getMediaType().toString());
        assertEquals(0, response.getLength());
    }

    @Test
    @Ignore
    public void testTextPlainOptions() {
        getClient().register(JettyHttp2ConnectorProvider.class);
        Response response = target().path(App.ROOT_PATH).request().header("Accept", MediaType.TEXT_PLAIN).options();
        assertEquals(200, response.getStatus());
        final String allowHeader = response.getHeaderString("Allow");
        _checkAllowContent(allowHeader);
        assertEquals(MediaType.TEXT_PLAIN_TYPE, response.getMediaType());
        final String responseBody = response.readEntity(String.class);
        _checkAllowContent(responseBody);
    }

    @Test
//    @Ignore
    public void testHttp2Support() throws URISyntaxException, IOException, InterruptedException {
        final HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
        final HttpRequest httpRequest = HttpRequest
                .newBuilder().uri(URI.create("https://127.0.0.1:8443/http2?latency=0"))
                .GET().build();
        final HttpResponse<String> httpResponse = httpClient.send(
                httpRequest, HttpResponse.BodyHandlers.ofString());
        assertTrue(httpResponse.version().equals(HttpClient.Version.HTTP_2));
        assertEquals(HelloWorldResource.CLICHED_MESSAGE, httpResponse.body());
    }

    private void _checkAllowContent(final String content) {
        assertTrue(content.contains("GET"));
        assertTrue(content.contains("HEAD"));
        assertTrue(content.contains("OPTIONS"));
    }

    @Test
    @Ignore
    public void testMissingResourceNotFound() {
        Response response;

        response = target().path(App.ROOT_PATH + "arbitrary").request().get();
        assertEquals(404, response.getStatus());

        response = target().path(App.ROOT_PATH).path("arbitrary").request().get();
        assertEquals(404, response.getStatus());
    }

    @Test
    @Ignore
    @RunSeparately
    public void testLoggingFilterClientClass() {
        Client client = client();
        client.register(CustomLoggingFilter.class).property("foo", "bar");
        CustomLoggingFilter.preFilterCalled = CustomLoggingFilter.postFilterCalled = 0;
        String s = target().path(App.ROOT_PATH).request().get(String.class);
        assertEquals(HelloWorldResource.CLICHED_MESSAGE, s);
        assertEquals(1, CustomLoggingFilter.preFilterCalled);
        assertEquals(1, CustomLoggingFilter.postFilterCalled);
    }

    @Test
    @Ignore
    @RunSeparately
    public void testLoggingFilterClientInstance() {
        Client client = client();
        client.register(new CustomLoggingFilter()).property("foo", "bar");
        CustomLoggingFilter.preFilterCalled = CustomLoggingFilter.postFilterCalled = 0;
        String s = target().path(App.ROOT_PATH).request().get(String.class);
        assertEquals(HelloWorldResource.CLICHED_MESSAGE, s);
        assertEquals(1, CustomLoggingFilter.preFilterCalled);
        assertEquals(1, CustomLoggingFilter.postFilterCalled);
    }

    @Test
    @Ignore
    @RunSeparately
    public void testLoggingFilterTargetClass() {
        WebTarget target = target().path(App.ROOT_PATH);
        target.register(CustomLoggingFilter.class).property("foo", "bar");
        CustomLoggingFilter.preFilterCalled = CustomLoggingFilter.postFilterCalled = 0;
        String s = target.request().get(String.class);
        assertEquals(HelloWorldResource.CLICHED_MESSAGE, s);
        assertEquals(1, CustomLoggingFilter.preFilterCalled);
        assertEquals(1, CustomLoggingFilter.postFilterCalled);
    }

    @Test
    @Ignore
    @RunSeparately
    public void testLoggingFilterTargetInstance() {
        WebTarget target = target().path(App.ROOT_PATH);
        target.register(new CustomLoggingFilter()).property("foo", "bar");
        CustomLoggingFilter.preFilterCalled = CustomLoggingFilter.postFilterCalled = 0;
        String s = target.request().get(String.class);
        assertEquals(HelloWorldResource.CLICHED_MESSAGE, s);
        assertEquals(1, CustomLoggingFilter.preFilterCalled);
        assertEquals(1, CustomLoggingFilter.postFilterCalled);
    }

    @Test
    @Ignore
    @RunSeparately
    public void testConfigurationUpdate() {
        Client client1 = client();
        client1.register(CustomLoggingFilter.class).property("foo", "bar");

        Client client = ClientBuilder.newClient(client1.getConfiguration());
        CustomLoggingFilter.preFilterCalled = CustomLoggingFilter.postFilterCalled = 0;
        String s = client.target(App.BASE_URI).path(App.ROOT_PATH).request().get(String.class);
        System.out.println(s);
        assertEquals(HelloWorldResource.CLICHED_MESSAGE, s);
        assertEquals(1, CustomLoggingFilter.preFilterCalled);
        assertEquals(1, CustomLoggingFilter.postFilterCalled);
    }

    @Test
    @Ignore
    @RunSeparately
    public void testQueryParameterGet() {
        String result = target().path(App.ROOT_PATH + "/query1").queryParam("test1", "expected1")
                .queryParam("test2", "expected2").request().get(String.class);
        assertEquals("expected1expected2", result);
    }

    @Test
    @Ignore
    @RunSeparately
    public void testQueryParameterPost() {
        String result = target().path(App.ROOT_PATH + "/query2").queryParam("test1", "expected1")
                .queryParam("test2", "expected2").request("text/plain").post(Entity.entity("entity", "text/plain"))
                .readEntity(String.class);
        assertEquals("entityexpected1expected2", result);
    }

}