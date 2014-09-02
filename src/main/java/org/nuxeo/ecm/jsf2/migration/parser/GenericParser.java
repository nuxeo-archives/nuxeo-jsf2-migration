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
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.dom4j.QName;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.jaxen.JaxenException;
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

    protected List<String> listPrefixToMigrate;

    @Override
    public void init(EnumTypeMigration rule, boolean doMigration) {
        xpath = rule.getXPath();
        this.rule = rule;
        this.doMigration = doMigration;
        listElementsToMigrate = new ArrayList<Node>();
        listPrefixToMigrate = new ArrayList<String>();
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

        listElementsToMigrate = xpathExpr.selectNodes(input);
        if (listElementsToMigrate.size() > 0) {
            List<String> params = new ArrayList<String>();
            params.add("" + listElementsToMigrate.size());
            report.getListParams().put(rule, params);
            report.getListMigration().put(rule, listElementsToMigrate.size());
        }
    }

    /**
     * The method checks if the namespace used for the prefix is the correct one
     * with JSF2. If not, an error is added to the report and the "incorrect"
     * namespace is used in the XPath request in order to get properly the
     * elements matching the request.
     *
     * @param rootElement The root element of the document.
     * @param prefixToCheck The prefix to check.
     * @param report The report to add the error if needed.
     * @return The name of the namespace to use for the XPath query.
     */
    protected String checkNamespace(
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
            // Add the prefix to the list of prefix to migrate
            listPrefixToMigrate.add(prefixToCheck);
            // Use the "incorrect" namespace in the xpath query
            namespace = namespaceInDoc.getURI();
        }

        return namespace;
    }


    @Override
    public void migrate(Document input, String originalFilePath) throws Exception {
        // Migrate the namespaces
        migratePrefixesNamespace(input);
        // Migrate the elements matching the rule
        if (rule.isMigrationAuto()) {
            for (Node node : listElementsToMigrate) {
                if (!StringUtils.isEmpty(rule.getNewValue())) {
                    Element element = (Element) node;
                    element.setName(rule.getNewValue());
                }
            }
        }
        // Create a new file with the migrations
        createMigratedFile(input, originalFilePath);
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

    /**
     * Migrate the namespaces that changed with JSF2.
     *
     * @param input The DOM of the input file.
     */
    protected void migratePrefixesNamespace(Document input)
        throws JaxenException{
        Element root = input.getRootElement();
        for (String prefix : listPrefixToMigrate) {
            Namespace newNamespace = new Namespace(prefix, EnumPrefixes.getPrefix(prefix).getNamespace());
            Namespace oldNamespace = root.getNamespaceForPrefix(prefix);
            if (oldNamespace != null) {
                root.remove(oldNamespace);
            }
            root.add(newNamespace);

            // Change the name of every elements with the prefix
            StringBuilder prefixXpath = new StringBuilder(
                    "//");
            prefixXpath.append(prefix);
            prefixXpath.append(":*");
            // Create a new XPath expression, with the old namespace in order to
            // get the elements matching the expression
            XPath xpath = new Dom4jXPath(
                    prefixXpath.toString());
            SimpleNamespaceContext nc = new SimpleNamespaceContext();
            nc.addNamespace(
                    prefix,
                    oldNamespace.getURI());
            xpath.setNamespaceContext(nc);

            @SuppressWarnings("unchecked")
            List<Element> elementsToMigrate = xpath.selectNodes(input);
            for (Element element : elementsToMigrate) {
                // The namespace to change is not hold by the element but the QName
                QName qname = element.getQName();
                QName newQName = new QName(qname.getName(), newNamespace, qname.getQualifiedName());
                element.setQName(newQName);
            }
        }
    }

    /**
     * Create a file containing the migration done in the Document.
     *
     * @param input
     * @param filePath
     * @throws Exception
     */
    protected void createMigratedFile(Document input, String filePath)
        throws Exception {
        File fileMigrated = new File(filePath + ".migrated");
        fileMigrated.createNewFile();

        PrintWriter printWriter = new PrintWriter(fileMigrated);
        XMLWriter writer = new XMLWriter(printWriter, OutputFormat.createPrettyPrint());
        writer.write(input);

        printWriter.close();
    }
}
