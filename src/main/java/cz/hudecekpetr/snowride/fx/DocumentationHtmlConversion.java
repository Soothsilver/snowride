package cz.hudecekpetr.snowride.fx;

public class DocumentationHtmlConversion {
    public static String robotToHtml(String fullDocumentation, int fontsize) {
        StringBuilder html = new StringBuilder();
        if (fullDocumentation == null) {
            return "";
        }
        int startFromIndex = 0;
        html.append("<span style='font-family: \"Segoe UI\", \"Calibri\", \"Tahoma\", \"Verdana\"; font-size: " + fontsize + "pt; '>");
        String italicsCharacter = "[ITALICS]";
        while (startFromIndex < fullDocumentation.length()) {
            int earliestAsterisk = fullDocumentation.indexOf('*', startFromIndex);
            int earliestUnderscore = fullDocumentation.indexOf('_', startFromIndex);
            int earliestCode = fullDocumentation.indexOf("``", startFromIndex);
            int minimumSpecial = minNonMinusOne(earliestAsterisk, earliestUnderscore, earliestCode);
            if (minimumSpecial == -1) {
                html.append(fullDocumentation.substring(startFromIndex));
                break;
            } else {
                html.append(fullDocumentation.substring(startFromIndex, minimumSpecial));
                if (minimumSpecial == earliestAsterisk) {
                    int finalAsterisk = fullDocumentation.indexOf('*', earliestAsterisk+1);
                    if (finalAsterisk != -1) {
                        html.append("<b>").append(fullDocumentation.substring(earliestAsterisk+1, finalAsterisk)).append("</b>");
                        startFromIndex = finalAsterisk +1;
                        continue;
                    }
                } else if (minimumSpecial == earliestCode) {
                    int finalCode  = fullDocumentation.indexOf("``", earliestCode+2);
                    if (finalCode != -1) {
                        html.append("<tt>").append(fullDocumentation.substring(earliestCode+2, finalCode)).append("</tt>");
                        startFromIndex = finalCode + 2;
                        continue;
                    }
                } else if (minimumSpecial == earliestUnderscore) {
                    int finalUnderscore  = fullDocumentation.indexOf(italicsCharacter, earliestUnderscore+1);
                    if (finalUnderscore != -1) {
                        html.append("<i>").append(fullDocumentation.substring(earliestUnderscore+italicsCharacter.length(), finalUnderscore)).append("</i>");
                        startFromIndex = finalUnderscore + italicsCharacter.length();
                        continue;
                    }
                }
                html.append(Character.toString(fullDocumentation.charAt(minimumSpecial)));
                startFromIndex = minimumSpecial + 1;
            }
        }
        html.append("</span>");
        return html.toString().replace("\n", "<br>");
    }

    private static int minNonMinusOne(int earliestAsterisk, int earliestUnderscore, int earliestCode) {
        int bestSoFar = Integer.MAX_VALUE;
        if (earliestAsterisk < bestSoFar && earliestAsterisk != -1) {
            bestSoFar = earliestAsterisk;
        }
        if (earliestUnderscore < bestSoFar && earliestUnderscore != -1) {
            bestSoFar = earliestUnderscore;
        }
        if (earliestCode < bestSoFar && earliestCode != -1) {
            bestSoFar = earliestCode;
        }
        if (bestSoFar == Integer.MAX_VALUE) {
            return -1;
        }
        return bestSoFar;
    }
}
