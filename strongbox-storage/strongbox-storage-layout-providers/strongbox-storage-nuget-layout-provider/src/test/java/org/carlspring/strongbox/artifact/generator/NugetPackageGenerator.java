package org.carlspring.strongbox.artifact.generator;

import org.carlspring.commons.io.RandomInputStream;
import org.carlspring.strongbox.io.LayoutOutputStream;
import org.carlspring.strongbox.resource.ResourceCloser;
import org.carlspring.strongbox.storage.metadata.nuget.Dependencies;
import org.carlspring.strongbox.storage.metadata.nuget.Dependency;
import org.carlspring.strongbox.storage.metadata.nuget.NugetFormatException;
import org.carlspring.strongbox.storage.metadata.nuget.Nuspec;
import org.carlspring.strongbox.util.MessageDigestUtils;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.semver.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
                                     String versionString,
                                     String... dependencyList)
            throws NugetFormatException, JAXBException, IOException, NoSuchAlgorithmException
    {
        File file = new File(getBasedir(), String.format("%s/%s/%s.%s.nupkg", id, versionString, id, versionString));
        file.getParentFile()
            .mkdirs();
        
        Version version = Version.parse(versionString);
        logger.debug("Version of the nupkg package: ", version.toString());
        
        generate(file, id, version, dependencyList);
    }

    public void generate(File nupkgFile, String id, Version version, String... dependencyList)
            throws IOException,
                   JAXBException,                   
                   NoSuchAlgorithmException, 
                   NugetFormatException
    {
        Nuspec nuspec = generateNuspec(id, version, dependencyList);
        createArchive(nuspec, nupkgFile, dependencyList);
        generateNuspecFile(nuspec);
    }

    public void createArchive(Nuspec nuspec, File packageFile, String... dependencyList)
            throws IOException,
                   JAXBException,
                   NoSuchAlgorithmException, 
                   NugetFormatException
    {
        ZipOutputStream zos = null;

        LayoutOutputStream layoutOutputStream = null;
        try
        {
            // Make sure the artifact's parent directory exists before writing the model.
            //noinspection ResultOfMethodCallIgnored
            packageFile.getParentFile()
                       .mkdirs();

            FileOutputStream fileOutputStream = new FileOutputStream(packageFile);
            
            layoutOutputStream = new LayoutOutputStream(fileOutputStream);
            layoutOutputStream.addAlgorithm(MessageDigestAlgorithms.SHA_512);
            layoutOutputStream.setDigestStringifier(this::toBase64);
            
            zos = new ZipOutputStream(layoutOutputStream);

            addNugetNuspecFile(nuspec, zos);
            createRandomNupkgFile(zos);
            
            String id = nuspec.getId();
            Version version = nuspec.getVersion();
            createMetadata(id, version.toString(), zos);
            
            createContentType(zos);
            createRels(id, zos);
        }
        finally
        {
            ResourceCloser.close(zos, logger);

            if (layoutOutputStream != null)
            {                
                generateChecksum(packageFile, layoutOutputStream);
            }
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

    private void addNugetNuspecFile(Nuspec nuspec,
                                    ZipOutputStream zos)
            throws IOException, JAXBException, NugetFormatException
    {
        ZipEntry ze = new ZipEntry(nuspec.getId() + ".nuspec");
        zos.putNextEntry(ze);

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

    private Nuspec generateNuspec(String id, Version version, String... dependencyList) throws NugetFormatException
    {
        Nuspec nuspec = new Nuspec();
        Nuspec.Metadata metadata = nuspec.getMetadata();
        metadata.id = id;
        metadata.version = version;
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
        return nuspec;
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

    private void generateNuspecFile(Nuspec nuspec)
            throws IOException, NugetFormatException, JAXBException, NoSuchAlgorithmException
    {
        String packageId = nuspec.getId();
        String packageVersion = nuspec.getVersion()
                                         .toString();
        File nuspecFile = new File(getBasedir(),
                String.format("%s/%s/%s.nuspec", packageId, packageVersion,
                              packageId));
        nuspecFile.getParentFile()
                  .mkdirs();

        try (FileOutputStream fileOutputStream = new FileOutputStream(nuspecFile))
        {
            LayoutOutputStream layoutOutputStream = new LayoutOutputStream(fileOutputStream);
            layoutOutputStream.addAlgorithm(MessageDigestAlgorithms.SHA_512);
            layoutOutputStream.setDigestStringifier(this::toBase64);
            try {
                nuspec.saveTo(layoutOutputStream);                
            } finally {
                layoutOutputStream.close();
            }
            
            generateChecksum(nuspecFile, layoutOutputStream);
        }


    }

    private String toBase64(byte[] digest)
    {
        byte[] encoded = Base64.getEncoder()
                               .encode(digest);
        return new String(encoded, StandardCharsets.UTF_8);
    }

    private void generateChecksum(File file,
                                  LayoutOutputStream layoutOutputStream)
        throws IOException,
               NoSuchAlgorithmException
    {

        String sha512 = layoutOutputStream.getDigestMap().get(MessageDigestAlgorithms.SHA_512);
        MessageDigestUtils.writeChecksum(file.toPath(), ".sha512", sha512);
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
