/*
 * Copyright (c) 2022 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.helloworld.netty;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ClientApp {
    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {
        final HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
        final HttpRequest httpRequest = HttpRequest
                .newBuilder().uri(new URI("http://localhost:8080/helloworld/"))
                .GET().build();
        final HttpResponse<String> httpResponse = httpClient.send(
                httpRequest,
                HttpResponse.BodyHandlers.ofString());

        System.out.println(String.format("Status of operation performed: %s", httpResponse.statusCode()));
        System.out.println(String.format("Protocol of operation performed: %s", httpResponse.version().name()));
        System.out.println(String.format("Content of operation performed: %s", httpResponse.body()));
    }
}
