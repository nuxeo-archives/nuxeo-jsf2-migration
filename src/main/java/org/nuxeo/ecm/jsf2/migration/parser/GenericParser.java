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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Namespace;
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

    @Override
    public void init(EnumTypeMigration rule) {
        xpath = rule.getXPath();
        this.rule = rule;
    }

    @Override
    public void parse(Document input, FileReport report) throws Exception {
        XPath xpathExpr = new Dom4jXPath(xpath);

        // Check if a namespace is needed
        List<String> prefixesInXpath = getPrefix(xpath);
        if (prefixesInXpath.size() > 0) {
            for (String prefixInXpath : prefixesInXpath) {
                // Check if the prefix is in the list of prefixes
                if (EnumPrefixes.getPrefix(prefixInXpath) != EnumPrefixes.UNKNOWN) {
                    String namespace = checkNamespace(input.getRootElement(), prefixInXpath, report);
                    SimpleNamespaceContext nsContext = new SimpleNamespaceContext();
                    nsContext.addNamespace(prefixInXpath,namespace);
                    xpathExpr.setNamespaceContext(nsContext);
                } else {
                    // Add an error in the file report for the unknown namespace
                    report.getListMigration().put(EnumTypeMigration.NAMESPACE_RULE_3, 1);
                    List<String> params = new ArrayList<String>();
                    params.add(prefixInXpath);
                    report.getListParams().put(EnumTypeMigration.NAMESPACE_RULE_3, params);
                }
            }
        }

        @SuppressWarnings("unchecked")
        List<Element> elements = xpathExpr.selectNodes(input);
        if (elements.size() > 0) {
            List<String> params = new ArrayList<String>();
            params.add("" + elements.size());
            report.getListParams().put(rule, params);
            report.getListMigration().put(rule, elements.size());
        }
    }

    /**
     * The method checks if the namespace used of the prefix is the correct one
     * with JSF2. If not, an error is added to the report and the "incorrect"
     * namespace is used in the XPath request in order to get properly the
     * elements matching the request.
     *
     * @param rootElement The root element of the document.
     * @param prefixToCheck The prefix to check.
     * @param report The report to add the error if needed.
     * @return The name of the namespace to use for the XPath query.
     */
    private String checkNamespace(
            Element rootElement,
            String prefixToCheck,
            FileReport report) {
        String namespace = EnumPrefixes.getPrefix(prefixToCheck).getNamespace();
        Namespace namespaceInDoc = rootElement.getNamespaceForPrefix(prefixToCheck);

        // Compare the namespace in the document and the one for reference
        if (namespaceInDoc != null &&
                !StringUtils.equals(namespace, namespaceInDoc.getURI())) {
            report.getListMigration().put(EnumTypeMigration.NAMESPACE_RULE_2, 1);
            List<String> params = new ArrayList<String>();
            params.add(prefixToCheck);
            params.add(namespace);
            report.getListParams().put(EnumTypeMigration.NAMESPACE_RULE_2, params);
            // Use the "incorrect" namespace in the xpath query
            namespace = namespaceInDoc.getURI();
        }

        return namespace;
    }


    @Override
    public File migrate(File input) throws Exception {
        // TODO Auto-generated method stub
        // return null;
        throw new UnsupportedOperationException();
    }

    /**
     * If a prefix is defined in the XPath expression, it is returned.
     *
     * @param xpath XPath to check
     * @return The value of the prefix if present.
     */
    private List<String> getPrefix(
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
