package org.glassfish.jersey.innate.io;

import java.io.InputStream;

public interface ExternalStreamWrapper {

    ExternalStreamListener getListener();

    InputStream getExternalStream();

}
