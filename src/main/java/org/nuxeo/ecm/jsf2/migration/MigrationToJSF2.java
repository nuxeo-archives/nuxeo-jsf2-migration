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
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
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
    public static void main(String[] args) throws IOException {
        // Check the arguments in parameter
        if (args.length == 0) {
            System.out.println("Usage : java -jar nuxeo-jsf2-migration-<version>.jar <path to project> <auto migration> <format original files> <recursive>");
            return;
        }
        // Get the parameters
        String path = args[0];
        final boolean migration = args.length > 1 && Boolean.parseBoolean(args[1]);
        final boolean format = args.length > 2 && Boolean.parseBoolean(args[2]);
        boolean recursive = args.length > 3 && Boolean.parseBoolean(args[3]);

        File directory = new File(path);

        // Check if the directory exists
        if (!directory.exists() || !directory.isDirectory()) {
            System.out.println("The directory does not exist or is not a directory");
            return;
        }

        if (!recursive) {
            if (!isValidProjectDirectory(path)) {
                System.out.println("The specified directory is not a valid project directory.");
                return;
            }
            processDirectory(directory.getAbsolutePath(), migration, format);
        } else {
            Path startingDir = Paths.get(path);
            Files.walkFileTree(startingDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir,
                    BasicFileAttributes attrs) throws IOException {
                    return processDirectory(dir.toFile().getPath(),
                        migration, format) ?
                        FileVisitResult.SKIP_SUBTREE : FileVisitResult.CONTINUE;
                }
            });
        }
    }

    private static boolean processDirectory(String directory, boolean migration, boolean format) {
        if (!isValidProjectDirectory(directory)) {
            return false;
        }
        System.out.println(String.format("Analyzing %s", directory));
        long start = System.currentTimeMillis();
        MigrationService migrationService = new MigrationServiceImpl();

        // Parse all files in the project directory to get all the XHTML files
        List<File> listXHTMLFiles = migrationService.getAllXhtmlFiles(getXHTMLRootDirectory(directory));
        // Generate the report
        File report = new File(directory + "/report.txt");
        try {
            migrationService.analyzeProject(report, listXHTMLFiles, migration,
                format);
        } catch (IOException ex) {
            System.out.println(String.format(
                "Error while generating the report : %s", ex.getMessage()));
        }

        long timeElapsed = System.currentTimeMillis() - start;
        System.out.println(String.format("The analyze is done in %d ms", timeElapsed));

        return true;
    }

    private static File getXHTMLRootDirectory(String directory) {
        String pathXhtmlRootDirectory = String.format(
            "%s/src/main/resources/web/nuxeo.war/",
            directory);
        return new File(pathXhtmlRootDirectory);
    }

    private static boolean isValidProjectDirectory(String directory) {
        File xhtmlRootDirectory = getXHTMLRootDirectory(directory);
        return xhtmlRootDirectory.exists();
    }
}
