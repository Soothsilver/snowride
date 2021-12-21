package cz.hudecekpetr.snowride.semantics.externallibraries;

import cz.hudecekpetr.snowride.semantics.Parameter;
import cz.hudecekpetr.snowride.semantics.ParameterKind;

import java.util.Collections;

public class ForLoops {
    public static void addForKeywordsTo(ExternalLibrary builtIn) {
        builtIn.keywords.add(new ExternalKeyword(": FOR", "Repeating same actions several times is quite a common need in test automation. With Robot Framework, test libraries can have any kind of loop constructs, and most of the time loops should be implemented in them. Robot Framework also has its own for loop syntax, which is useful, for example, when there is a need to repeat keywords from different libraries.\n" +
                "\n" +
                "For loops can be used with both test cases and user keywords. Except for really simple cases, user keywords are better, because they hide the complexity introduced by for loops. The basic for loop syntax, FOR item IN sequence, is derived from Python, but similar syntax is supported also by various other programming languages." +
                "\n\n" +
                "*This syntax is deprecated.* For loop syntax was enhanced in various ways in Robot Framework 3.1. The most noticeable change was that loops nowadays end with the explicit END marker and keywords inside the loop do not need to be indented. In the space separated plain text format indentation required escaping with a backslash which resulted in quite ugly syntax. Another change was that the for loop marker used to be :FOR when nowadays just FOR is enough. Related to that, the :FOR marker and also the IN separator were case-insensitive but nowadays both FOR and IN are case-sensitive.\n" +
                "\n" +
                "Old for loop syntax still works in Robot Framework 3.1 and only using IN case-insensitively causes a deprecation warning. Not closing loops with END, escaping keywords inside loops with \\, and using :FOR instead of FOR are all going to be deprecated in Robot Framework 3.2. Users are advised to switch to the new syntax as soon as possible.", Collections.singletonList(new Parameter("arguments", ParameterKind.VARARGS)), builtIn));


        builtIn.keywords.add(new ExternalKeyword("FOR", "Repeating same actions several times is quite a common need in test automation. With Robot Framework, test libraries can have any kind of loop constructs, and most of the time loops should be implemented in them. Robot Framework also has its own for loop syntax, which is useful, for example, when there is a need to repeat keywords from different libraries.\n" +
                "\n" +
                "For loops can be used with both test cases and user keywords. Except for really simple cases, user keywords are better, because they hide the complexity introduced by for loops. The basic for loop syntax, FOR item IN sequence, is derived from Python, but similar syntax is supported also by various other programming languages.", Collections.singletonList(new Parameter("arguments", ParameterKind.VARARGS)), builtIn));


        builtIn.keywords.add(new ExternalKeyword("IF", "Sometimes there is a need to execute some keywords conditionally. Starting from Robot Framework 4.0 there is a separate if expression syntax, but there are also other ways to execute keywords conditionally. Notice that if the logic gets complicated, it is typically better to move it into a test library.", Collections.emptyList(), builtIn));
        builtIn.keywords.add(new ExternalKeyword("ELSE IF", "Robot Framework also supports ELSE IF branches that have their own condition that is evaluated if the initial condition is not true. There can be any number of ELSE IF branches and they are gone through in the order they are specified. If one of the ELSE IF conditions is true, the block following it is executed and remaining ELSE IF branches are ignored. An optional ELSE branch can follow ELSE IF branches and it is executed if all conditions are false..", Collections.emptyList(), builtIn));
        builtIn.keywords.add(new ExternalKeyword("ELSE", "Like most other languages supporting conditional execution, Robot Framework IF syntax also supports ELSE branches that are executed if the IF condition is not true.", Collections.emptyList(), builtIn));
        builtIn.keywords.add(new ExternalKeyword("END", "This token, on a line on its own, ends the new Robot Framework for loop syntax.", Collections.emptyList(), builtIn));

    }
}
