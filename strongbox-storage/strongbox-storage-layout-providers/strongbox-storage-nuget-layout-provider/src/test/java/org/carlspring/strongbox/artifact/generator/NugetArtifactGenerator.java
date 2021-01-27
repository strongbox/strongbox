package org.carlspring.strongbox.artifact.generator;

import org.carlspring.strongbox.artifact.coordinates.NugetArtifactCoordinates;
import org.carlspring.strongbox.artifact.coordinates.versioning.SemanticVersion;
import org.carlspring.strongbox.io.LayoutOutputStream;
import org.carlspring.strongbox.storage.metadata.nuget.Dependencies;
import org.carlspring.strongbox.storage.metadata.nuget.Dependency;
import org.carlspring.strongbox.storage.metadata.nuget.NugetFormatException;
import org.carlspring.strongbox.storage.metadata.nuget.Nuspec;
import org.carlspring.strongbox.testing.artifact.LicenseConfiguration;
import org.carlspring.strongbox.testing.artifact.LicenseType;
import org.carlspring.strongbox.util.MessageDigestUtils;
import org.carlspring.strongbox.util.TestFileUtils;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Kate Novik.
 * @author Pablo Tirado
 */
public class NugetArtifactGenerator
        implements ArtifactGenerator
{

    private static final Logger logger = LoggerFactory.getLogger(NugetArtifactGenerator.class);

    private static final String PSMDCP_CONTENT = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" +
                                                 "<coreProperties xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:dcterms=\"http://purl.org/dc/terms/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://schemas.openxmlformats.org/package/2006/m\r\netadata/core-properties\">\r\n  " +
                                                 "<dc:creator>Carlspring</dc:creator>\r\n  " +
                                                 "<dc:description>Strongbox Nuget generated package for tests</dc:description>\r\n  " +
                                                 "<dc:identifier>%s</dc:identifier>\r\n  " +
                                                 "<version>%s</version>\r\n  <keywords>mono strongbox nuget</keywords>\r\n  " +
                                                 "<lastModifiedBy>org.carlspring.strongbox.artifact.generator.NugetArtifactGenerator</lastModifiedBy>\r\n" +
                                                 "</coreProperties>";

    private static final String RELS_CONTENT = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" +
                                               "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">\r\n  " +
                                               "<Relationship Type=\"http://schemas.microsoft.com/packaging/2010/07/manifest\" Target=\"/%s.nuspec\" Id=\"Rc20b205c579d4f85\" />\r\n  " +
                                               "<Relationship Type=\"http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties\" Target=\"/package/services/metadata/core-properties/metadata.psmdcp\" Id=\"R23f62\r\ne2778b3442e\" />\r\n" +
                                               "</Relationships>";

    private static final String PACKAGING_NUPKG = "nupkg";

    private static final int DEFAULT_BYTE_SIZE = 1000000;

    private Path basePath;

    private LicenseConfiguration[] licenses;

    public NugetArtifactGenerator(String baseDir)
    {
        this(Paths.get(baseDir));
    }

    public NugetArtifactGenerator(Path basePath)
    {
        this.basePath = basePath.normalize().toAbsolutePath();
    }

    public String getBasedir()
    {
        return basePath.normalize().toAbsolutePath().toString();
    }

    @Override
    public void setLicenses(LicenseConfiguration[] licenses) 
            throws IOException
    {
        if (!ArrayUtils.isEmpty(licenses))
        {
            if (licenses.length > 1)
            {
                throw new IOException("Nuget doesn't support multiple licenses!");
            }
        }
        this.licenses = licenses;
    }

    @Override
    public Path generateArtifact(String id,
                                 String version,
                                 long bytesSize)
            throws IOException
    {
        try
        {
            return generate(id, version, PACKAGING_NUPKG, bytesSize);
        }
        catch (NoSuchAlgorithmException | JAXBException | NugetFormatException e)
        {
            throw new IOException(e);
        }
    }

    @Override
    public Path generateArtifact(URI uri,
                                 long bytesSize)
            throws IOException
    {
        NugetArtifactCoordinates coordinates = NugetArtifactCoordinates.parse(uri.toString());
        return generateArtifact(coordinates, bytesSize);
    }

    public Path generateArtifact(NugetArtifactCoordinates coordinates,
                                 long bytesSize)
            throws IOException
    {
        return generateArtifact(coordinates.getId(), coordinates.getVersion(), coordinates.getType(), bytesSize);
    }

    public Path generateArtifact(String id,
                                 String version,
                                 String packaging,
                                 long bytesSize)
            throws IOException
    {
        try
        {
            return generate(id, version, packaging, bytesSize);
        }
        catch (NoSuchAlgorithmException | JAXBException | NugetFormatException e)
        {
            throw new IOException(e);
        }
    }

    public Path generate(String id,
                         String version,
                         String packaging,
                         String... dependencyList)
            throws IOException, NoSuchAlgorithmException, JAXBException, NugetFormatException
    {
        return generate(id, version, packaging, new Random().nextInt(DEFAULT_BYTE_SIZE), dependencyList);
    }

    public Path generate(String id,
                         String version,
                         String packaging,
                         long bytesSize,
                         String... dependencyList)
            throws IOException, NoSuchAlgorithmException, JAXBException, NugetFormatException
    {
        NugetArtifactCoordinates coordinates = new NugetArtifactCoordinates(id, version, packaging);
        Path fullPath = basePath.resolve(coordinates.buildPath()).normalize().toAbsolutePath();
        Files.createDirectories(fullPath.getParent());

        SemanticVersion semanticVersion = SemanticVersion.parse(version);
        logger.debug("Version of the {} package: {}", packaging, semanticVersion.toString());

        generate(fullPath, id, semanticVersion, bytesSize, dependencyList);

        return fullPath;
    }

    public void generate(Path packagePath,
                         String id,
                         SemanticVersion version,
                         String... dependencyList)
            throws IOException, JAXBException, NoSuchAlgorithmException, NugetFormatException
    {
        generate(packagePath, id, version, new Random().nextInt(DEFAULT_BYTE_SIZE), dependencyList);
    }

    public void generate(Path packagePath,
                         String id,
                         SemanticVersion version,
                         long bytesSize,
                         String... dependencyList)
            throws IOException, JAXBException, NoSuchAlgorithmException, NugetFormatException
    {
        Nuspec nuspec = generateNuspec(id, version, dependencyList);

        createArchive(nuspec, packagePath, bytesSize);

        generateNuspecFile(nuspec);
    }

    public void createArchive(Nuspec nuspec,
                              Path packagePath,
                              long bytesSize)
            throws IOException,
                   JAXBException,
                   NoSuchAlgorithmException
    {
        // Make sure the artifact's parent directory exists before writing the model.
        Files.createDirectories(packagePath.getParent());

        try (OutputStream fileOutputStream = Files.newOutputStream(packagePath))
        {
            try (LayoutOutputStream layoutOutputStream = new LayoutOutputStream(fileOutputStream))
            {
                layoutOutputStream.addAlgorithm(MessageDigestAlgorithms.SHA_512);
                layoutOutputStream.setDigestStringifier(this::toBase64);

                try (ZipOutputStream zos = new ZipOutputStream(layoutOutputStream))
                {
                    addNugetNuspecFile(nuspec, zos);
                    TestFileUtils.generateFile(zos, bytesSize, "file-with-given-size");

                    String id = nuspec.getId();

                    SemanticVersion version = nuspec.getVersion();
                    createMetadata(id, version.toString(), zos);

                    createContentType(zos);
                    createRels(id, zos);
                    copyLicenseFiles(zos);
                }
                generateChecksum(packagePath, layoutOutputStream);
            }
        }
    }

    /**
     * Nuget packages store license information in .nuspec metadata file
     * @see {https://docs.microsoft.com/en-us/nuget/reference/nuspec#licenseurl}
     */
    private void copyLicenseFiles(ZipOutputStream zos)
            throws IOException
    {
        if (!ArrayUtils.isEmpty(licenses))
        {
            LicenseConfiguration license = licenses[0];
            ZipEntry zipEntry = new ZipEntry(license.destinationPath());
            zos.putNextEntry(zipEntry);

            copyLicenseFile(license.license().getLicenseFileSourcePath(), zos);
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

    private void createContentType(ZipOutputStream zos)
            throws IOException
    {
        ZipEntry ze = new ZipEntry("[Content_Types].xml");
        zos.putNextEntry(ze);

        try (InputStream is = getClass().getResourceAsStream("[Content_Types].xml"))
        {
            byte[] buffer = new byte[4096];
            int len;
            while ((len = is.read(buffer)) > 0)
            {
                zos.write(buffer, 0, len);
            }
        }
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
            throws IOException, JAXBException
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

    private Nuspec generateNuspec(String id,
                                  SemanticVersion version,
                                  String... dependencyList)
            throws NugetFormatException
    {
        Nuspec nuspec = new Nuspec();
        Nuspec.Metadata metadata = nuspec.getMetadata();
        metadata.id = id;
        metadata.version = version;
        metadata.authors = "carlspring";
        metadata.owners = "Carlspring Consulting &amp; Development Ltd.";
        metadata.description = "Strongbox Nuget package for tests";
        String licenseUrl = LicenseType.NONE.getUrl();
        if (!ArrayUtils.isEmpty(licenses))
        {
            licenseUrl = licenses[0].license().getUrl();
        }
        metadata.licenseUrl = licenseUrl;

        if (dependencyList != null)
        {
            metadata.dependencies = new Dependencies();
            metadata.dependencies.dependencies = new ArrayList<>();

            for (String dependency : dependencyList)
            {
                metadata.dependencies.dependencies.add(Dependency.parseString(dependency));
            }
        }

        return nuspec;
    }

    private void generateNuspecFile(Nuspec nuspec)
            throws IOException, JAXBException, NoSuchAlgorithmException
    {
        String packageId = nuspec.getId();
        String packageVersion = nuspec.getVersion().toString();

        NugetArtifactCoordinates nac = new NugetArtifactCoordinates(packageId, packageVersion, "nuspec");
        Path basePath = Paths.get(getBasedir()).normalize().toAbsolutePath();
        Path nuspecPath = basePath.resolve(nac.buildPath()).normalize().toAbsolutePath();
        Files.createDirectories(nuspecPath.getParent());

        try (OutputStream fileOutputStream = Files.newOutputStream(nuspecPath))
        {
            LayoutOutputStream layoutOutputStream = new LayoutOutputStream(fileOutputStream);
            layoutOutputStream.addAlgorithm(MessageDigestAlgorithms.SHA_512);
            layoutOutputStream.setDigestStringifier(this::toBase64);

            try
            {
                nuspec.saveTo(layoutOutputStream);
            }
            finally
            {
                layoutOutputStream.close();
            }

            generateChecksum(nuspecPath, layoutOutputStream);
        }
    }

    private String toBase64(byte[] digest)
    {
        byte[] encoded = Base64.getEncoder().encode(digest);

        return new String(encoded, StandardCharsets.UTF_8);
    }

    private void generateChecksum(Path artifactPath,
                                  LayoutOutputStream layoutOutputStream)
            throws IOException
    {
        String sha512 = layoutOutputStream.getDigestMap().get(MessageDigestAlgorithms.SHA_512);
        MessageDigestUtils.writeChecksum(artifactPath, ".sha512", sha512);
    }

}
