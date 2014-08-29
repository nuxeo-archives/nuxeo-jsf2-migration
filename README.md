nuxeo-jsf2-migration
====================

Module with helper methods for JSF2 migration of XHTML templates.

The module analyzes a project directory to get all the XHTML files (and XHTML only) to determine the action for the migration to JSF 2.

Usage : java -jar \[path\] \[migration\]
 - path : path to the project directory. It is important to specify the path to the root of the directory and not the folder containing the XHTML files to analyze
 - migration : boolean, not mandatory, by default the value is false. If set to false, only a report is generated containing the actions to be done for the migration. If true, a report will be generated and an automatic migration will be done (when possible)