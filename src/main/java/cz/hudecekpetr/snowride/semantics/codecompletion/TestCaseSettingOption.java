package cz.hudecekpetr.snowride.semantics.codecompletion;

import cz.hudecekpetr.snowride.fx.IAutocompleteOption;
import cz.hudecekpetr.snowride.ui.Images;
import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestCaseSettingOption implements IAutocompleteOption {

    public static List<TestCaseSettingOption> testCaseTableOptions = Arrays.asList(
            new TestCaseSettingOption("Documentation", "Used for specifying a test case documentation."),
            new TestCaseSettingOption("Tags", "Used for tagging test cases."),
            new TestCaseSettingOption("Arguments", "Used for specifying user keyword arguments."),
            new TestCaseSettingOption("Return", "Used for specifying user keyword return values."),
            new TestCaseSettingOption("Teardown", "The keyword in this setting will be executed in teardown mode at the end of this test case or user keyword."),
            new TestCaseSettingOption("Timeout", "The test case timeout can be set either by using the Test Timeout setting in the Setting table or the [Timeout] setting in the Test Case table. Test Timeout in the Setting table defines a default test timeout value for all the test cases in the test suite, whereas [Timeout] in the Test Case table applies a timeout to an individual test case and overrides the possible default value.\n" +
                    "\n" +
                    "Using an empty [Timeout] means that the test has no timeout even when Test Timeout is used. It is also possible to use value NONE for this purpose.\n" +
                    "\n" +
                    "Regardless of where the test timeout is defined, the first cell after the setting name contains the duration of the timeout. The duration must be given in Robot Framework's time format, that is, either directly in seconds or in a format like 1 minute 30 seconds. It must be noted that there is always some overhead by the framework, and timeouts shorter than one second are thus not recommended.\n" +
                    "\n" +
                    "The default error message displayed when a test timeout occurs is Test timeout <time> exceeded. It is also possible to use custom error messages, and these messages are written into the cells after the timeout duration. The message can be split into multiple cells, similarly as documentations. Both the timeout value and the error message may contain variables.\n" +
                    "\n" +
                    "If there is a timeout, the keyword running is stopped at the expiration of the timeout and the test case fails. However, keywords executed as test teardown are not interrupted if a test timeout occurs, because they are normally engaged in important clean-up activities. If necessary, it is possible to interrupt also these keywords with user keyword timeouts."),
            new TestCaseSettingOption("Template", "Templates are currently not supported by Snowride!\n\nTest templates convert normal keyword-driven test cases into data-driven tests. Whereas the body of a keyword-driven test case is constructed from keywords and their possible arguments, test cases with template contain only the arguments for the template keyword. Instead of repeating the same keyword multiple times per test and/or with all tests in a file, it is possible to use it only per test or just once per file.\n" +
                    "\n" +
                    "Template keywords can accept both normal positional and named arguments, as well as arguments embedded to the keyword name. Unlike with other settings, it is not possible to define a template using a variable."),
            new TestCaseSettingOption("Setup", "Robot Framework has similar test setup and teardown functionality as many other test automation frameworks. In short, a test setup is something that is executed before a test case, and a test teardown is executed after a test case. In Robot Framework setups and teardowns are just normal keywords with possible arguments.\n" +
                    "\n" +
                    "Setup and teardown are always a single keyword. If they need to take care of multiple separate tasks, it is possible to create higher-level user keywords for that purpose. An alternative solution is executing multiple keywords using the BuiltIn keyword Run Keywords.\n" +
                    "\n" +
                    "The test teardown is special in two ways. First of all, it is executed also when a test case fails, so it can be used for clean-up activities that must be done regardless of the test case status. In addition, all the keywords in the teardown are also executed even if one of them fails. This continue on failure functionality can be used also with normal keywords, but inside teardowns it is on by default.\n" +
                    "\n" +
                    "The easiest way to specify a setup or a teardown for test cases in a test case file is using the Test Setup and Test Teardown settings in the Setting table. Individual test cases can also have their own setup or teardown. They are defined with the [Setup] or [Teardown] settings in the test case table and they override possible Test Setup and Test Teardown settings. Having no keyword after a [Setup] or [Teardown] setting means having no setup or teardown. It is also possible to use value NONE to indicate that a test has no setup/teardown.")
    );
    public static List<TestCaseSettingOption> settingsTableOptions = Arrays.asList(
            new TestCaseSettingOption("Library", "Test libraries are normally imported using the Library setting in the Setting table and having the library name in the subsequent column. Unlike most of the other data, the library name is both case- and space-sensitive. If a library is in a package, the full name including the package name must be used.\n" +
                    "\n" +
                    "In those cases where the library needs arguments, they are listed in the columns after the library name. It is possible to use default values, variable number of arguments, and named arguments in test library imports similarly as with arguments to keywords. Both the library name and arguments can be set using variables.\n" +
                    "\n" +
                    "It is possible to import test libraries in test case files, resource files and test suite initialization files. In all these cases, all the keywords in the imported library are available in that file. With resource files, those keywords are also available in other files using them.\n" +
                    "\n"),
            new TestCaseSettingOption("Resource", "Resource files are imported using the Resource setting in the Settings table. The path to the resource file is given in the cell after the setting name.\n" +
                    "\n" +
                    "If the path is given in an absolute format, it is used directly. In other cases, the resource file is first searched relatively to the directory where the importing file is located. If the file is not found there, it is then searched from the directories in Python's module search path. The path can contain variables, and it is recommended to use them to make paths system-independent (for example, ${RESOURCES}/login_resources.robot or ${RESOURCE_PATH}). Additionally, forward slashes (/) in the path are automatically changed to backslashes (\\) on Windows.\n" +
                    "\n" +
                    "Resource files can use all the same extensions as test case files created using the supported file formats. When using the plain text format, it is possible to use a special .resource extension in addition to the normal .robot extensions. This makes it easier to separate test case files and resource files from each others.\n" +
                    "\n"),
            new TestCaseSettingOption("Variables", "All test data files can import variables using the Variables setting in the Setting table, in the same way as resource files are imported using the Resource setting. Similarly to resource files, the path to the imported variable file is considered relative to the directory where the importing file is, and if not found, it is searched from the directories in the module search path. The path can also contain variables, and slashes are converted to backslashes on Windows. If an argument file takes arguments, they are specified in the cells after the path and also they can contain variables.\n" +
                    "\n"),
            new TestCaseSettingOption("Documentation", " The resource file itself can have Documentation in the Setting table similarly as test suites.\n" +
                    "\n" +
                    "Both Libdoc and RIDE use these documentations, and they are naturally available for anyone opening resource files. The first logical line of the documentation of a keyword, until the first empty line, is logged when the keyword is run, but otherwise resource file documentation is ignored during the test execution."),
            new TestCaseSettingOption("Metadata", "Test suites can also have other metadata than the documentation. This metadata is defined in the Setting table using the Metadata setting. Metadata set in this manner is shown in test reports and logs.\n" +
                    "\n" +
                    "The name and value for the metadata are located in the columns following Metadata. The value is handled similarly as documentation, which means that it can be split into several cells (joined together with spaces) or into several rows (joined together with newlines), simple HTML formatting works and even variables can be used."),
            new TestCaseSettingOption("Suite Setup", "Not only test cases but also test suites can have a setup and a teardown. A suite setup is executed before running any of the suite's test cases or child test suites, and a test teardown is executed after them. All test suites can have a setup and a teardown; with suites created from a directory they must be specified in a test suite initialization file.\n" +
                    "\n" +
                    "Similarly as with test cases, a suite setup and teardown are keywords that may take arguments. They are defined in the Setting table with Suite Setup and Suite Teardown settings, respectively. Keyword names and possible arguments are located in the columns after the setting name.\n" +
                    "\n" +
                    "If a suite setup fails, all test cases in it and its child test suites are immediately assigned a fail status and they are not actually executed. This makes suite setups ideal for checking preconditions that must be met before running test cases is possible.\n" +
                    "\n" +
                    "A suite teardown is normally used for cleaning up after all the test cases have been executed. It is executed even if the setup of the same suite fails. If the suite teardown fails, all test cases in the suite are marked failed, regardless of their original execution status. Note that all the keywords in suite teardowns are executed even if one of them fails.\n" +
                    "\n" +
                    "The name of the keyword to be executed as a setup or a teardown can be a variable. This facilitates having different setups or teardowns in different environments by giving the keyword name as a variable from the command line.\n" +
                    "\n"),
            new TestCaseSettingOption("Suite Teardown", "Not only test cases but also test suites can have a setup and a teardown. A suite setup is executed before running any of the suite's test cases or child test suites, and a test teardown is executed after them. All test suites can have a setup and a teardown; with suites created from a directory they must be specified in a test suite initialization file.\n" +
                    "\n" +
                    "Similarly as with test cases, a suite setup and teardown are keywords that may take arguments. They are defined in the Setting table with Suite Setup and Suite Teardown settings, respectively. Keyword names and possible arguments are located in the columns after the setting name.\n" +
                    "\n" +
                    "If a suite setup fails, all test cases in it and its child test suites are immediately assigned a fail status and they are not actually executed. This makes suite setups ideal for checking preconditions that must be met before running test cases is possible.\n" +
                    "\n" +
                    "A suite teardown is normally used for cleaning up after all the test cases have been executed. It is executed even if the setup of the same suite fails. If the suite teardown fails, all test cases in the suite are marked failed, regardless of their original execution status. Note that all the keywords in suite teardowns are executed even if one of them fails.\n" +
                    "\n" +
                    "The name of the keyword to be executed as a setup or a teardown can be a variable. This facilitates having different setups or teardowns in different environments by giving the keyword name as a variable from the command line.\n" +
                    "\n"),
            new TestCaseSettingOption("Force Tags", "All test cases in a test case file with this setting always get specified tags. If it is used in the test suite initialization file, all test cases in sub test suites get these tags."),
            new TestCaseSettingOption("Default Tags", "Test cases that do not have a [Tags] setting of their own get these tags. Default tags are not supported in test suite initialization files."),
            new TestCaseSettingOption("Test Setup", "The easiest way to specify a setup or a teardown for test cases in a test case file is using the Test Setup and Test Teardown settings in the Setting table. "),
            new TestCaseSettingOption("Test Teardown", "The easiest way to specify a setup or a teardown for test cases in a test case file is using the Test Setup and Test Teardown settings in the Setting table. "),
            new TestCaseSettingOption("Test Template", "Test templates convert normal keyword-driven test cases into data-driven tests. Whereas the body of a keyword-driven test case is constructed from keywords and their possible arguments, test cases with template contain only the arguments for the template keyword. Instead of repeating the same keyword multiple times per test and/or with all tests in a file, it is possible to use it only per test or just once per file.\n" +
                    "\n" +
                    "Template keywords can accept both normal positional and named arguments, as well as arguments embedded to the keyword name. Unlike with other settings, it is not possible to define a template using a variable.\n" +
                    "\n"),
            new TestCaseSettingOption("Test Timeout", "The test case timeout can be set either by using the Test Timeout setting in the Setting table or the [Timeout] setting in the Test Case table. Test Timeout in the Setting table defines a default test timeout value for all the test cases in the test suite, whereas [Timeout] in the Test Case table applies a timeout to an individual test case and overrides the possible default value.\n" +
                    "\n" +
                    "Using an empty [Timeout] means that the test has no timeout even when Test Timeout is used. It is also possible to use value NONE for this purpose.\n" +
                    "\n" +
                    "Regardless of where the test timeout is defined, the first cell after the setting name contains the duration of the timeout. The duration must be given in Robot Framework's time format, that is, either directly in seconds or in a format like 1 minute 30 seconds. It must be noted that there is always some overhead by the framework, and timeouts shorter than one second are thus not recommended.\n" +
                    "\n" +
                    "The default error message displayed when a test timeout occurs is Test timeout <time> exceeded. It is also possible to use custom error messages, and these messages are written into the cells after the timeout duration. The message can be split into multiple cells, similarly as documentations. Both the timeout value and the error message may contain variables.\n" +
                    "\n" +
                    "If there is a timeout, the keyword running is stopped at the expiration of the timeout and the test case fails. However, keywords executed as test teardown are not interrupted if a test timeout occurs, because they are normally engaged in important clean-up activities. If necessary, it is possible to interrupt also these keywords with user keyword timeouts.\n" +
                    "\n")
    );
    private String setting;
    private String documentation;

    private TestCaseSettingOption(String setting, String documentation) {
        this.setting = setting;
        this.documentation = documentation;
    }

    @Override
    public Image getAutocompleteIcon() {
        return Images.brackets;
    }

    @Override
    public String getAutocompleteText() {
        return toString();
    }

    @Override
    public String getFullDocumentation() {
        return documentation;
    }

    @Override
    public String getItalicsSubheading() {
        return "Setting";
    }

    @Override
    public String toString() {
        return "[" + setting + "]";
    }
}
