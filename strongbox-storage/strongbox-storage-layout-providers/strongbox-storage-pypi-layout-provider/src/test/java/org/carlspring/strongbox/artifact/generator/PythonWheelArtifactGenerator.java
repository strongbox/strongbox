package org.carlspring.strongbox.artifact.generator;

import org.carlspring.strongbox.artifact.coordinates.PypiWheelArtifactCoordinates;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.google.common.hash.Hashing;
import org.apache.commons.io.FilenameUtils;

public class PythonWheelArtifactGenerator implements ArtifactGenerator
{

    private static final String METADATA_CONTENT = "Metadata-Version: 2.1\n" +
                                                   "Name: %s\n" +
                                                   "Version: %s\n" +
                                                   "Summary: Strongbox wheel package for test\n" +
                                                   "Home-page: https://strongbox.github.io\n" +
                                                   "Author: Strongbox\n" +
                                                   "Author-email: strongbox@carlspring.com\n" +
                                                   "License: Test license\n" +
                                                   "Platform: Test platform\n" +
                                                   "Classifier: Programming Language :: Python :: 3\n" +
                                                   "Classifier: License :: OSI Approved :: MIT License\n" +
                                                   "Classifier: Operating System :: OS Independent\n" +
                                                   "\n" +
                                                   "Strongbox wheel package for test";

    private Path basedir;
    
    public PythonWheelArtifactGenerator(Path basedir)
    {
        this.basedir = basedir;
    }

    public PythonWheelArtifactGenerator(String basedir)
    {
        this.basedir = Paths.get(basedir);
    }

    @Override
    public Path generateArtifact(String id,
                                 String version,
                                 int size)
            throws IOException
    {
        PypiWheelArtifactCoordinates coordinates = new PypiWheelArtifactCoordinates(id,
                                                                                    version,
                                                                                    null,
                                                                                    "py3",
                                                                                    "none",
                                                                                    "any");
        return generateArtifact(coordinates);
    }

    @Override
    public Path generateArtifact(URI uri,
                                 int size)
            throws IOException
    {
        PypiWheelArtifactCoordinates coordinates = PypiWheelArtifactCoordinates.parse(FilenameUtils.getName(uri.toString()));
        return generateArtifact(coordinates);
    }

    public Path generateArtifact(PypiWheelArtifactCoordinates coordinates)
            throws IOException
    {
        String packagePath = coordinates.toPath();

        Path fullPath = basedir.resolve(packagePath);

        try(ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(fullPath))) {
            createPackageFiles(zos, coordinates.getId(), coordinates.getVersion());
        }

        return fullPath;
    }

    private void createPackageFiles(ZipOutputStream zos,
                                    String name,
                                    String version)
            throws IOException
    {
        //create bin zip entry
        String binPath = String.format("%s-%s.%s", name, version, "data") + "/scripts/" + name;
        byte[] binContent = "print(\"hello world\")".getBytes();
        createZipEntry(zos, binPath, binContent);

        String dirPath = String.format("%s-%s.%s", name, version, "dist-info");

        //create METADATA zip entry
        String metadataPath = dirPath + "/" + "METADATA";
        byte[] metadataContent = String.format(METADATA_CONTENT, name, version).getBytes();
        createZipEntry(zos, metadataPath, metadataContent);

        //create LICENSE zip entry
        String licensePath = dirPath + "/" + "LICENSE";
        byte[] licenseContent = "Copyright (c) 2018 The Python Packaging Authority".getBytes();
        createZipEntry(zos, licensePath, licenseContent);

        //create WHEEL zip entry
        String wheelPath = dirPath + "/" + "WHEEL";
        byte[] wheelContent = ("Wheel-Version: 1.0\n" +
                               "Generator: bdist_wheel (0.33.4)\n" +
                               "Root-Is-Purelib: true\n" +
                               "Tag: py2-none-any").getBytes();

        createZipEntry(zos, wheelPath, wheelContent);

        //create top_level.txt zip entry
        String topLevelPath = dirPath + "/" + "top_level.txt";
        byte[] topLevelContent = " ".getBytes();
        createZipEntry(zos, topLevelPath, topLevelContent);

        //create RECORD zip entry
        String recordPath = dirPath + "/" + "RECORD";
        String recordLineTmpl = "%s,sha256=%s,%s\n";
        StringBuilder recordContent = new StringBuilder()
                                              .append(String.format(recordLineTmpl,
                                                                    binPath,
                                                                    calculateHash(binContent),
                                                                    binContent.length))
                                              .append(String.format(recordLineTmpl,
                                                                    licensePath,
                                                                    calculateHash(licenseContent),
                                                                    licenseContent.length))
                                              .append(String.format(recordLineTmpl,
                                                                    metadataPath,
                                                                    calculateHash(metadataContent),
                                                                    metadataContent.length))
                                              .append(String.format(recordLineTmpl,
                                                                    wheelPath,
                                                                    calculateHash(wheelContent),
                                                                    wheelContent.length))
                                              .append(String.format(recordLineTmpl,
                                                                    topLevelPath,
                                                                    calculateHash(topLevelContent),
                                                                    topLevelContent.length))
                                              .append(recordPath)
                                              .append(",,");
        createZipEntry(zos, recordPath, recordContent.toString().getBytes());
    }

    private String calculateHash(byte[] content)
    {
        return Base64.getUrlEncoder()
                     .encodeToString(Hashing.sha256().hashBytes(content).asBytes())
                     .replace("=", "");
    }

    private void createZipEntry(ZipOutputStream zos,
                                String path,
                                byte[] contentData)
            throws IOException
    {
        ZipEntry zipEntry = new ZipEntry(path);

        zos.putNextEntry(zipEntry);
        zos.write(contentData, 0, contentData.length);
        zos.closeEntry();
    }

}
