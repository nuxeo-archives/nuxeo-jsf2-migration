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
package org.nuxeo.ecm.jsf2.migration;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.nuxeo.ecm.jsf2.migration.api.MigrationService;
import org.nuxeo.ecm.jsf2.migration.impl.MigrationServiceImpl;

/**
 * Main class for the tool to help the migration to JSF 2.
 *
 * @since 5.9.6
 */
public class MigrationToJSF2 {

    /**
     * @param args Path to the directory to analyze.
     */
    public static void main(String[] args) {
        // Check the arguments in parameter
        if (args.length == 0) {
            System.out.println("Usage : java -jar <path to project> <auto migration> <format original files>");
            return;
        }
        // Get the parameters
        String path = args[0];
        boolean migration = args.length > 1 ? Boolean.parseBoolean(args[1])
                : false;
        boolean format = args.length > 2 ? Boolean.parseBoolean(args[2])
                : false;
        File directory = new File(path);

        // Check if the directory exists
        if (!directory.exists() || !directory.isDirectory()) {
            System.out.println("The directory does not exist or is not a directory");
            return;
        }

        long start = System.currentTimeMillis();
        MigrationService migrationService = new MigrationServiceImpl();

        // Check that a proper directory has been defined
        String pathXhtmlRootDirectory = String.format(
                "%s/src/main/resources/web/nuxeo.war/",
                directory.getAbsolutePath());
        File xhtmlRootDirectory = new File(pathXhtmlRootDirectory);
        if (!xhtmlRootDirectory.exists()) {
            System.out.println("The specified directory is not a valid project directory.");
        }

        // Parse all files in the project directory to get all the XHTML files
        List<File> listXHTMLFiles = migrationService.getAllXhtmlFiles(xhtmlRootDirectory);
        // Generate the report
        File report = new File(path + "/report.txt");
        try {
            migrationService.analyzeProject(report, listXHTMLFiles, migration,
                    format);
        } catch (IOException ex) {
            System.out.println(String.format(
                    "Error while generating the report : %s", ex.getMessage()));
        }

        long timeElapsed = System.currentTimeMillis() - start;
        System.out.println(String.format("The analyze is done in %d ms",
                Long.valueOf(timeElapsed)));
    }
}
