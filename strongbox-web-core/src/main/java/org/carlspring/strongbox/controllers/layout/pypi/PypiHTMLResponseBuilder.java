package org.carlspring.strongbox.controllers.layout.pypi;

import java.util.Formatter;
import java.util.List;

public class PypiHTMLResponseBuilder {
    private static final String END =
            "        </body>\n" +
                    "    </html>";
    private static final String MID = "                 <a href=\"%s\">%s</a><br>\n";
    private static final String LIST_START =
            "<html>\n" +
            "        <head>\n" +
            "            <title>Index of packages</title>\n" +
            "        </head>\n" +
            "        <body>\n" +
            "            <h1>Index of packages</h1>\n";


    private static final String PROJECT_START =
            "<html>\n" +
            "        <head>\n" +
            "            <title>Links for 1$s</title>\n" +
            "        </head>\n" +
            "        <body>\n" +
            "            <h1>Links for 1$s</h1>\n";



    public static String BuildListResponse(List<String> name, List<String> link){
        StringBuilder builder = new StringBuilder(LIST_START);
        Formatter formatter = new Formatter(builder);
        for (int i = 0; i < name.size(); i++)
            formatter.format(MID,link.get(i),name.get(i));
        builder.append(END);
        return builder.toString();
    }

    public static String BuildProjectResponse(List<String> name, List<String> link,String Project){
        StringBuilder builder = new StringBuilder();
        Formatter formatter = new Formatter(builder);
        formatter.format(PROJECT_START,Project);
        for (int i = 0; i < name.size(); i++)
            formatter.format(MID,link.get(i),name.get(i));
        builder.append(END);
        return builder.toString();
    }

}
