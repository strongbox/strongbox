package org.carlspring.strongbox.xml;

import org.carlspring.strongbox.xml.parsers.Dummy;
import org.carlspring.strongbox.xml.parsers.GenericParser;

import javax.xml.bind.JAXBException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * @author mtodorov
 */
public class ElementWrapperTest
{

    @Test
    public void testWrapping()
            throws JAXBException
    {
        Dummy d1 = new Dummy();
        d1.setName("dummy1");

        Dummy d2 = new Dummy();
        d2.setName("dummy2");

        List<Dummy> listOfDummies = new ArrayList<Dummy>();
        listOfDummies.add(d1);
        listOfDummies.add(d2);

        ElementWrapper<Dummy> dummies = new ElementWrapper<Dummy>(listOfDummies) {};

        GenericParser<ElementWrapper<Dummy>> parser = new GenericParser<ElementWrapper<Dummy>>(ElementWrapper.class, Dummy.class);

        parser.store(dummies, System.out);
    }

}
