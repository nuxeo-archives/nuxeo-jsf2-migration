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

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.dom4j.DocumentException;
import org.jaxen.JaxenException;
import org.nuxeo.ecm.jsf2.migration.enumeration.EnumTypeMigration;
import org.nuxeo.ecm.jsf2.migration.report.FileReport;

/**
 * API to help the migration to JSF 2.
 *
 * @since 5.9.6
 */
public interface MigrationService {

    /**
     * Get all the XHTML files in the project folder.
     *
     * @param root The root of the directory to search.
     * @return A list of XHTML files
     */
    public List<File> getAllXhtmlFiles(File root);

    /**
     * Analyze the project to generate the report and eventually do the
     * migration automatically (if possible).
     *
     * @param report The report file which will contain the report.
     * @param listFiles The list of files to analyze.
     * @param doMigration Do the automatic migration if allowed by the rules.
     * @param format Before migrating the file, do a format of the original
     *            files in order to have the same format for the two files
     * @throws IOException
     */
    public void analyzeProject(File report, List<File> listFiles,
            boolean doMigration, boolean format) throws IOException;

    /**
     * Analyze an XHTML file for the action to be done for the migration.
     *
     * @param file The file to analyze.
     * @param doMigration Automatically do the migration in the file if
     *            possible.
     * @param format Before migrating the file, do a format of the original
     *            files in order to have the same format for the two files
     * @return A FileReport object containing the action to be done.
     */
    public FileReport analyzeFile(File file, boolean doMigration, boolean format)
            throws JaxenException, DocumentException;

    /**
     * Analyze an XHTML file for the action to be done for the migration but
     * only for a specific list of rules.
     *
     * @param file The file to analyze.
     * @param listRules The list of rules to execute.
     * @param doMigration Automatically do the migration in the file if
     *            possible.
     * @param format Before migrating the file, do a format of the original
     *            files in order to have the same format for the two files
     * @return A FileReport object containing the action to be done.
     */
    public FileReport analyzeFileForRule(File file,
            List<EnumTypeMigration> listRules, boolean doMigration,
            boolean format) throws JaxenException, DocumentException;

}
