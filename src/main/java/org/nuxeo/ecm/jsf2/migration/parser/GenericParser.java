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
package org.nuxeo.ecm.jsf2.migration.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.XPath;
import org.jaxen.dom4j.Dom4jXPath;
import org.nuxeo.ecm.jsf2.migration.enumeration.EnumPrefixes;
import org.nuxeo.ecm.jsf2.migration.enumeration.EnumTypeMigration;
import org.nuxeo.ecm.jsf2.migration.report.FileReport;

/**
 * A generic parser only looking if an element is present in the file. If the
 * migration is activated, it replaces the element by the JSF2 compatible one.
 *
 * @since 5.9.6
 */
public class GenericParser implements
        RuleParser {

    protected final String patternPrefix = "/[a-zAZ0-9]+:[a-zAZ0-9]+";

    protected EnumTypeMigration rule;

    protected String xpath;

    protected boolean doMigration;

    protected List<Node> listElementsToMigrate;

    @Override
    public void init(EnumTypeMigration rule, boolean doMigration) {
        xpath = rule.getXPath();
        this.rule = rule;
        this.doMigration = doMigration;
        listElementsToMigrate = new ArrayList<Node>();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void parse(Document input, FileReport report) throws Exception {
        XPath xpathExpr = new Dom4jXPath(xpath);

        // Check if a namespace is needed
        List<String> prefixesInXpath = getPrefix(xpath);
        if (prefixesInXpath.size() > 0) {
            for (String prefixInXpath : prefixesInXpath) {
                // Check if the prefix is in the list of prefixes
                EnumPrefixes enumPrefix = EnumPrefixes.getPrefix(prefixInXpath);
                if (enumPrefix != EnumPrefixes.UNKNOWN) {
                    Namespace namespace = input.getRootElement().getNamespaceForPrefix(prefixInXpath);
                    // If the namespace is not present in the root element, we
                    // use the one defined in the enum to avoid errors while
                    // executing the XPath expression.
                    // Specific rules are used to check the validity of the
                    // namespaces
                    String nsURI = namespace != null ? namespace.getURI() : enumPrefix.getNamespace();
                    SimpleNamespaceContext nsContext = new SimpleNamespaceContext();
                    nsContext.addNamespace(prefixInXpath,nsURI);
                    xpathExpr.setNamespaceContext(nsContext);
                } else {
                    // Add an error in the file report for the unknown namespace
                    report.getListMigration().put(EnumTypeMigration.NAMESPACE_RULE_2, 1);
                    List<String> params = new ArrayList<String>();
                    params.add(prefixInXpath);
                    report.getListParams().put(EnumTypeMigration.NAMESPACE_RULE_2, params);
                }
            }
        }

        listElementsToMigrate = xpathExpr.selectNodes(input);
        if (listElementsToMigrate.size() > 0) {
            List<String> params = new ArrayList<String>();
            params.add("" + listElementsToMigrate.size());
            report.getListParams().put(rule, params);
            report.getListMigration().put(rule, listElementsToMigrate.size());
        }
    }

    @Override
    public void migrate(Document input) throws Exception {
        // Migrate the elements matching the rule
        if (rule.isMigrationAuto()) {
            for (Node node : listElementsToMigrate) {
                if (!StringUtils.isEmpty(rule.getNewValue())) {
                    Element element = (Element) node;
                    element.setName(rule.getNewValue());
                }
            }
        }
    }

    /**
     * If a prefix is defined in the XPath expression, it is returned.
     *
     * @param xpath XPath to check
     * @return The value of the prefix if present.
     */
    protected List<String> getPrefix(
            String xpath) {
        List<String> listPrefixes = new ArrayList<String>();

        if (!StringUtils.isEmpty(xpath)) {
            Pattern pattern = Pattern.compile(patternPrefix);
            Matcher matcher = pattern.matcher(xpath);
            while (matcher.find()) {
                String prefix = matcher.group();
                // Get only the left part to get the prefix
                prefix = prefix.split(":")[0];
                // Remove the first character which is '/'
                prefix = prefix.substring(1);
                listPrefixes.add(prefix);
            }
        }

        return listPrefixes;
    }
}
