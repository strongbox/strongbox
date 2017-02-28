package org.carlspring.strongbox.artifact.generator;

import org.carlspring.commons.io.MultipleDigestInputStream;
import org.carlspring.commons.io.RandomInputStream;
import org.carlspring.strongbox.util.MessageDigestUtils;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.aristar.jnuget.files.ClassicNupkg;
import ru.aristar.jnuget.files.NugetFormatException;
import ru.aristar.jnuget.files.Nupkg;

/**
 * @author Kate Novik.
 */
public class NugetPackageGenerator
{

    private static final Logger logger = LoggerFactory.getLogger(NugetPackageGenerator.class);

    private String basedir;

    public NugetPackageGenerator()
    {
    }

    public NugetPackageGenerator(String basedir)
    {
        this.basedir = basedir;
    }

    public NugetPackageGenerator(File basedir)
    {
        this.basedir = basedir.getAbsolutePath();
    }

    public void generateNugetPackage(String id,
                                     String... versions)
            throws NugetFormatException, JAXBException, IOException, NoSuchAlgorithmException
    {
        for (String version : versions)
        {
            File file = new File(getBasedir(), String.format("%s/%s.%s.nupkg", version, id, version));
            ClassicNupkg nupkgFile = new ClassicNupkg(file);
            generate(nupkgFile);
        }
    }

    public void generate(ClassicNupkg nupkgFile)
            throws NugetFormatException, JAXBException, IOException, NoSuchAlgorithmException
    {
        writeRandomNupkgFile(new FileOutputStream(nupkgFile.getLocalFile()));
        generateNuspecFile(nupkgFile);
        generateChecksum(nupkgFile.getLocalFile());
    }

    private void writeRandomNupkgFile(FileOutputStream os)
            throws IOException
    {
        RandomInputStream ris = new RandomInputStream(true, 1000000);

        byte[] buffer = new byte[4096];
        int len;
        while ((len = ris.read(buffer)) > 0)
        {
            os.write(buffer, 0, len);
        }

        ris.close();

    }

    private void generateNuspecFile(Nupkg nupkgFile)
            throws IOException, NugetFormatException, JAXBException, NoSuchAlgorithmException
    {
        File nuspecFile = new File(getBasedir(),
                                   String.format("%s/%s.nuspec", nupkgFile.getVersion(), nupkgFile.getId()));
        try (FileOutputStream fileOutputStream = new FileOutputStream(nuspecFile))
        {
            nupkgFile.getNuspecFile()
                     .saveTo(fileOutputStream);
        }
        generateChecksum(nuspecFile);

    }

    private void generateChecksum(File file)
            throws IOException, NoSuchAlgorithmException
    {

        InputStream is = new FileInputStream(file);
        MultipleDigestInputStream mdis = new MultipleDigestInputStream(is);

        int size = 4096;
        byte[] bytes = new byte[size];

        //noinspection StatementWithEmptyBody
        while (mdis.read(bytes, 0, size) != -1) ;

        mdis.close();

        String sha512 = mdis.getMessageDigestAsHexadecimalString("SHA512");

        MessageDigestUtils.writeChecksum(file, ".sha512", sha512);

    }

    public String getBasedir()
    {
        return basedir;
    }

    public void setBasedir(String basedir)
    {
        this.basedir = basedir;
    }

}
