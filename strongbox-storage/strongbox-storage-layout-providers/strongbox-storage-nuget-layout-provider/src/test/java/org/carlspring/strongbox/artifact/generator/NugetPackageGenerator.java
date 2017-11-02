package org.carlspring.strongbox.artifact.generator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.bind.JAXBException;

import org.carlspring.commons.io.MultipleDigestInputStream;
import org.carlspring.commons.io.RandomInputStream;
import org.carlspring.strongbox.resource.ResourceCloser;
import org.carlspring.strongbox.util.MessageDigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.aristar.jnuget.files.ClassicNupkg;
import ru.aristar.jnuget.files.NugetFormatException;
import ru.aristar.jnuget.files.Nupkg;
import ru.aristar.jnuget.files.nuspec.Dependencies;
import ru.aristar.jnuget.files.nuspec.Dependency;
import ru.aristar.jnuget.files.nuspec.NuspecFile;

/**
 * @author Kate Novik.
 */
public class NugetPackageGenerator
{

    private static final Logger logger = LoggerFactory.getLogger(NugetPackageGenerator.class);
    
    private static final String PSMDCP_CONTENT = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" + 
            "<coreProperties xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:dcterms=\"http://purl.org/dc/terms/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://schemas.openxmlformats.org/package/2006/m\r\netadata/core-properties\">\r\n  " +
            "<dc:creator>Carlspring</dc:creator>\r\n  " +
            "<dc:description>Strongbox Nuget generated package for tests</dc:description>\r\n  " +
            "<dc:identifier>%s</dc:identifier>\r\n  " +
            "<version>%s</version>\r\n  <keywords>mono strongbox nuget</keywords>\r\n  " +
            "<lastModifiedBy>org.carlspring.strongbox.artifact.generator.NugetPackageGenerator</lastModifiedBy>\r\n" +
            "</coreProperties>";

    private static final String RELS_CONTENT = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" +
            "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">\r\n  " +
            "<Relationship Type=\"http://schemas.microsoft.com/packaging/2010/07/manifest\" Target=\"/%s.nuspec\" Id=\"Rc20b205c579d4f85\" />\r\n  " +
            "<Relationship Type=\"http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties\" Target=\"/package/services/metadata/core-properties/metadata.psmdcp\" Id=\"R23f62\r\ne2778b3442e\" />\r\n"  +
            "</Relationships>";
    
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
                                     String version,
                                     String... dependencyList)
            throws NugetFormatException, JAXBException, IOException, NoSuchAlgorithmException
    {
        File file = new File(getBasedir(), String.format("%s/%s/%s.%s.nupkg", id, version, id, version));
        file.getParentFile()
            .mkdirs();
        ClassicNupkg nupkgFile = new ClassicNupkg(file);

        logger.debug("Version of the nupkg package: ", nupkgFile.getVersion()
                                                                .toString());
        generate(nupkgFile, dependencyList);
    }

    public void generate(ClassicNupkg nupkgFile, String... dependencyList)
            throws IOException,
                   JAXBException,
                   NugetFormatException,
                   NoSuchAlgorithmException
    {
        createArchive(nupkgFile, dependencyList);
        generateNuspecFile(nupkgFile);
    }

    public void createArchive(ClassicNupkg nupkgFile, String... dependencyList)
            throws IOException,
                   JAXBException,
                   NoSuchAlgorithmException, NugetFormatException
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

            addNugetNuspecFile(nupkgFile, zos, dependencyList);
            createRandomNupkgFile(zos);
            createMetadata(nupkgFile.getId(), nupkgFile.getVersion().toString(), zos);
            createContentType(zos);
            createRels(nupkgFile.getId(), zos);
        }
        finally
        {
            ResourceCloser.close(zos, logger);

            generateChecksum(packageFile);
        }
    }

    private void createRels(String id,
                            ZipOutputStream zos)
        throws IOException
    {
        ZipEntry ze = new ZipEntry("_rels/.rels");
        zos.putNextEntry(ze);

        ByteArrayInputStream is = new ByteArrayInputStream(String.format(RELS_CONTENT, id).getBytes());
        byte[] buffer = new byte[4096];
        int len;
        while ((len = is.read(buffer)) > 0)
        {
            zos.write(buffer, 0, len);
        }

        is.close();
        zos.closeEntry();
    }

    private void createContentType(ZipOutputStream zos) throws IOException
    {
        ZipEntry ze = new ZipEntry("[Content_Types].xml");
        zos.putNextEntry(ze);

        InputStream is = getClass().getResourceAsStream("[Content_Types].xml");
        byte[] buffer = new byte[4096];
        int len;
        while ((len = is.read(buffer)) > 0)
        {
            zos.write(buffer, 0, len);
        }

        is.close();
        zos.closeEntry();

    }

    private void createMetadata(String id,
                                String version,
                                ZipOutputStream zos)
        throws IOException
    {
        ZipEntry ze = new ZipEntry("package/services/metadata/core-properties/metadata.psmdcp");
        zos.putNextEntry(ze);

        ByteArrayInputStream is = new ByteArrayInputStream(String.format(PSMDCP_CONTENT, id, version).getBytes());
        byte[] buffer = new byte[4096];
        int len;
        while ((len = is.read(buffer)) > 0)
        {
            zos.write(buffer, 0, len);
        }

        is.close();
        zos.closeEntry();
    }

    private void addNugetNuspecFile(ClassicNupkg nupkgFile,
                                    ZipOutputStream zos,
                                    String... dependencyList)
            throws IOException, JAXBException, NugetFormatException
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

        if (dependencyList != null)
        {
            metadata.dependencies = new Dependencies();
            metadata.dependencies.dependencies = new ArrayList<>();
            for (int i = 0; i < dependencyList.length; i++)
            {
                metadata.dependencies.dependencies.add(Dependency.parseString(dependencyList[i]));
            }
        }
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
        ZipEntry ze = new ZipEntry("lib/random-size-file");
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