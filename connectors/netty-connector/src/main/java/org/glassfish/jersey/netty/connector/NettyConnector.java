/*
 * Copyright (c) 2016, 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.netty.connector;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http2.Http2FrameCodecBuilder;
import io.netty.handler.proxy.ProxyHandler;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.IdentityCipherSuiteFilter;
import io.netty.handler.ssl.JdkSslContext;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.Configuration;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.client.innate.ClientProxy;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Optional;

/**
 * Netty connector implementation.
 *
 * @author Pavel Bucek
 */
class NettyConnector extends AbstractNettyConnector {


    NettyConnector(Client client) {
        super(client);
    }

    @Override
    public ChannelInitializer<?> getChannelInitializer(ClientRequest jerseyRequest, Optional<ClientProxy> handlerProxy) {
        return new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline p = ch.pipeline();

                Configuration config = jerseyRequest.getConfiguration();
                final URI requestUri = jerseyRequest.getUri();
                Integer connectTimeout = jerseyRequest.resolveProperty(ClientProperties.CONNECT_TIMEOUT, 0);

                // http proxy
                handlerProxy.ifPresent(clientProxy -> {
                    final URI u = clientProxy.uri();
                    InetSocketAddress proxyAddr = new InetSocketAddress(u.getHost(),
                            u.getPort() == -1 ? 8080 : u.getPort());
                    ProxyHandler proxy1 = createProxyHandler(jerseyRequest, proxyAddr,
                            clientProxy.userName(), clientProxy.password(), connectTimeout);
                    p.addLast(proxy1);
                });

                // Enable HTTPS if necessary.
                if ("https".equals(requestUri.getScheme())) {
                    // making client authentication optional for now; it could be extracted to configurable property
                    JdkSslContext jdkSslContext = new JdkSslContext(
                            client.getSslContext(),
                            true,
                            (Iterable) null,
                            IdentityCipherSuiteFilter.INSTANCE,
                            (ApplicationProtocolConfig) null,
                            ClientAuth.NONE,
                            (String[]) null, /* enable default protocols */
                            false /* true if the first write request shouldn't be encrypted */
                    );
                    int port = requestUri.getPort();
                    SslHandler sslHandler = jdkSslContext.newHandler(ch.alloc(), requestUri.getHost(),
                            port <= 0 ? 443 : port, executorService);
                    if (ClientProperties.getValue(config.getProperties(),
                            NettyClientProperties.ENABLE_SSL_HOSTNAME_VERIFICATION, true)) {
                        SSLEngine sslEngine = sslHandler.engine();
                        SSLParameters sslParameters = sslEngine.getSSLParameters();
                        sslParameters.setEndpointIdentificationAlgorithm("HTTPS");
                        sslEngine.setSSLParameters(sslParameters);
                    }

                    p.addLast(sslHandler);
                }

                p.addLast(new HttpClientCodec());
                p.addLast(new ChunkedWriteHandler());
                p.addLast(new HttpContentDecompressor());
            }
        };
    }
}
