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
package org.nuxeo.ecm.jsf2.migration.report;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;


/**
 * Enumeration of the type of migration.
 *
 * @since 5.9.6
 */
public enum EnumTypeMigration {

    A4J_FORM_RULE_1("//a4j:form","a4j","http://richfaces.org/a4j","tag.a4j.ajax.rule1.message", Severity.WARNING),
    A4J_FORM_RULE_2("//a4j:form","a4j","https://ajax4jsf.dev.java.net/ajax","tag.a4j.ajax.rule1.message", Severity.WARNING),
    NAMESPACE_RULE_1(null, null, null, "tag.namespace.rule1.message", Severity.ERROR),
    NAMESPACE_RULE_2("a4j", "a4j", "https://ajax4jsf.dev.java.net/ajax", "tag.namespace.rule2.message", Severity.INFO);

    private enum Severity {INFO, WARNING, ERROR};

    private String xpath;

    private String prefix;

    private String namespace;

    private String keyMessage;

    private Severity severityMessage;

    private EnumTypeMigration(
            String xpath,
            String prefix,
            String namespace,
            String keyMessage,
            Severity severity) {
        this.xpath = xpath;
        this.prefix = prefix;
        this.namespace = namespace;
        this.keyMessage = keyMessage;
        this.severityMessage = severity;
    }

    public String getXPath() {
        return xpath;
    }

    public String getKeyMessage() {
        return keyMessage;
    }

    public Severity getSeverity() {
        return severityMessage;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getPrefix() {
        return prefix;
    }

    /**
     * Get all the type of migration with a xpath defined.
     *
     * @return
     */
    public static List<EnumTypeMigration> getTypesMigration() {
        List<EnumTypeMigration> result = new ArrayList<EnumTypeMigration>();

        for (EnumTypeMigration type : values()) {
            if (!StringUtils.isEmpty(type.xpath)) {
                result.add(type);
            }
        }

        return result;
    }
}
