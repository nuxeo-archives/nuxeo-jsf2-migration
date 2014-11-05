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

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.XPath;
import org.jaxen.dom4j.Dom4jXPath;
import org.nuxeo.ecm.jsf2.migration.enumeration.EnumPrefixes;
import org.nuxeo.ecm.jsf2.migration.enumeration.EnumTypeMigration;
import org.nuxeo.ecm.jsf2.migration.report.FileReport;

/**
 * Parser to check the namespaces
 *
 * @since 5.9.6
 */
public class NamespaceParser extends GenericParser {

    protected List<String> listPrefixToMigrate;

    @Override
    public void init(EnumTypeMigration rule, boolean doMigration) {
        this.rule = rule;
        this.doMigration = doMigration;
        listPrefixToMigrate = new ArrayList<String>();
    }

    @Override
    public void parse(Document input, FileReport report) throws Exception {
        Element rootElement = input.getRootElement();
        // For each prefix defined, we check, when it's present in the root
        // element, that the namespace is correct
        for (EnumPrefixes prefix : EnumPrefixes.values()) {
            Namespace ns = rootElement.getNamespaceForPrefix(prefix.getPrefix());
            if (ns != null
                    && !StringUtils.equals(prefix.getNamespace(), ns.getURI())) {
                listPrefixToMigrate.add(prefix.getPrefix());
                // Add the value for the report
                report.getListMigrations().put(
                        EnumTypeMigration.NAMESPACE_RULE_1, Integer.valueOf(1));
                List<String> params = new ArrayList<String>();
                params.add(prefix.getPrefix());
                params.add(prefix.getNamespace());
                report.getListParams().put(EnumTypeMigration.NAMESPACE_RULE_1,
                        params);
            }
        }
    }

    @Override
    public void migrate(Document input) throws Exception {
        Element root = input.getRootElement();
        for (String prefix : listPrefixToMigrate) {
            Namespace newNamespace = new Namespace(prefix,
                    EnumPrefixes.getPrefix(prefix).getNamespace());
            Namespace oldNamespace = root.getNamespaceForPrefix(prefix);
            if (oldNamespace != null) {
                root.remove(oldNamespace);
            }
            root.add(newNamespace);

            // Change the name of every elements with the prefix
            StringBuilder prefixXpath = new StringBuilder("//");
            prefixXpath.append(prefix);
            prefixXpath.append(":*");
            // Create a new XPath expression, with the old namespace in order
            // to
            // get the elements matching the expression
            XPath xpath = new Dom4jXPath(prefixXpath.toString());
            SimpleNamespaceContext nc = new SimpleNamespaceContext();
            nc.addNamespace(prefix, oldNamespace.getURI());
            xpath.setNamespaceContext(nc);

            @SuppressWarnings("unchecked")
            List<Element> elementsToMigrate = xpath.selectNodes(input);
            for (Element element : elementsToMigrate) {
                // The namespace to change is not hold by the element but the
                // QName
                QName qname = element.getQName();
                QName newQName = new QName(qname.getName(), newNamespace,
                        qname.getQualifiedName());
                element.setQName(newQName);
            }
        }
    }
}
