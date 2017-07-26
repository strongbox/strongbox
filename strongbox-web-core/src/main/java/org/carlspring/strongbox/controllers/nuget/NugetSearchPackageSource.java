package org.carlspring.strongbox.controllers.nuget;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import ru.aristar.jnuget.Version;
import ru.aristar.jnuget.files.Nupkg;
import ru.aristar.jnuget.query.Expression;
import ru.aristar.jnuget.sources.AbstractPackageSource;

public class NugetSearchPackageSource extends AbstractPackageSource<Nupkg>
{

    private Expression expression;

    public NugetSearchPackageSource(Expression expression)
    {
        this.expression = expression;
    }

    protected Expression getExpression()
    {
        return expression;
    }

    @Override
    public Collection<Nupkg> getPackages()
    {
        // TODO: implement Package search
        return new ArrayList<>();
    }

    @Override
    public Collection<Nupkg> getLastVersionPackages()
    {
        // TODO: implement Package search
        return new ArrayList<>();
    }

    @Override
    public Collection<Nupkg> getPackages(String id)
    {
        // TODO: implement Package search
        return new ArrayList<>();
    }

    @Override
    public Nupkg getLastVersionPackage(String id)
    {
        // TODO: implement Package search
        return null;
    }

    @Override
    public Nupkg getPackage(String id,
                            Version version)
    {
        // TODO: implement Package search
        return null;
    }

    @Override
    public void removePackage(Nupkg nupkg)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void refreshPackage(Nupkg nupkg)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void processPushPackage(Nupkg nupkg)
        throws IOException
    {
        throw new UnsupportedOperationException();
    }

}
