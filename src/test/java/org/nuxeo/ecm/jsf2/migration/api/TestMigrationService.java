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
package org.nuxeo.ecm.jsf2.migration.api;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.io.File;
import java.net.URL;

import org.dom4j.DocumentException;
import org.jaxen.JaxenException;
import org.junit.Test;
import org.nuxeo.ecm.jsf2.migration.enumeration.EnumTypeMigration;
import org.nuxeo.ecm.jsf2.migration.impl.MigrationServiceImpl;
import org.nuxeo.ecm.jsf2.migration.report.FileReport;

/**
 * Test case of the migration
 *
 * @since 5.9.6
 */
public class TestMigrationService {

    private MigrationService migrationService = new MigrationServiceImpl();

    @Test
    public void testAnalyzeWithNoMigration()
            throws JaxenException, DocumentException {

        FileReport report = loadTemplateAndAnalyzeFile("template_nothing_to_migrate.xhtml");

        // Check the result
        assertEquals(0, report.getListMigration().size());
        assertEquals(0, report.getListParams().size());
    }

    @Test
    public void testAnalyzeWithWrongNamespace()
            throws JaxenException, DocumentException {

        FileReport report = loadTemplateAndAnalyzeFile("template_wrong_namespace.xhtml");

        // Check the result
        assertEquals(1, report.getListMigration().size());
        assertEquals(1, report.getListParams().size());
        assertTrue(report.getListMigration().containsKey(EnumTypeMigration.NAMESPACE_RULE_2));
    }

    @Test
    public void testAnalyzeWithUnboundPrefix()
            throws JaxenException, DocumentException {
        FileReport report = loadTemplateAndAnalyzeFile("template_prefix_unbound.xhtml");

        // Check the result
        assertEquals(1, report.getListMigration().size());
        assertEquals(1, report.getListParams().size());
        assertTrue(report.getListMigration().containsKey(EnumTypeMigration.NAMESPACE_RULE_1));
    }

    private FileReport loadTemplateAndAnalyzeFile(String templateName)
            throws DocumentException, JaxenException {
        URL url = Thread.currentThread().getContextClassLoader().getResource(
                templateName);
        File template = new File(url.getPath());

        return migrationService.analyzeFile(template, false);
    }
}
