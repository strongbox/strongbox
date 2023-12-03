package org.carlspring.strongbox.artifact.generator;

import org.carlspring.commons.encryption.EncryptionAlgorithmsEnum;
import org.carlspring.commons.io.MultipleDigestInputStream;
import org.carlspring.commons.io.MultipleDigestOutputStream;
import org.carlspring.strongbox.artifact.MavenArtifactUtils;
import org.carlspring.strongbox.testing.artifact.LicenseConfiguration;
import org.carlspring.strongbox.testing.artifact.MavenArtifactTestUtils;
import org.carlspring.strongbox.testing.artifact.MavenLicenseConfiguration;
import org.carlspring.strongbox.util.TestFileUtils;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Writer;
import org.apache.maven.model.License;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.WriterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * @author mtodorov
 */
public class MavenArtifactGenerator implements ArtifactGenerator
{

    private static final Logger logger = LoggerFactory.getLogger(MavenArtifactGenerator.class);

    public static final String PACKAGING_JAR = "jar";

    protected Path basedir;

    private MavenLicenseConfiguration[] licenses;


    public MavenArtifactGenerator()
    {
    }

    public MavenArtifactGenerator(String basedir)
    {
        this.basedir = Paths.get(basedir);
    }

    public MavenArtifactGenerator(File basedir)
    {
        this.basedir = basedir.toPath();
    }

    public MavenArtifactGenerator(Path basedir)
    {
        this.basedir = basedir;
    }

    @Override
    public Path generateArtifact(String id,
                                 String version,
                                 long bytesSize)
            throws IOException
    {
        Artifact artifact = MavenArtifactTestUtils.getArtifactFromGAVTC(String.format("%s:%s", id, version));
        return generateArtifact(artifact, bytesSize);
    }

    @Override
    public Path generateArtifact(URI uri,
                                 long bytesSize)
            throws IOException
    {
        Artifact artifact = MavenArtifactUtils.convertPathToArtifact(uri.toString());
        return generateArtifact(artifact, bytesSize);
    }

    private Path generateArtifact(Artifact artifact,
                                  long bytesSize)
            throws IOException
    {
        try
        {
            generate(artifact, bytesSize);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new IOException(e);
        }

        return basedir.resolve(MavenArtifactUtils.convertArtifactToPath(artifact));
    }

    public void generate(String ga, String packaging, String... versions)
            throws IOException,
                   NoSuchAlgorithmException
    {
        if (packaging == null)
        {
            packaging = PACKAGING_JAR;
        }

        for (String version : versions)
        {
            Artifact artifact = MavenArtifactTestUtils.getArtifactFromGAVTC(ga + ":" + version);
            artifact.setFile(basedir.resolve("/" + MavenArtifactUtils.convertArtifactToPath(artifact)).toFile());

            generate(artifact, packaging);
        }
    }

    public void generate(String ga, String... versions)
            throws IOException,
                   NoSuchAlgorithmException
    {
        for (String version : versions)
        {
            Artifact artifact = MavenArtifactTestUtils.getArtifactFromGAVTC(ga + ":" + version);
            artifact.setFile(basedir.resolve("/" + MavenArtifactUtils.convertArtifactToPath(artifact)).toFile());

            generate(artifact);
        }
    }

    public void generate(Artifact artifact)
            throws IOException,
                   NoSuchAlgorithmException
    {
        generate(artifact, new Random().nextInt(ArtifactGenerator.DEFAULT_BYTES_SIZE));
    }

    public void generate(Artifact artifact, String packaging)
            throws IOException,
                   NoSuchAlgorithmException
    {
        generatePom(artifact, packaging);
        createArchive(artifact);
    }

    public void generate(Artifact artifact, long bytesSize)
            throws IOException,
                   NoSuchAlgorithmException
    {
        generatePom(artifact, PACKAGING_JAR);
        createArchive(artifact, bytesSize);
    }

    public void createArchive(Artifact artifact)
            throws IOException, NoSuchAlgorithmException
    {
        createArchive(artifact, new Random().nextInt(ArtifactGenerator.DEFAULT_BYTES_SIZE));
    }

