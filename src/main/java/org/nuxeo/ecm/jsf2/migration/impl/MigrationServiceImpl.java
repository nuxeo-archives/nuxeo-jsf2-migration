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
package org.nuxeo.ecm.jsf2.migration.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.jaxen.JaxenException;
import org.nuxeo.ecm.jsf2.migration.api.MigrationService;
import org.nuxeo.ecm.jsf2.migration.enumeration.EnumTypeMigration;
import org.nuxeo.ecm.jsf2.migration.parser.RuleParser;
import org.nuxeo.ecm.jsf2.migration.report.FileReport;

/**
 * Implementation of the services to help the migration to JSF 2.
 *
 * @since 5.9.6
 */
public class MigrationServiceImpl implements MigrationService {

    private static Log logger = LogFactory.getLog(MigrationServiceImpl.class);

    private static final String FILE_EXTENSION = "xhtml";

    private static final String NOTHING_MESSAGE = "file.migration.nothing.message";

    private static final String SUFFIX_DETAILED_MESSAGE = ".detailed";

    private static final String SUFFIX_SUMMARIZED_MESSAGE = ".summarized";

    private static final Set<String> listTemplatesNuxeoPlatform = new HashSet<String>();

    @Override
    public List<File> getAllXhtmlFiles(File root) {
        List<File> listFiles = new ArrayList<File>();

        // Parse all files in the directory
        for (File children : root.listFiles()) {
            if (children.isDirectory()) {
                listFiles.addAll(getAllXhtmlFiles(children));
            }
            if (children.isFile()) {
                String extension = FilenameUtils.getExtension(children.getName());
                if (FILE_EXTENSION.equals(extension)) {
                    listFiles.add(children);
                }
            }
        }

        return listFiles;
    }

    @Override
    public void analyzeProject(File report, List<File> listFiles,
            boolean doMigration, boolean format) throws IOException {
        // If the file does not exist, it is created
        if (!report.exists()) {
            report.createNewFile();
        }

        PrintWriter printWriter = new PrintWriter(report);
        printWriter.append("##############################\n");
        printWriter.append("# Migration report for JSF 2 #\n");
        printWriter.append("##############################\n\n");

        List<FileReport> listReports = new ArrayList<FileReport>();
        for (File file : listFiles) {
            try {
                listReports.add(analyzeFile(file, doMigration, format));
            } catch (DocumentException ex) {
                System.out.println(String.format(
                        "Error while reading file %s.", file.getName()));
                System.out.println(ex.getMessage());
            } catch (JaxenException jex) {
                System.out.println(String.format(
                        "Error while parsing file %s.", file.getName()));
                System.out.println(jex.getMessage());
            }
        }

        // Generate the content report
        generateReport(listReports, printWriter);

        printWriter.close();
    }

    /**
     * Method to generate the final report.
     *
     * @param listResults List of result of analyze of each file.
     * @param report The text output stream of the report to complete.
     */
    private void generateReport(List<FileReport> listResults, PrintWriter report)
            throws IOException {
        // Load the file containing the messages to display in the report
        Properties reportProp = new Properties();
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(
                "report.properties");
        reportProp.load(is);

        generateSummaryReport(listResults, report, reportProp);

        generateDetailedReport(listResults, report, reportProp);
    }

    /**
     * Generate the summary part of the report.
     *
     * @param listResults List of result of analyze of each file.
     * @param report The text output stream of the report to complete.
     * @param reportProp Properties file containing the message to display.
     * @throws IOException
     */
    private void generateSummaryReport(List<FileReport> listResults,
            PrintWriter report, Properties reportProp) throws IOException {

        report.append("Summary\n");
        report.append("#######\n");
        report.append("Number of files analyzed : " + listResults.size() + "\n");

        for (EnumTypeMigration type : EnumTypeMigration.values()) {
            int occurence = 0;
            for (FileReport fileReport : listResults) {
                if (fileReport.getListMigrations().containsKey(type)) {
                    occurence += fileReport.getListMigrations().get(type);
                }
            }

            // If the type of migration is present, it's added to the report
            if (occurence > 0) {
                report.append(" * [" + type.getSeverity() + "] ");
                String key = type.getKeyMessage() + SUFFIX_SUMMARIZED_MESSAGE;
                report.append(MessageFormat.format(reportProp.getProperty(key),
                        occurence));
                report.append('\n');
            }
        }

        report.append("\n");
    }

    /**
     * Generate the summary part of the report.
     *
     * @param listResults List of result of analyze of each file.
     * @param report The text output stream of the report to complete.
     * @param reportProp Properties file containing the message to display.
     * @throws IOException
     */
    private void generateDetailedReport(List<FileReport> listResults,
            PrintWriter report, Properties reportProp) throws IOException {
        report.append("Details\n");
        report.append("#######");
        for (FileReport result : listResults) {
            report.append('\n');
            // Create a section for the file
            report.append(result.getAttachedFile().getName());
            report.append("\n-----------------------\n");

            // If nothing was reported, display a generic message
            if (result.getListMigrations().size() == 0) {
                report.append(reportProp.getProperty(NOTHING_MESSAGE));
                report.append('\n');
            }

            // Get the actions to do for the migration
            for (EnumTypeMigration type : result.getListMigrations().keySet()) {
                List<String> listParams = result.getListParams().get(type);
                String key = type.getKeyMessage() + SUFFIX_DETAILED_MESSAGE;
                String messageReport = MessageFormat.format(
                        reportProp.getProperty(key), listParams.toArray());
                report.append("[" + type.getSeverity() + "] ");
                report.append(messageReport);
                report.append('\n');
            }
        }
    }

