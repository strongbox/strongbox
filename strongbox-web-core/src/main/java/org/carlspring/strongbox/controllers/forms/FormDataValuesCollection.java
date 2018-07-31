package org.carlspring.strongbox.controllers.forms;

import java.util.List;

/**
 * @author Przemyslaw Fusik
 */
public class FormDataValuesCollection
{

    private List<FormDataValues<?>> formDataValues;


    public FormDataValuesCollection(final List<FormDataValues<?>> formDataValues)
    {
        this.formDataValues = formDataValues;
    }

    public List<FormDataValues<?>> getFormDataValues()
    {
        return formDataValues;
    }

    public void setFormDataValues(final List<FormDataValues<?>> formDataValues)
    {
        this.formDataValues = formDataValues;
    }


}
