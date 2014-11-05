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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.tree.DefaultAttribute;

/**
 * Parser for the migration of the 'reRender' parameter.
 *
 * @since 5.9.6
 */
public class ReRenderParser extends GenericParser {

    private static final String REGEX_EL = "(\\#|\\$)\\{([^}]+)\\}([^, ]*)|([^, ]+)";

    @Override
    public void migrate(Document input) throws Exception {
        // Migrate the elements matching the rule
        if (rule.isMigrationAuto()) {
            for (Node node : listElementsToMigrate) {
                // Change the name of attribute "reRender"
                Attribute attribute = createNewAttribute((Attribute) node);
                Element parentElement = node.getParent();
                parentElement.remove(node);
                parentElement.add(attribute);
            }
        }
    }

    /**
     * Create a new attribute with the new name.
     *
     * @param node
     * @return
     */
    protected Attribute createNewAttribute(Attribute originalAttribute) {
        String newName = rule.getNewValue();
        String newValue = generateNewValue(originalAttribute.getValue());
        return new DefaultAttribute(newName, newValue);
    }

    /**
     * Generate the new value for the attribute, replacing coma separators by
     * whitespace.
     *
     * @param value The original value of the attribute.
     * @return The converted value for JSF2.
     */
    protected String generateNewValue(String value) {
        String newValue = value;

        // Check if the value contains a value expression
        if (!isValueReference(value)) {
            // No value expression, the ',' are replaced by ' '
            newValue = value.replace(',', ' ');
        } else {
            Pattern pattern = Pattern.compile(REGEX_EL);
            Matcher matcher = pattern.matcher(value);
            if (matcher.groupCount() > 1) {
                StringBuilder newValueBuilder = new StringBuilder();
                while (matcher.find()) {
                    newValueBuilder.append(matcher.group());
                    newValueBuilder.append(' ');
                }
                newValue = newValueBuilder.toString();
                // Remove the last space
                newValue = newValue.substring(0, newValue.length() - 1);
            }
        }

        return newValue;
    }

    /**
     * Returns true if the specified value contains a value expression, e.g the
     * start and end of EL markers.
     *
     * @param value the value to evaluate, returns false if null
     */
    protected boolean isValueReference(String value) {
        if (value == null) {
            return false;
        }
        return value.contains("#{") && value.indexOf("#{") < value.indexOf('}')
                || value.contains("${")
                && value.indexOf("${") < value.indexOf('}');
    }
}