    @Override
    public FileReport analyzeFile(File file, boolean doMigration, boolean format)
            throws JaxenException, DocumentException {
        return analyzeFileForRules(file, EnumTypeMigration.getTypesMigration(),
                doMigration, format);
    }

    @Override
    public FileReport analyzeFileForRules(File file,
            List<EnumTypeMigration> listRules, boolean doMigration,
            boolean format) throws JaxenException, DocumentException {
        FileReport fileReport = new FileReport(file);

        SAXReader reader = new SAXReader();

        // Check if the file overrides a Nuxeo template
        analyzeOverriddenFile(fileReport, file);

        try {
            Document xhtmlDoc = reader.read(file);
            Document xhtmlOriginal = (Document) xhtmlDoc.clone();

            for (EnumTypeMigration type : listRules) {
                RuleParser parser = type.getInstance(doMigration);
                if (parser != null) {
                    parser.parse(xhtmlDoc, fileReport);
                    if (doMigration) {
                        // If the automatic migration is activated, the parser
                        // tries to do the migration too
                        parser.migrate(xhtmlDoc);
                    }
                }
                // reset the instance of the parser
                type.resetInstance();
            }

            if (doMigration && fileReport.getListMigrations().size() > 0) {
                if (format) {
                    // Format the input file to allow the user to do a diff
                    // easily
                    createFile(xhtmlOriginal, file.getAbsolutePath(), false);
                }
                // Create a new file with the migrations
                createFile(xhtmlDoc, file.getAbsolutePath() + ".migrated", true);
            }
        } catch (DocumentException docEx) {
            // A parsing exception occured, the error is loaded in the
            // FileReport.
            List<String> params = new ArrayList<String>();
            params.add(docEx.getMessage());
            fileReport.getListParams().put(
                    EnumTypeMigration.ERROR_READING_DOCUMENT, params);
            fileReport.getListMigrations().put(
                    EnumTypeMigration.ERROR_READING_DOCUMENT, 1);
        } catch (Exception ex) {
            // TODO
        }

        return fileReport;
    }


    /**
     * Create a file containing the migration done in the Document.
     *
     * @param input
     * @param filePath
     * @throws Exception
     */
    protected void createFile(Document input, String filePath,
            boolean createNewFile) throws Exception {

        // Create file
        File fileMigrated = new File(filePath);
        if (createNewFile) {
            fileMigrated.createNewFile();
        }
        PrintWriter printWriter = new PrintWriter(fileMigrated);
        XMLWriter writer = new XMLWriter(printWriter,
                OutputFormat.createPrettyPrint());
        writer.write(input);

        printWriter.close();
    }

    @Override
    public boolean checkOverriddenTemplate(File file,
            Set<String> listTemplatesRef, boolean completePath) {
        StringBuilder fileName = new StringBuilder();
        if (completePath) {
            fileName.append("nuxeo.war/");
            List<String> listParents = new ArrayList<>();
            File parent = file.getParentFile();
            if (parent != null && parent.exists()) {
                while (!"nuxeo.war".equals(parent.getName())) {
                    listParents.add(parent.getName());
                    parent = parent.getParentFile();
                }
            }
            for (int i = listParents.size() - 1; i >= 0; i--) {
                fileName.append(listParents.get(i));
                fileName.append("/");
            }

        }
        fileName.append(file.getName());

        return listTemplatesRef.contains(fileName.toString());
    }

    public Set<String> getListTemplatesNuxeoPlatform() {
        if (listTemplatesNuxeoPlatform.size() == 0) {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(
                    "listTemplatesNuxeoPlatform.txt");
            InputStreamReader in = new InputStreamReader(is);
            BufferedReader buff = new BufferedReader(in);
            String line = null;
            try {
                while ((line = buff.readLine()) != null) {
                    listTemplatesNuxeoPlatform.add(line);
                }
            } catch (IOException e) {
                logger.error("Error while reading file 'listTemplatesNuxeoPlatform.txt' : "
                        + e.getMessage());
            }
        }

        return listTemplatesNuxeoPlatform;
    }

    /**
     * Analyze if a file overrides a Nuxeo template.
     *
     * @param fileReport The FileReport to fill.
     * @param file The file which is been analyzed.
     */
    protected void analyzeOverriddenFile(FileReport fileReport, File file) {
        // Check if the file is an override of a Nuxeo template
        boolean isOverride = checkOverriddenTemplate(file, getListTemplatesNuxeoPlatform(), true);
        if (isOverride) {
            fileReport.getListMigrations().put(EnumTypeMigration.OVERRIDE_RULE, 1);
            List<String> params = new ArrayList<String>();
            params.add(file.getName());
            fileReport.getListParams().put(EnumTypeMigration.OVERRIDE_RULE, params);
        }
    }
}
