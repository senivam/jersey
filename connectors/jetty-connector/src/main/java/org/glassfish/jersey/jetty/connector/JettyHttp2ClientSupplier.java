package org.glassfish.jersey.jetty.connector;

import org.eclipse.jetty.http2.client.HTTP2Client;

public class JettyHttp2ClientSupplier implements JettyHttpClientContract<HTTP2Client> {
    private final HTTP2Client http2Client;

    /**
     * {@code HTTP2Client} supplier to be optionally registered to a {@link org.glassfish.jersey.client.ClientConfig}
     * @param http2Client a HttpClient to be supplied when {@link JettyConnector#getHttpClient()} is called.
     */
    public JettyHttp2ClientSupplier(HTTP2Client http2Client) {
        this.http2Client = http2Client;
    }

    @Override
    public HTTP2Client getHttpClient() {
        return http2Client;
    }
}