    public void createArchive(Artifact artifact,
                              long bytesSize)
            throws NoSuchAlgorithmException,
                   IOException
    {
        Path artifactPath = basedir.resolve(MavenArtifactUtils.convertArtifactToPath(artifact));

        // Make sure the artifact's parent directory exists before writing the model.
        Files.createDirectories(artifactPath.getParent());

        try (JarOutputStream zos = new JarOutputStream(Files.newOutputStream(artifactPath), createManifest()))
        {
            createMavenPropertiesFile(artifact, zos);
            addMavenPomFile(artifact, zos);
            TestFileUtils.generateFile(zos, bytesSize);
            copyLicenseFiles(zos);
            zos.flush();
        }

        generateChecksumsForArtifact(artifactPath);
    }

    public Manifest createManifest()
    {
        Manifest manifest = new Manifest();
        Attributes mainAttributes = manifest.getMainAttributes();
        mainAttributes.put(Attributes.Name.MANIFEST_VERSION, "1.0.0");
        mainAttributes.put(new Attributes.Name("Created-By"), "strongbox-maven-test-artifact-generator");

        if (licenses != null)
        {
            String licenseUrls = "";
            for (MavenLicenseConfiguration licenseConfiguration : licenses)
            {
                if (StringUtils.isEmpty(licenseUrls))
                {
                    licenseUrls = licenseConfiguration.license().getUrl();
                }
                else
                {
                    licenseUrls += ", " + licenseConfiguration.license().getUrl();
                }
            }

            if (!StringUtils.isEmpty(licenseUrls))
            {
                mainAttributes.put(new Attributes.Name("Bundle-License"), licenseUrls);
            }
        }

        return manifest;
    }

    private void copyLicenseFiles(JarOutputStream jos)
            throws IOException
    {
        if (licenses != null && licenses.length > 0)
        {
            for (MavenLicenseConfiguration licenseConfiguration : licenses)
            {
                JarEntry jarEntry = new JarEntry(licenseConfiguration.destinationPath());
                jos.putNextEntry(jarEntry);

                copyLicenseFile(licenseConfiguration.license().getLicenseFileSourcePath(), jos);
                jos.closeEntry();
            }
        }
    }

    protected OutputStream newOutputStream(File artifactFile)
            throws IOException
    {
        return Files.newOutputStream(artifactFile.toPath());
    }

    public void createMetadata(Metadata metadata,
                               String metadataPathStr)
            throws NoSuchAlgorithmException, IOException
    {
        try
        {
            Path metadataPath = basedir.resolve(metadataPathStr);

            if (Files.exists(metadataPath))
            {
                Files.delete(metadataPath);
            }

            try (OutputStream os = new MultipleDigestOutputStream(metadataPath, Files.newOutputStream(metadataPath)))
            {
                Writer writer = WriterFactory.newXmlWriter(os);
                MetadataXpp3Writer mappingWriter = new MetadataXpp3Writer();
                mappingWriter.write(writer, metadata);

                os.flush();
            }
        }
        finally
        {
            generateChecksumsForArtifact(basedir.resolve(metadataPathStr));
        }
    }

     private void addMavenPomFile(Artifact artifact,
                                 JarOutputStream zos)
            throws IOException
    {
        final Artifact pomArtifact = MavenArtifactTestUtils.getPOMArtifact(artifact);
        Path pomPath = basedir.resolve(MavenArtifactUtils.convertArtifactToPath(pomArtifact));

        try (InputStream fis = Files.newInputStream(pomPath))
        {
            JarEntry ze = new JarEntry("META-INF/maven/" +
                                       artifact.getGroupId() + "/" +
                                       artifact.getArtifactId() + "/" +
                                       "pom.xml");
            zos.putNextEntry(ze);

            byte[] buffer = new byte[4096];
            int len;
            while ((len = fis.read(buffer)) > 0)
            {
                zos.write(buffer, 0, len);
            }
        }
        finally
        {
            zos.closeEntry();
        }
    }

