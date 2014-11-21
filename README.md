nuxeo-jsf2-migration
====================

Module with helper methods for JSF2 migration of XHTML templates.

The module analyzes a project directory to get all the XHTML files (and XHTML only) to determine the action for the migration to JSF 2.
A report is generated containing the actions to be done for the migration.

Usage : java -jar nuxeo-jsf2-migration-\[version\].jar \[options\] \[path\]

 + options:
   + -m : If set, an automatic migration will be done (when possible)
   + -f : If set, the original files will be formatted in order to allow users to do a diff between the original file and the migrated one easily
   + -r : If set, the migration will walk the directory tree and will be applied in every valid project directory found.
 + path : path to the project directory. It is important to specify the path to the root of the directory and not the folder containing the XHTML files to analyze

## How to add a new rule

The migration rules are defined in the enumeration EnumTypeMigration. To add a new rule, just add a new entry in the enumeration.
When defining a new rule, the parameters to define are :
+ xpath
+ keyMessage
+ severityMessage
+ parser
+ migrationAuto
+ newValue

#### xpath

This parameter represents the XPath expression used to search the elements impacted by the migration. It could be null or empty if the rule does not use XPath expression.

#### keyMessage

This parameter represents the key of the message in the properties file 'report.properties'. The key is not used by itself, there are two kind of messages :

- message in the summary part of the report, the key to the message to display in this part is the keyMessage plus the suffixe '.summarized'
- message in the detailed part of the report, the key to the message to display in this part is the keyMessage plus the suffixe '.detailed'

Be careful when you add a new rule to not forget to add those two messages

#### severityMessage

This parameter represents the severity of the message, it could be WARNING, INFO or ERROR. The level of error is displayed in the report before the message associated to the rule.

#### parser

The parser is a class implementing the RuleParser interface. Two methods have to be defined : 'parse' and 'migrate'.

The method 'parse' is called when a file is analyzed for the rule. When a rule is matching the input file, add a new entry in the object FileReport for the list 'listMigrations'. This list is iterated when generating the report.

The method 'migrate' will do the automatic migration of the file if the rule associated to the parser allows it. Only the DOM of the input file is updated, the writing of the DOM is done after the process of every rules.

Note that if the parser is null (for example to handle 'technical' rule as an error with SaxReader when parsing the input file, see the rule ERROR\_READING\_DOCUMENT), the rule will not be automatically processed on the files of the project. It will have to be called manually in the MigrationService.

#### migrationAuto

This parameter defines that the rule allows an automatic migration, if this mode is selected by the user when lauching the migration module.

#### newValue

This parameter is optional. If set, it represents the value to replace the name of the elements listed by the XPath expression. It could be the name of a tag or an attribute for example. When the migration is more complicated than just a 'search and replace' rule, it might be necessary to define a specific parser and the behavior for the migration is defined in the 'migrate' method.

## FileReport object

When a rule analyzes a file, it will feed the FileReport object when a match is found. This object is used when generating the report. A FileReport is associated to a File that has been parsed so it contains only the migration steps to do for this file.

The list of matching rule are stored in the list 'listMigrations'. This list enumerates the migration steps found on the file. For each rule listed, a new line will be create in the report. The value paired with the rule is the number of occurences of the rule for the file. It is used by the summary of the report when counting the occurences of every rules in every files.

The list 'listParams' contains the parameters used to display the message in the report.
