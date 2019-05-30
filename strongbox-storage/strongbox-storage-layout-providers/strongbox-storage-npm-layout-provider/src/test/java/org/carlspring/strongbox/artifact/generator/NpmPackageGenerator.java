package org.carlspring.strongbox.artifact.generator;

import org.carlspring.commons.encryption.EncryptionAlgorithmsEnum;
import org.carlspring.commons.io.MultipleDigestInputStream;
import org.carlspring.commons.io.RandomInputStream;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.artifact.coordinates.NpmArtifactCoordinates;
import org.carlspring.strongbox.npm.metadata.Dist;
import org.carlspring.strongbox.npm.metadata.PackageVersion;

import java.io.*;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NpmPackageGenerator implements ArtifactGenerator
{
    private static final Logger logger = LoggerFactory.getLogger(NpmPackageGenerator.class);

    private NpmArtifactCoordinates coordinates;

    private PackageVersion packageJson = new PackageVersion();

    private Path basePath;

    private Path packagePath;

    private Path publishJsonPath;

    private ObjectMapper mapper = new ObjectMapper();

    private static final String PACKAGING_JAR = "jar";

    public NpmPackageGenerator(String basedir)
    {
        super();
        this.basePath = Paths.get(basedir);

        packageJson.setDist(new Dist());
    }

    public NpmPackageGenerator of(NpmArtifactCoordinates c)
    {
        packageJson.setName(c.getId());
        packageJson.setVersion(c.getVersion());

        this.coordinates = c;

        return this;
    }

    public NpmPackageGenerator in(Path path)
    {
        this.basePath = path;
        return this;
    }

    public PackageVersion getPackageJson()
    {
        return packageJson;
    }

    public Path getPackagePath()
    {
        return packagePath;
    }

    public Path getPublishJsonPath()
    {
        return publishJsonPath;
    }

    public Path buildPackage()
        throws IOException
    {
        Files.createDirectories(basePath);

        packagePath = basePath.resolve(coordinates.toPath());

        Files.createDirectories(packagePath.getParent());

        try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(packagePath, StandardOpenOption.CREATE)))
        {
            GzipCompressorOutputStream gzipOut = new GzipCompressorOutputStream(out);
            TarArchiveOutputStream tarOut = new TarArchiveOutputStream(gzipOut);

            writeContent(tarOut);
            writePackageJson(tarOut);

            tarOut.close();
            gzipOut.close();
        }

        calculateChecksum();

        return packagePath;
    }

    private void calculateChecksum()
        throws IOException
    {
        MessageDigest crypt;
        try
        {
            crypt = MessageDigest.getInstance("SHA-1");
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new UndeclaredThrowableException(e);
        }

        crypt.reset();
        crypt.update(Files.readAllBytes(packagePath));

        String shasum = Base64.getEncoder().encodeToString(crypt.digest());
        packageJson.getDist().setShasum(shasum);
    }

    private void writePackageJson(TarArchiveOutputStream tarOut)
        throws IOException
    {
        Path packageJsonPath = packagePath.getParent().resolve("package.json");
        try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(packageJsonPath, StandardOpenOption.CREATE)))
        {
            out.write(mapper.writeValueAsBytes(packageJson));
        }

        TarArchiveEntry entry = new TarArchiveEntry(packageJsonPath.toFile(), "package.json");
        tarOut.putArchiveEntry(entry);

        Files.copy(packageJsonPath, tarOut);

        tarOut.closeArchiveEntry();
    }

    private void writeContent(TarArchiveOutputStream tarOut)
        throws IOException,
               UnsupportedEncodingException
    {
        Path indexJsPath = packagePath.getParent().resolve("index.js");
        try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(indexJsPath, StandardOpenOption.CREATE)))
        {
            out.write("data = \"".getBytes("UTF-8"));

            OutputStream dataOut = Base64.getEncoder().wrap(out);
            RandomInputStream ris = new RandomInputStream(true, 1000000);
            byte[] buffer = new byte[4096];
            int len;
            while ((len = ris.read(buffer)) > 0)
            {
                dataOut.write(buffer, 0, len);
            }
            ris.close();

            out.write("\";".getBytes("UTF-8"));
        }

        TarArchiveEntry entry = new TarArchiveEntry(indexJsPath.toFile(), "index.js");
        tarOut.putArchiveEntry(entry);

        Files.copy(indexJsPath, tarOut);

        tarOut.closeArchiveEntry();
    }

    public Path buildPublishJson()
        throws IOException
    {
        if (packagePath == null)
        {
            buildPackage();
        }
        Path publishJsonPath = packagePath.resolveSibling("publish.json");
        try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(publishJsonPath, StandardOpenOption.CREATE)))
        {
            JsonFactory jfactory = new JsonFactory();
            JsonGenerator jGenerator = jfactory.createGenerator(out, JsonEncoding.UTF8);

            jGenerator.writeStartObject();
            jGenerator.writeStringField("_id", packageJson.getName());
            jGenerator.writeStringField("name", packageJson.getName());

            // versions
            jGenerator.writeFieldName("versions");
            jGenerator.writeStartObject();
            jGenerator.writeFieldName(packageJson.getVersion());
            jGenerator.writeStartObject();

            // version
            jGenerator.writeStringField("name", packageJson.getName());
            jGenerator.writeStringField("version", packageJson.getVersion());

            // dist
            jGenerator.writeFieldName("dist");
            jGenerator.writeStartObject();
            jGenerator.writeStringField("shasum", packageJson.getDist().getShasum());
            jGenerator.writeEndObject();

            jGenerator.writeEndObject();
            jGenerator.writeEndObject();

            // _attachments
            jGenerator.writeFieldName("_attachments");

            jGenerator.writeStartObject();
            jGenerator.writeFieldName(coordinates.toPath());

            jGenerator.writeStartObject();
            jGenerator.writeStringField("content_type", "application/octet-stream");

            jGenerator.writeFieldName("data");
            byte[] packageData = Files.readAllBytes(packagePath);
            jGenerator.writeBinary(packageData);

            jGenerator.writeNumberField("length", packageData.length);

            jGenerator.writeEndObject();
            jGenerator.writeEndObject();

            jGenerator.writeEndObject();

            jGenerator.flush();
        }

        return publishJsonPath;
    }

    @Override
    public Path generateArtifact(String id,
                                 String version,
                                 int size)
            throws IOException
    {
        Artifact artifact = ArtifactUtils.getArtifactFromGAVTC(String.format("%s:%s", id, version));

        return generateArtifact(artifact);
    }

    @Override
    public Path generateArtifact(URI uri,
                                 int size)
            throws IOException
    {
        Artifact artifact = ArtifactUtils.convertPathToArtifact(uri.toString());

        return generateArtifact(artifact);
    }

    private Path generateArtifact(Artifact artifact)
            throws IOException
    {
        try
        {
            generate(artifact);
        }
        catch (NoSuchAlgorithmException| XmlPullParserException e)
        {
            throw new IOException(e);
        }

        return basePath.resolve(ArtifactUtils.convertArtifactToPath(artifact));
    }

    public void generate(Artifact artifact)
            throws IOException,
                   XmlPullParserException,
                   NoSuchAlgorithmException
    {
        generatePom(artifact, PACKAGING_JAR);
        createArchive(artifact);
    }

    public void createArchive(Artifact artifact)
            throws NoSuchAlgorithmException,
                   IOException
    {
        File artifactFile = basePath.resolve(ArtifactUtils.convertArtifactToPath(artifact)).toFile();

        // Make sure the artifact's parent directory exists before writing the model.
        //noinspection ResultOfMethodCallIgnored
        artifactFile.getParentFile().mkdirs();

        try(ZipOutputStream zos = new ZipOutputStream(newOutputStream(artifactFile)))
        {
            createMavenPropertiesFile(artifact, zos);
            addMavenPomFile(artifact, zos);
            createRandomSizeFile(zos);

            zos.flush();
        }
        generateChecksumsForArtifact(artifactFile);
    }

    protected OutputStream newOutputStream(File artifactFile)
            throws IOException
    {
        return new FileOutputStream(artifactFile);
    }

    private void addMavenPomFile(Artifact artifact, ZipOutputStream zos) throws IOException
    {
        final Artifact pomArtifact = ArtifactUtils.getPOMArtifact(artifact);
        File pomFile = basePath.resolve(ArtifactUtils.convertArtifactToPath(pomArtifact)).toFile();

        ZipEntry ze = new ZipEntry("META-INF/maven/" +
                                   artifact.getGroupId() + "/" +
                                   artifact.getArtifactId() + "/" +
                                   "pom.xml");
        zos.putNextEntry(ze);

        try (FileInputStream fis = new FileInputStream(pomFile))
        {

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

    private void createMavenPropertiesFile(Artifact artifact, ZipOutputStream zos)
            throws IOException
    {
        ZipEntry ze = new ZipEntry("META-INF/maven/" +
                                   artifact.getGroupId() + "/" +
                                   artifact.getArtifactId() + "/" +
                                   "pom.properties");
        zos.putNextEntry(ze);

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
            zos.write(buffer, 0, len);
        }

        bais.close();
        zos.closeEntry();
    }

    private void createRandomSizeFile(ZipOutputStream zos)
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

    public void generatePom(Artifact artifact, String packaging)
            throws IOException,
                   XmlPullParserException,
                   NoSuchAlgorithmException
    {
        final Artifact pomArtifact = ArtifactUtils.getPOMArtifact(artifact);
        File pomFile = basePath.resolve(ArtifactUtils.convertArtifactToPath(pomArtifact)).toFile();

        // Make sure the artifact's parent directory exists before writing the model.
        //noinspection ResultOfMethodCallIgnored
        pomFile.getParentFile().mkdirs();

        Model model = new Model();
        model.setGroupId(artifact.getGroupId());
        model.setArtifactId(artifact.getArtifactId());
        model.setVersion(artifact.getVersion());
        model.setPackaging(packaging);

        logger.debug("Generating pom file for " + artifact.toString() + "...");

        try (OutputStreamWriter pomFileWriter = new OutputStreamWriter(newOutputStream(pomFile)))
        {
            MavenXpp3Writer xpp3Writer = new MavenXpp3Writer();
            xpp3Writer.write(pomFileWriter, model);
        }

        generateChecksumsForArtifact(pomFile);
    }

    private void generateChecksumsForArtifact(File artifactFile)
            throws NoSuchAlgorithmException, IOException
    {
        try (InputStream is = new FileInputStream(artifactFile);
             MultipleDigestInputStream mdis = new MultipleDigestInputStream(is))
        {
            int size = 4096;
            byte[] bytes = new byte[size];

            //noinspection StatementWithEmptyBody
            while (mdis.read(bytes, 0, size) != -1) ;

            mdis.close();

            String md5 = mdis.getMessageDigestAsHexadecimalString(EncryptionAlgorithmsEnum.MD5.getAlgorithm());
            String sha1 = mdis.getMessageDigestAsHexadecimalString(EncryptionAlgorithmsEnum.SHA1.getAlgorithm());

            Path artifactPath = artifactFile.toPath();

            Path checksumPath = artifactPath.resolveSibling(artifactPath.getFileName() + EncryptionAlgorithmsEnum.MD5.getExtension());
            try (OutputStream os = newOutputStream(checksumPath.toFile()))
            {
                IOUtils.write(md5, os, Charset.forName("UTF-8"));
                os.flush();
            }

            checksumPath = artifactPath.resolveSibling(artifactPath.getFileName() + EncryptionAlgorithmsEnum.SHA1.getExtension());
            try (OutputStream os = newOutputStream(checksumPath.toFile()))
            {
                IOUtils.write(sha1, os, Charset.forName("UTF-8"));
                os.flush();
            }
        }
    }
}