    public static void createMavenPropertiesFile(Artifact artifact,
                                                 JarOutputStream jos)
            throws IOException
    {
        JarEntry ze = new JarEntry("META-INF/maven/" +
                                   artifact.getGroupId() + "/" +
                                   artifact.getArtifactId() + "/" +
                                   "pom.properties");
        jos.putNextEntry(ze);

        Properties properties = new Properties();
        properties.setProperty("groupId", artifact.getGroupId());
        properties.setProperty("artifactId", artifact.getArtifactId());
        properties.setProperty("version", artifact.getVersion());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        properties.store(baos, null);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

        byte[] buffer = new byte[4096];
        int len;
        while ((len = bais.read(buffer)) > 0)
        {
            jos.write(buffer, 0, len);
        }

        bais.close();
        jos.closeEntry();
    }

    public void generatePom(Artifact artifact,
                            String packaging)
            throws IOException,
                   NoSuchAlgorithmException
    {
        final Artifact pomArtifact = MavenArtifactTestUtils.getPOMArtifact(artifact);
        Path artifactPath = basedir.resolve(MavenArtifactUtils.convertArtifactToPath(pomArtifact));

        // Make sure the artifact's parent directory exists before writing the model.
        Files.createDirectories(artifactPath.getParent());

        Model model = new Model();
        model.setGroupId(artifact.getGroupId());
        model.setArtifactId(artifact.getArtifactId());
        model.setVersion(artifact.getVersion());
        model.setPackaging(packaging);

        setLicensesInPom(model);

        logger.debug("Generating pom file for {}...", artifact);

        try (OutputStreamWriter pomFileWriter = new OutputStreamWriter(Files.newOutputStream(artifactPath)))
        {
            MavenXpp3Writer xpp3Writer = new MavenXpp3Writer();
            xpp3Writer.write(pomFileWriter, model);
        }

        generateChecksumsForArtifact(artifactPath);
    }

    private void setLicensesInPom(Model model)
    {
        if (licenses != null && licenses.length > 0)
        {
            List<License> pomLicenses = new ArrayList<>();

            for (MavenLicenseConfiguration licenseConfiguration : licenses)
            {
                License license = new License();
                license.setName(licenseConfiguration.license().getName());
                license.setUrl(licenseConfiguration.license().getUrl());

                pomLicenses.add(license);
            }

            model.setLicenses(pomLicenses);
        }
    }

    private void generateChecksumsForArtifact(Path artifactPath)
            throws NoSuchAlgorithmException, IOException
    {
        try (InputStream is = Files.newInputStream(artifactPath);
             MultipleDigestInputStream mdis = new MultipleDigestInputStream(is))
        {
            int size = 4096;
            byte[] bytes = new byte[size];

            //noinspection StatementWithEmptyBody
            while (mdis.read(bytes, 0, size) != -1) ;

            mdis.close();

            String md5 = mdis.getMessageDigestAsHexadecimalString(EncryptionAlgorithmsEnum.MD5.getAlgorithm());
            String sha1 = mdis.getMessageDigestAsHexadecimalString(EncryptionAlgorithmsEnum.SHA1.getAlgorithm());

            Path checksumPath = artifactPath.resolveSibling(artifactPath.getFileName() + EncryptionAlgorithmsEnum.MD5.getExtension());
            try (OutputStream os = Files.newOutputStream(checksumPath))
            {
                IOUtils.write(md5, os, StandardCharsets.UTF_8);
                os.flush();
            }

            checksumPath = artifactPath.resolveSibling(artifactPath.getFileName() + EncryptionAlgorithmsEnum.SHA1.getExtension());
            try (OutputStream os = Files.newOutputStream(checksumPath))
            {
                IOUtils.write(sha1, os, StandardCharsets.UTF_8);
                os.flush();
            }
        }
    }

    public String getBasedir()
    {
        return basedir.toAbsolutePath().toString();
    }

    public Path getBasedirPath()
    {
        return basedir;
    }

    public MavenLicenseConfiguration[] getLicenses()
    {
        return licenses;
    }

    public void setLicenses(MavenLicenseConfiguration[] licenses)
    {
        this.licenses = licenses;
    }

    @Override
    public void setLicenses(LicenseConfiguration[] licenses)
    {
        this.licenses = (MavenLicenseConfiguration[]) licenses;
    }

}
