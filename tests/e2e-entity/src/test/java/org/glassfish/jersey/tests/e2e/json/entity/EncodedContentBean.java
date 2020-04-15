/*
 * Copyright (c) 2010, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.json.entity;

import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * @author Jakub Podlesak
 */
@SuppressWarnings({"RedundantIfStatement", "StringEquality"})
@XmlRootElement
public class EncodedContentBean {

    public String one;
    public String two;

    public static Object createTestInstance() {
        EncodedContentBean instance = new EncodedContentBean();
        instance.one = "\tone\n\tbig";
        instance.two = "haf\u010C";
        return instance;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final EncodedContentBean other = (EncodedContentBean) obj;
        if (this.one != other.one && (this.one == null || !this.one.equals(other.one))) {
            return false;
        }
        if (this.two != other.two && (this.two == null || !this.two.equals(other.two))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + (this.one != null ? this.one.hashCode() : 0);
        hash = 17 * hash + (this.two != null ? this.two.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return String.format("ECB(%s, %s)", one, two);
    }

}
