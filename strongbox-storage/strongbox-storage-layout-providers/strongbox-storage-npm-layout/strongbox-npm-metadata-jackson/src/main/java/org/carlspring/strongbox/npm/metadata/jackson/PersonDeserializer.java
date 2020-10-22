package org.carlspring.strongbox.npm.metadata.jackson;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.carlspring.strongbox.npm.metadata.Person;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class PersonDeserializer extends JsonDeserializer<Person>
{

    private static final Pattern EMAIL_PATTERN = Pattern.compile(".*<(.*?)>.*");
    private static final Pattern WWW_PATTERN = Pattern.compile(".*\\((.*?)\\).*");

    @Override
    public Person deserialize(JsonParser jp,
                              DeserializationContext c)
        throws IOException,
        JsonProcessingException
    {

        ObjectCodec codec = jp.getCodec();
        JsonNode node = codec.readTree(jp);

        String value = node.asText();

        if (value.startsWith("{"))
        {
            return codec.treeToValue(node, Person.class);
        }

        Person person = new Person();

        String email = "";
        Matcher emailMatcher = EMAIL_PATTERN.matcher(value);
        if (emailMatcher.matches())
        {
            email = emailMatcher.replaceFirst("$1");
        }

        String www = "";
        Matcher wwwMatcher = WWW_PATTERN.matcher(value);
        if (wwwMatcher.matches())
        {
            www = wwwMatcher.replaceFirst("$1");
        }

        String name = value.replace("<" + email + ">", "");
        name = name.replace("(" + www + ")", "");

        person.setName(name);
        person.setEmail(name);
        person.setUrl(www);

        return person;
    }

    public static void main(String args[])
        throws Exception
    {

        String person = "Barney Rubble <b@rubble.com> (http://barnyrubble.tumblr.com/)";

        String email = "";
        Matcher emailMatcher = EMAIL_PATTERN.matcher(person);
        if (emailMatcher.matches())
        {
            email = emailMatcher.replaceFirst("$1");
            System.out.println(email);
        }

        String www = "";
        Matcher wwwMatcher = WWW_PATTERN.matcher(person);
        if (wwwMatcher.matches())
        {
            www = wwwMatcher.replaceFirst("$1");
            System.out.println(www);
        }

        String name = person.replace("<" + email + ">", "");
        name = name.replace("(" + www + ")", "");
        System.out.println(name);
    }
}
