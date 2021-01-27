package org.carlspring.strongbox.artifact.generator;

import org.carlspring.strongbox.artifact.coordinates.NpmArtifactCoordinates;
import org.carlspring.strongbox.npm.metadata.Dist;
import org.carlspring.strongbox.npm.metadata.License;
import org.carlspring.strongbox.npm.metadata.PackageVersion;
import org.carlspring.strongbox.testing.artifact.LicenseConfiguration;
import org.carlspring.strongbox.util.TestFileUtils;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

public class NpmArtifactGenerator
        implements ArtifactGenerator
{

    private NpmArtifactCoordinates coordinates;

    private PackageVersion packageJson = new PackageVersion();

    private Path basePath;

    private Path packagePath;

    private ObjectMapper mapper = new ObjectMapper();

    private LicenseConfiguration[] licenses;

    public NpmArtifactGenerator(String basedir)
    {
        this(Paths.get(basedir));
    }

    public NpmArtifactGenerator(Path basePath)
    {
        super();
        this.basePath = basePath;

        packageJson.setDist(new Dist());
    }

    public NpmArtifactGenerator of(NpmArtifactCoordinates coordinates)
    {
        packageJson.setName(coordinates.getId());
        packageJson.setVersion(coordinates.getVersion());

        this.coordinates = coordinates;

        return this;
    }

    public NpmArtifactGenerator in(Path path)
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

    public void setPackagePath(Path packagePath)
    {
        this.packagePath = packagePath;
    }

    @Override
    public void setLicenses(LicenseConfiguration[] licenses)
    {
        this.licenses = licenses;
    }

    public Path buildPackage(long bytesSize)
            throws IOException
    {
        Files.createDirectories(basePath);

        packagePath = basePath.resolve(coordinates.buildPath());

        Files.createDirectories(packagePath.getParent());

        try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(packagePath, StandardOpenOption.CREATE));
             GzipCompressorOutputStream gzipOut = new GzipCompressorOutputStream(out);
             TarArchiveOutputStream tarOut = new TarArchiveOutputStream(gzipOut))
        {
            writeContent(tarOut, bytesSize);
            writePackageJson(tarOut);
            copyLicenseFiles(tarOut);
        }

        calculateChecksum();

        return packagePath;
    }

    private void copyLicenseFiles(TarArchiveOutputStream tarOut)
            throws IOException
    {
        if (!ArrayUtils.isEmpty(licenses))
        {
            for (LicenseConfiguration licenseConfiguration : licenses)
            {
                TarArchiveEntry entry = new TarArchiveEntry(licenseConfiguration.destinationPath());
                entry.setSize(getLicenseFileSize(licenseConfiguration));
                tarOut.putArchiveEntry(entry);

                copyLicenseFile(licenseConfiguration.license().getLicenseFileSourcePath(), tarOut);
                tarOut.closeArchiveEntry();
            }
        }
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
            throw new IOException(e);
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
        
        try (OutputStream outputStream = Files.newOutputStream(packageJsonPath, StandardOpenOption.CREATE);
             BufferedOutputStream out = new BufferedOutputStream(outputStream))
        {
            populateNpmLicensesinPackageJson();

            out.write(mapper.writeValueAsBytes(packageJson));
        }

        TarArchiveEntry entry = new TarArchiveEntry(packageJsonPath.toFile(), "package.json");
        tarOut.putArchiveEntry(entry);

        Files.copy(packageJsonPath, tarOut);

        tarOut.closeArchiveEntry();
    }

    /**
     * NPM packages store license information in package.json metadata file
     * @see {https://docs.npmjs.com/cli/v6/configuring-npm/package-json#license}
     */
    private void populateNpmLicensesinPackageJson()
    {
        if (!ArrayUtils.isEmpty(licenses))
        {
            List<License> npmLicenses = Arrays.asList(licenses)
                                              .stream()
                                              .map(licenseConfig -> {

                                                  License npmLicense = new License();
                                                  npmLicense.setType(licenseConfig.license().getName());
                                                  npmLicense.setUrl(licenseConfig.license().getUrl());
                                                  return npmLicense;
                                              })
                                              .collect(Collectors.toList());

            packageJson.setLicenses(npmLicenses);
        }
    }

    private void writeContent(TarArchiveOutputStream tarOut,
                              long bytesSize)
            throws IOException
    {
        Path indexJsPath = packagePath.getParent().resolve("index.js");
        try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(indexJsPath, StandardOpenOption.CREATE)))
        {
            TestFileUtils.generateFile(out, bytesSize);
        }

        TarArchiveEntry entry = new TarArchiveEntry(indexJsPath.toFile(), "index.js");
        tarOut.putArchiveEntry(entry);

        Files.copy(indexJsPath, tarOut);
        Files.delete(indexJsPath);

        tarOut.closeArchiveEntry();
    }

    public Path generateArtifact(NpmArtifactCoordinates coordinates,
                                 long bytesSize)
            throws IOException
    {
        return this.of(coordinates).buildPackage(bytesSize);
    }

    @Override
    public Path generateArtifact(URI uri,
                                 long bytesSize)
            throws IOException
    {
        return this.of(NpmArtifactCoordinates.parse(uri.toString())).buildPackage(bytesSize);
    }

    @Override
    public Path generateArtifact(String id,
                                 String version,
                                 long bytesSize)
            throws IOException
    {
        this.of(NpmArtifactCoordinates.of(id, version)).buildPublishJson(bytesSize);
        return getPackagePath();
    }

    public Path buildPublishJson(long bytesSize)
            throws IOException
    {
        if (packagePath == null)
        {
            buildPackage(bytesSize);
        }

        Path publishJsonPath = packagePath.resolveSibling("publish.json");
        try (OutputStream outputStream = Files.newOutputStream(publishJsonPath, StandardOpenOption.CREATE);
             BufferedOutputStream out = new BufferedOutputStream(outputStream))
        {
            JsonFactory jFactory = new JsonFactory();
            JsonGenerator jGenerator = jFactory.createGenerator(out, JsonEncoding.UTF8);

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
            jGenerator.writeFieldName(coordinates.buildPath());

            jGenerator.writeStartObject();
            jGenerator.writeStringField("content_type", MediaType.APPLICATION_OCTET_STREAM_VALUE);

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

}
