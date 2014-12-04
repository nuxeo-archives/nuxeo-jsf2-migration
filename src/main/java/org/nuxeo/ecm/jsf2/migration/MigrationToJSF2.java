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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.FilenameUtils;
import org.nuxeo.ecm.jsf2.migration.api.MigrationService;
import org.nuxeo.ecm.jsf2.migration.impl.MigrationServiceImpl;

/**
 * Main class for the tool to help the migration to JSF 2.
 *
 * @since 5.9.6
 */
public class MigrationToJSF2 {

    /**
     * Command line flags
     */
    static class Flags {
        final static Option MIGRATE = new Option("m", "migration", false,
            "perform migration where possible");

        final static Option FORMAT = new Option("f", "format", false,
            "format original files");

        final static Option RECURSIVE = new Option("r", "recursive", false,
            "recursive");
    }

    /**
     * @param args Path to the directory to analyze.
     */
    public static void main(String[] args) throws Exception {

        // Parse command line
        CommandLineParser parser = new PosixParser();

        Options options = new Options();
        options.addOption(Flags.MIGRATE);
        options.addOption(Flags.FORMAT);
        options.addOption(Flags.RECURSIVE);

        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
            if (cmd.getArgs().length != 1) {
                throw new ParseException("Must specify project directory.");
            }
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            System.out.println(e.getMessage());
            formatter.printHelp("java -jar nuxeo-jsf2-migration-<version>.jar <path to project>", options);
            System.exit(-1);
        }

        // Get the parameters
        String path = cmd.getArgs()[0];
        final boolean migration = cmd.hasOption(Flags.MIGRATE.getOpt());
        final boolean format = cmd.hasOption(Flags.FORMAT.getOpt());
        boolean recursive = cmd.hasOption(Flags.RECURSIVE.getOpt());

        File file = new File(path);

        // Check if the file exists
        if (!file.exists()) {
            System.out.println("The file does not exist");
            return;
        }
        final boolean isDirectory = file.isDirectory();
        if (recursive && !isDirectory) {
            System.out.println("The file is not a directory");
            return;
        }
        if (!isDirectory) {
            if (!isValidXHTMLFile(path)) {
                System.out.println("The specified file is not xhtml file.");
                return;
            }
            processSingleXHTMLFile(file, migration, format);
        } else if (!recursive) {
            if (!isValidProjectDirectory(path)) {
                System.out.println("The specified directory is not a valid project directory.");
                return;
            }
            processDirectory(file.getAbsolutePath(), migration, format);
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
        return processAnalyze(directory, migration, format, start,
                listXHTMLFiles);
    }

    private static boolean processAnalyze(String directory, boolean migration,
            boolean format, long start,
            List<File> listXHTMLFiles) {
        // Generate the report
        File report = new File(directory + "/report.txt");
        MigrationService migrationService = new MigrationServiceImpl();
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

    private static boolean processSingleXHTMLFile(File file, boolean migration, boolean format) {
        return processAnalyze(file.getParent(), migration, format, System.currentTimeMillis(), Arrays.asList(new File[] {file}));
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

    private static boolean isValidXHTMLFile(String path) {
        return FilenameUtils.getExtension(path).equals("xhtml");
    }
}
