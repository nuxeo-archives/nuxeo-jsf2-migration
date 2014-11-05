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

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.tree.DefaultAttribute;

/**
 * Parser for the migration of the 'ajaxSingle' attribute.
 *
 * @since 6.0
 */
public class AttributeAjaxSingleParser extends GenericParser {

    @Override
    public void migrate(Document input) throws Exception {
        // Migrate the elements matching the rule
        if (rule.isMigrationAuto()) {
            for (Node node : listElementsToMigrate) {
                Element element = (Element) node;
                // Remove the "ajaxSingle" attribute
                Attribute attrAS = element.attribute("ajaxSingle");
                element.remove(attrAS);
                // Add the "execute" attribute
                Attribute attribute = new DefaultAttribute("execute", "@this");
                element.add(attribute);
            }
        }
    }
}
