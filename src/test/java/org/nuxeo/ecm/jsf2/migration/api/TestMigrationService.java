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
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

import org.dom4j.DocumentException;
import org.jaxen.JaxenException;
import org.junit.Test;
import org.nuxeo.ecm.jsf2.migration.enumeration.EnumTypeMigration;
import org.nuxeo.ecm.jsf2.migration.impl.MigrationServiceImpl;
import org.nuxeo.ecm.jsf2.migration.report.FileReport;

/**
 * Test case of the migration to JSF2.
 *
 * @since 5.9.6
 */
public class TestMigrationService {

    private static final String TEMPLATE_WITH_MIGRATIONS = "template_with_migrations.xhtml";

    private static final String TEMPLATE_PREFIX_UNBOUND = "template_prefix_unbound.xhtml";

    private static final String TEMPLATE_WRONG_NAMESPACE = "template_wrong_namespace.xhtml";

    private static final String TEMPLATE_NOTHING_TO_MIGRATE = "template_nothing_to_migrate.xhtml";

    private MigrationService migrationService = new MigrationServiceImpl();

    @Test
    public void testAnalyzeWithNoMigration()
            throws JaxenException, DocumentException {

        FileReport report = loadTemplateAndAnalyzeFile(TEMPLATE_NOTHING_TO_MIGRATE, false);

        // Check the result
        assertEquals(0, report.getListMigration().size());
        assertEquals(0, report.getListParams().size());
    }

    @Test
    public void testAnalyzeWithWrongNamespace()
            throws JaxenException, DocumentException {

        FileReport report = loadTemplateAndAnalyzeFile(TEMPLATE_WRONG_NAMESPACE, false);

        // Check the result
        assertEquals(1, report.getListMigration().size());
        assertEquals(1, report.getListParams().size());
        assertTrue(report.getListMigration().containsKey(EnumTypeMigration.NAMESPACE_RULE_1));
    }

    @Test
    public void testAnalyzeWithUnboundPrefix()
            throws JaxenException, DocumentException {
        FileReport report = loadTemplateAndAnalyzeFile(TEMPLATE_PREFIX_UNBOUND, false);

        // Check the result
        assertEquals(1, report.getListMigration().size());
        assertEquals(1, report.getListParams().size());
        assertTrue(report.getListMigration().containsKey(EnumTypeMigration.ERROR_READING_DOCUMENT));
    }

    @Test
    public void testAnalyzeWithManyMigrations()
            throws JaxenException, DocumentException {
        FileReport report = loadTemplateAndAnalyzeFile(TEMPLATE_WITH_MIGRATIONS, false);

        // Check the result
        assertEquals(5, report.getListMigration().size());
        assertEquals(5, report.getListParams().size());
        assertTrue(report.getListMigration().containsKey(EnumTypeMigration.NAMESPACE_RULE_1));
        assertTrue(report.getListMigration().containsKey(EnumTypeMigration.A4J_RERENDER_RULE));
        assertTrue(report.getListMigration().containsKey(EnumTypeMigration.A4J_ACTIONPARAM_RULE));
        assertTrue(report.getListMigration().containsKey(EnumTypeMigration.RICH_SUGGESTIONBOX_RULE));
        assertTrue(report.getListMigration().containsKey(EnumTypeMigration.A4J_FORM_RULE));
    }

    @Test
    public void testAutoMigration()
            throws Exception {
        loadTemplateAndAnalyzeFile(TEMPLATE_WITH_MIGRATIONS, true);
        loadTemplateAndAnalyzeFile(TEMPLATE_WRONG_NAMESPACE, true);
        loadTemplateAndAnalyzeFile(TEMPLATE_NOTHING_TO_MIGRATE, true);
        // Check the content of the generated files
        compareContentMigratedFile(TEMPLATE_WITH_MIGRATIONS);
        compareContentMigratedFile(TEMPLATE_WRONG_NAMESPACE);
        // Check that no files have been created
        URL urlFileNotPresent = Thread.currentThread().getContextClassLoader().getResource(
                TEMPLATE_NOTHING_TO_MIGRATE + ".migrated");
        assertNull(urlFileNotPresent);
    }

    private FileReport loadTemplateAndAnalyzeFile(String templateName, boolean doMigration)
            throws DocumentException, JaxenException {
        URL url = Thread.currentThread().getContextClassLoader().getResource(
                templateName);
        File template = new File(url.getPath());

        return migrationService.analyzeFile(template, doMigration, false);
    }

    private void compareContentMigratedFile(String templateName)
            throws FileNotFoundException, IOException {
        // Get the generated file
        URL urlFileGenerated = Thread.currentThread().getContextClassLoader().getResource(
                templateName + ".migrated");
        File fileGenerated = new File(urlFileGenerated.getPath());
        // Get the expected file
        URL urlFileExpected = Thread.currentThread().getContextClassLoader().getResource(
                "expectedFiles/" + templateName + ".expected");
        File fileExpected = new File(urlFileExpected.getPath());

        // Get the content of the two files
        String contentGenerated = deserializeString(fileGenerated);
        String contentExpected = deserializeString(fileExpected);

        assertEquals(contentExpected, contentGenerated);
        // Remove the generated file
        fileGenerated.delete();
    }

    /**
     * Load a text file contents as a <code>String<code>.
     * This method does not perform enconding conversions
     *
     * @param file The input file
     * @return The file contents as a <code>String</code>
     * @exception IOException IO Error
     */
    public static String deserializeString(File file)
            throws IOException {
        int len;
        char[] chr = new char[4096];
        final StringBuffer buffer = new StringBuffer();
        final FileReader reader = new FileReader(file);
        try {
            while ((len = reader.read(chr)) > 0) {
                buffer.append(chr, 0, len);
            }
        } finally {
            reader.close();
        }
        return buffer.toString();
    }
}
