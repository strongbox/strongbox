package org.carlspring.strongbox.artifact.generator;

import org.carlspring.commons.io.RandomInputStream;
import org.carlspring.strongbox.artifact.coordinates.NpmArtifactCoordinates;
import org.carlspring.strongbox.data.PropertyUtils;
import org.carlspring.strongbox.npm.metadata.Package;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Base64;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;

public class NpmPackageGenerator
{

    private NpmArtifactCoordinates coordinates;
    private Package packageJson = new Package();
    private Path basePath = Paths.get(PropertyUtils.getHomeDirectory() + "/tmp");
    private Path packagePath;
    private Path publishJsonPath;
    private ObjectMapper mapper = new ObjectMapper();

    public static NpmPackageGenerator newInstance()
    {
        return new NpmPackageGenerator();
    }

    private NpmPackageGenerator()
    {
        super();
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

    public Package getPackageJson()
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

        return packagePath;
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

}
