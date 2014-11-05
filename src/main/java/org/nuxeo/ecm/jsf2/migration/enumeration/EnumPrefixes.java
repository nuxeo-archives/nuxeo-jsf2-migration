/*
 * (C) Copyright 2014 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     <a href="mailto:glefevre@nuxeo.com">Gildas</a>
 */
package org.nuxeo.ecm.jsf2.migration.enumeration;

import org.apache.commons.lang.StringUtils;

/**
 * Enumeration of all the prefixes and the namespaces associated.
 *
 * @since 5.9.6
 */
public enum EnumPrefixes {

    C("c", "http://java.sun.com/jstl/core"),
    //
    H("h", "http://java.sun.com/jsf/html"),
    //
    F("f", "http://java.sun.com/jsf/core"),
    //
    UI("ui", "http://java.sun.com/jsf/facelets"),
    //
    FN("fn", "http://java.sun.com/jsp/jstl/functions"),
    //
    A4J("a4j", "http://richfaces.org/a4j"),
    //
    RICH("rich", "http://richfaces.org/rich"),
    //
    A4F("a4f", "https://ajax4jsf.dev.java.net/ajax"),
    //
    NXL("nxl", "http://nuxeo.org/nxforms/layout"),
    //
    NXTHEMES("nxthemes", "http://nuxeo.org/nxthemes"),
    //
    NXU("nxu", "http://nuxeo.org/nxweb/util"),
    //
    NXH("nxh", "http://nuxeo.org/nxweb/html"),
    //
    NXDIR("nxdir", "http://nuxeo.org/nxdirectory"),
    //
    NXD("nxd", "http://nuxeo.org/nxweb/document"),
    //
    NXP("nxp", "http://nuxeo.org/nxweb/pdf"),
    //
    NXA4J("nxa4j", "http://nuxeo.org/nxweb/a4j"),
    //
    UNKNOWN("unknown", null);

    private String prefix;

    private String namespace;

    /**
     * @param prefix
     * @param namespace
     */
    private EnumPrefixes(String prefix, String namespace) {
        this.prefix = prefix;
        this.namespace = namespace;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getNamespace() {
        return namespace;
    }

    public static EnumPrefixes getPrefix(String prefixName) {
        for (EnumPrefixes enumPrefix : values()) {
            if (StringUtils.equals(prefixName, enumPrefix.prefix)) {
                return enumPrefix;
            }
        }

        return UNKNOWN;
    }
}
