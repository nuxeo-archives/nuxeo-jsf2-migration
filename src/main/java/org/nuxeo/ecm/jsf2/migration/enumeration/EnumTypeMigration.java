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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.jsf2.migration.parser.GenericParser;
import org.nuxeo.ecm.jsf2.migration.parser.ReRenderParser;
import org.nuxeo.ecm.jsf2.migration.parser.RuleParser;


/**
 * Enumeration of the type of migration.
 *
 * @since 5.9.6
 */
public enum EnumTypeMigration {

    A4J_FORM_RULE("//a4j:form","a4j.ajax.rule1.message", Severity.ERROR, GenericParser.class, false),
    A4J_RERENDER_RULE("//@reRender","a4j.rerender.rule.message", Severity.WARNING, ReRenderParser.class, true, "render"),
    A4J_ACTIONPARAM_RULE("//a4j:actionparam", "a4j.actionParam.rule.message",Severity.INFO, GenericParser.class, true, "a4j:param"),
    RICH_SUGGESTIONBOX_RULE("//rich:suggestionbox", "rich.suggestionBox.rule.message",Severity.INFO, GenericParser.class, false),
    NAMESPACE_RULE_1(null, "namespace.rule1.message", Severity.ERROR, null, false),
    NAMESPACE_RULE_2(null, "namespace.rule2.message", Severity.INFO, null, true),
    NAMESPACE_RULE_3(null, "namespace.rule3.message", Severity.INFO, null, false);

    private enum Severity {INFO, WARNING, ERROR};

    // XPath used to get the elements to check
    private String xpath;

    // The key of the message stored in the properties file
    private String keyMessage;

    // The severity of the message
    private Severity severityMessage;

    // The parser used to analyze and migrate the file
    private Class parser;

    // The instance of the parser
    private RuleParser instance;

    // The migration can it be done by the parser?
    private boolean migrationAuto;

    // When an auto migration is possible, newValue contains the new value for the element
    private String newValue;

    private EnumTypeMigration(
            String xpath,
            String keyMessage,
            Severity severity,
            Class parser,
            boolean migrationAuto) {
        this.xpath = xpath;
        this.keyMessage = keyMessage;
        this.severityMessage = severity;
        this.parser = parser;
        this.migrationAuto = migrationAuto;
    }

    private EnumTypeMigration(
            String xpath,
            String keyMessage,
            Severity severity,
            Class parser,
            boolean migrationAuto,
            String newValue) {
        this.xpath = xpath;
        this.keyMessage = keyMessage;
        this.severityMessage = severity;
        this.parser = parser;
        this.migrationAuto = migrationAuto;
        this.newValue = newValue;
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

    public boolean isMigrationAuto() {
        return migrationAuto;
    }

    public String getNewValue() {
        return newValue;
    }

    public RuleParser getInstance(boolean doMigration) {
        try {
            initParser(doMigration);
            } catch (Exception ex) {
            // TODO
            }
            return instance;
    }

    /**
     * Init the parser.
     *
     * @param listPrefixes The list of prefixes defined in the contribution.
     * @throws Exception
     */
    public void initParser(
            boolean doMigration)
            throws Exception {
        if (instance == null) {
            instance = (RuleParser) parser.newInstance();
        }
        instance.init(this, doMigration);
    }

    public void resetInstance() {
        instance = null;
    }

    /**
     * Get all the type of migration with an element defined.
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
