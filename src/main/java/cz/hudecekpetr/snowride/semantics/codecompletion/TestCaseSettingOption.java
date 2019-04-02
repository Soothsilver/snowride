package cz.hudecekpetr.snowride.semantics.codecompletion;

import cz.hudecekpetr.snowride.fx.IAutocompleteOption;
import cz.hudecekpetr.snowride.ui.Images;
import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestCaseSettingOption implements IAutocompleteOption {

    public static List<TestCaseSettingOption> allOptions = Arrays.asList(
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
    private String setting;
    private String documentation;

    public TestCaseSettingOption(String setting, String documentation) {
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
        return "Test case setting";
    }

    @Override
    public String toString() {
        return "[" + setting + "]";
    }
}
