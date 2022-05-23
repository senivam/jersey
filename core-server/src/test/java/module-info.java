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

open module org.glassfish.jersey.core.server {
    requires jakarta.xml.bind;
    requires jakarta.ws.rs;
    requires jakarta.validation;
    requires jakarta.activation;

    requires org.glassfish.jersey.core.common;
    requires org.glassfish.jersey.core.client;
    requires org.glassfish.jersey.inject.hk2;
    requires jdk.httpserver;
    requires java.management;

    requires org.hamcrest;
    requires junit;

    requires jboss.vfs;
    requires jboss.logging;

    exports org.glassfish.jersey.server.model;
    exports org.glassfish.jersey.server.internal.routing;

    provides jakarta.ws.rs.container.DynamicFeature
            with org.glassfish.jersey.server.JaxRsFeatureRegistrationTest.DynamicFeatureImpl;
    provides org.glassfish.jersey.internal.spi.AutoDiscoverable
            with org.glassfish.jersey.server.AutoDiscoverableServerTest.CommonAutoDiscoverable,
                org.glassfish.jersey.server.filter.internal.ServerFiltersAutoDiscoverable;
    provides jakarta.ws.rs.ext.RuntimeDelegate
            with org.glassfish.jersey.server.internal.RuntimeDelegateImpl;
    provides org.glassfish.jersey.internal.spi.ForcedAutoDiscoverable
            with org.glassfish.jersey.server.wadl.internal.WadlAutoDiscoverable,
                    org.glassfish.jersey.server.internal.monitoring.MonitoringAutodiscoverable;
    provides org.glassfish.jersey.model.internal.spi.ParameterServiceProvider
            with org.glassfish.jersey.server.model.Parameter.ServerParameterService;
}