/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.integration.j441.one;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

import javax.servlet.ServletContext;

/**
 * JAX-RS provider added just to make sure the application
 * deploys fine. Since JAX-RS providers get initialized
 * at Jersey bootstrapping phase, we would get a deploy
 * error if something went wrong.
 *
 * @author Jakub Podlesak
 */
public class CustomExceptionMapper implements ExceptionMapper<Exception> {

    @Context
    private ServletContext sc;

    public Response toResponse(final Exception ex) {
        return Response.status(200).entity(sc.getContextPath()).build();
    }
}
