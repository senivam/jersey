package org.glassfish.jersey.jetty.connector;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpClientTransport;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.http2.client.http.HttpClientTransportOverHTTP2;

public class JettyHttp2ClientSupplier implements JettyHttpClientContract<HttpClient> {
    private final HttpClient http2Client;

    /**
     * default Http2Client created for the supplier.
     */
    public JettyHttp2ClientSupplier() {
        this(createHttp2Client());
    }
    /**
     * supplier for the {@code HttpClient} with {@code HttpClientTransportOverHTTP2} to be optionally registered
     * to a {@link org.glassfish.jersey.client.ClientConfig}
     * @param http2Client a HttpClient to be supplied when {@link JettyConnector#getHttpClient()} is called.
     */
    public JettyHttp2ClientSupplier(HttpClient http2Client) {
        this.http2Client = http2Client;
    }

    private static final HttpClient createHttp2Client() {
        final HttpClientTransport transport =  new HttpClientTransportOverHTTP2(new HTTP2Client());
        return new HttpClient(transport);
    }

    @Override
    public HttpClient getHttpClient() {
        return http2Client;
    }
}