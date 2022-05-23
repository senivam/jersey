/*
 * Copyright (c) 2022 Oracle and/or its affiliates. All rights reserved.
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

open module org.glassfish.jersey.core.common {
    requires jakarta.inject;
    requires jakarta.ws.rs;
    requires jakarta.xml.bind;
    requires jakarta.activation;
    requires jakarta.annotation;

    requires java.logging;
    requires java.desktop;

    requires junit;
    requires org.hamcrest;
    requires net.bytebuddy;
    requires org.mockito;

    provides jakarta.ws.rs.ext.RuntimeDelegate
            with org.glassfish.jersey.internal.RuntimeDelegateImpl;
    provides org.glassfish.jersey.internal.spi.AutoDiscoverable
            with org.glassfish.jersey.logging.LoggingFeatureAutoDiscoverable,
                    org.glassfish.jersey.internal.config.ExternalPropertiesAutoDiscoverable;
}