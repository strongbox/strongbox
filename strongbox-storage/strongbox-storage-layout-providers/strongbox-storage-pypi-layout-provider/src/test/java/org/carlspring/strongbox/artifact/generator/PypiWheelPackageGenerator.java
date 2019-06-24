package org.carlspring.strongbox.artifact.generator;

import org.carlspring.commons.io.RandomInputStream;
import org.carlspring.strongbox.artifact.coordinates.PypiWheelArtifactCoordinates;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;

public class PypiWheelPackageGenerator
{

//    private PypiWheelArtifactCoordinates coordinates;
//
//    private Path basePath;
//
//    private Path packagePath;
//
//    private Path publishJsonPath;
//
//    private ObjectMapper mapper = new ObjectMapper();
//
//
//    public PypiWheelPackageGenerator(String basedir)
//    {
//        super();
//        this.basePath = Paths.get(basedir);
//
////        packageJson.setDist(new Dist());
//    }
//
//    public PypiWheelPackageGenerator of(PypiWheelArtifactCoordinates c)
//    {
//        packageJson.setName(c.getId());
//        packageJson.setVersion(c.getVersion());
//
//        this.coordinates = c;
//
//        return this;
//    }
//
//    public PypiWheelPackageGenerator in(Path path)
//    {
//        this.basePath = path;
//
//        return this;
//    }
//
////    public PackageVersion getPackageJson()
////    {
////        return packageJson;
////    }
//
//    public Path getPackagePath()
//    {
//        return packagePath;
//    }
//
//    public Path getPublishJsonPath()
//    {
//        return publishJsonPath;
//    }
//
//    public Path buildPackage()
//        throws IOException
//    {
//        Files.createDirectories(basePath);
//
//        packagePath = basePath.resolve(coordinates.toPath());
//
//        Files.createDirectories(packagePath.getParent());
//
//        try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(packagePath, StandardOpenOption.CREATE)))
//        {
//            GzipCompressorOutputStream gzipOut = new GzipCompressorOutputStream(out);
//            TarArchiveOutputStream tarOut = new TarArchiveOutputStream(gzipOut);
//
//            writeContent(tarOut);
//            writePackageJson(tarOut);
//
//            tarOut.close();
//            gzipOut.close();
//        }
//
//        calculateChecksum();
//
//        return packagePath;
//    }
//
//    private void calculateChecksum()
//        throws IOException
//    {
//        MessageDigest crypt;
//        try
//        {
//            crypt = MessageDigest.getInstance("SHA-256");
//        }
//        catch (NoSuchAlgorithmException e)
//        {
//            throw new UndeclaredThrowableException(e);
//        }
//
//        crypt.reset();
//        crypt.update(Files.readAllBytes(packagePath));
//
//        String shasum = Base64.getEncoder().encodeToString(crypt.digest());
//
//        packageJson.getDist().setShasum(shasum);
//    }
//
//    private void writePackageJson(TarArchiveOutputStream tarOut)
//        throws IOException
//    {
//        Path packageJsonPath = packagePath.getParent().resolve("package.json");
//        try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(packageJsonPath, StandardOpenOption.CREATE)))
//        {
//            out.write(mapper.writeValueAsBytes(packageJson));
//        }
//
//        TarArchiveEntry entry = new TarArchiveEntry(packageJsonPath.toFile(), "package.json");
//        tarOut.putArchiveEntry(entry);
//
//        Files.copy(packageJsonPath, tarOut);
//
//        tarOut.closeArchiveEntry();
//    }
//
//    private void writeContent(TarArchiveOutputStream tarOut)
//        throws IOException
//    {
//        Path indexJsPath = packagePath.getParent().resolve("index.js");
//        try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(indexJsPath, StandardOpenOption.CREATE)))
//        {
//            out.write("data = \"".getBytes("UTF-8"));
//
//            OutputStream dataOut = Base64.getEncoder().wrap(out);
//            RandomInputStream ris = new RandomInputStream(true, 1000000);
//            byte[] buffer = new byte[4096];
//            int len;
//            while ((len = ris.read(buffer)) > 0)
//            {
//                dataOut.write(buffer, 0, len);
//            }
//            ris.close();
//
//            out.write("\";".getBytes("UTF-8"));
//        }
//
//        TarArchiveEntry entry = new TarArchiveEntry(indexJsPath.toFile(), "index.js");
//        tarOut.putArchiveEntry(entry);
//
//        Files.copy(indexJsPath, tarOut);
//
//        tarOut.closeArchiveEntry();
//    }

}
