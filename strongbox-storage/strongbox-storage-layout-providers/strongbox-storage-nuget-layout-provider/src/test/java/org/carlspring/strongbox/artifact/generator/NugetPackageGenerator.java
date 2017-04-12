package org.carlspring.strongbox.artifact.generator;

import org.carlspring.commons.io.MultipleDigestInputStream;
import org.carlspring.commons.io.RandomInputStream;
import org.carlspring.strongbox.resource.ResourceCloser;
import org.carlspring.strongbox.util.MessageDigestUtils;

import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.aristar.jnuget.files.ClassicNupkg;
import ru.aristar.jnuget.files.NugetFormatException;
import ru.aristar.jnuget.files.Nupkg;
import ru.aristar.jnuget.files.nuspec.NuspecFile;

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
            file.getParentFile()
                .mkdirs();
            ClassicNupkg nupkgFile = new ClassicNupkg(file);

            logger.debug("Version of the nupkg package: ", nupkgFile.getVersion()
                                                                    .toString());
            generate(nupkgFile);
        }
    }

    public void generate(ClassicNupkg nupkgFile)
            throws IOException,
                   JAXBException,
                   NugetFormatException,
                   NoSuchAlgorithmException
    {
        createArchive(nupkgFile);
        generateNuspecFile(nupkgFile);
    }

    public void createArchive(ClassicNupkg nupkgFile)
            throws IOException,
                   JAXBException,
                   NoSuchAlgorithmException
    {
        ZipOutputStream zos = null;

        File packageFile = null;

        try
        {
            packageFile = nupkgFile.getLocalFile();

            // Make sure the artifact's parent directory exists before writing the model.
            //noinspection ResultOfMethodCallIgnored
            packageFile.getParentFile()
                       .mkdirs();

            zos = new ZipOutputStream(new FileOutputStream(packageFile));

            addNugetNuspecFile(nupkgFile, zos);
            createRandomNupkgFile(zos);
        }
        finally
        {
            ResourceCloser.close(zos, logger);

            generateChecksum(packageFile);
        }
    }

    private void addNugetNuspecFile(ClassicNupkg nupkgFile,
                                    ZipOutputStream zos)
            throws IOException, JAXBException
    {
        ZipEntry ze = new ZipEntry(nupkgFile.getId() + ".nuspec");
        zos.putNextEntry(ze);

        NuspecFile nuspec = new NuspecFile();
        NuspecFile.Metadata metadata = nuspec.getMetadata();
        metadata.id = nupkgFile.getId();
        metadata.version = nupkgFile.getVersion();
        metadata.authors = "carlspring";
        metadata.owners = "Carlspring Consulting &amp; Development Ltd.";
        metadata.licenseUrl = "https://www.apache.org/licenses/LICENSE-2.0";
        metadata.description = "Strongbox Nuget package for tests";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        nuspec.saveTo(baos);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

        byte[] buffer = new byte[4096];
        int len;
        while ((len = bais.read(buffer)) > 0)
        {
            zos.write(buffer, 0, len);
        }

        bais.close();
        zos.closeEntry();

    }

    private void createRandomNupkgFile(ZipOutputStream zos)
            throws IOException
    {
        ZipEntry ze = new ZipEntry("random-size-file");
        zos.putNextEntry(ze);

        RandomInputStream ris = new RandomInputStream(true, 1000000);

        byte[] buffer = new byte[4096];
        int len;
        while ((len = ris.read(buffer)) > 0)
        {
            zos.write(buffer, 0, len);
        }

        ris.close();
        zos.closeEntry();

    }

    private void generateNuspecFile(Nupkg nupkgFile)
            throws IOException, NugetFormatException, JAXBException, NoSuchAlgorithmException
    {
        File nuspecFile = new File(getBasedir(), String.format("%s/%s.nuspec", nupkgFile.getVersion()
                                                                                        .toString(),
                                                               nupkgFile.getId()));
        nuspecFile.getParentFile()
                  .mkdirs();

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

        mdis.addAlgorithm("SHA-512");

        String sha512 = mdis.getMessageDigestAsHexadecimalString("SHA-512");

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