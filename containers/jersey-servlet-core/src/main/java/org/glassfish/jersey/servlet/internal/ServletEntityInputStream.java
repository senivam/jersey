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

package org.glassfish.jersey.servlet.internal;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import org.glassfish.jersey.message.internal.EntityInputStream;
import org.glassfish.jersey.message.internal.EntityInputStreamListener;

import java.io.IOException;
import java.io.UncheckedIOException;

public abstract class ServletEntityInputStream extends ServletInputStream {

    protected EntityInputStream wrappedStream;


    protected abstract ServletInputStream getServletInputStream();

    @Override
    public boolean isFinished() {
        return getServletInputStream().isFinished();
    }

    @Override
    public boolean isReady() {
        return getServletInputStream().isReady();
    }

    @Override
    public void setReadListener(ReadListener readListener) {
        getServletInputStream().setReadListener(readListener);
    }

    @Override
    public int read() throws IOException {
        return getServletInputStream().read();
    }

    public EntityInputStream getWrappedStream() {
        if (wrappedStream == null) {
            wrappedStream = new EntityInputStream(getServletInputStream());
            wrappedStream.setListener(new EntityInputStreamListener() {
                @Override
                public boolean isEmpty() {
                    try {
                        return getServletInputStream().available() == 0
                                || getServletInputStream().isFinished();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }

                @Override
                public boolean isReady() {
                    return getServletInputStream().isReady();
                }
            });
        }

        return wrappedStream;
    }


}
