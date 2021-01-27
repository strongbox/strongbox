package org.carlspring.strongbox.artifact.generator;

import static java.nio.file.StandardOpenOption.CREATE;

import org.carlspring.strongbox.artifact.coordinates.PypiArtifactCoordinates;
import org.carlspring.strongbox.testing.artifact.LicenseConfiguration;
import org.carlspring.strongbox.testing.artifact.LicenseType;
import org.carlspring.strongbox.util.TestFileUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;

import com.google.common.hash.Hashing;

public class PypiArtifactGenerator
        implements ArtifactGenerator
{
    
    private static final String METADATA_CONTENT = "Metadata-Version: 2.1\n" +
                                                   "Name: %s\n" +
                                                   "Version: %s\n" +
                                                   "Summary: Strongbox package for test\n" +
                                                   "Home-page: https://strongbox.github.io\n" +
                                                   "Author: Strongbox\n" +
                                                   "Author-email: strongbox@carlspring.com\n" +
                                                   "License: %s\n" +
                                                   "Platform: Test platform\n" +
                                                   "Classifier: Programming Language :: Python :: 3\n" +
                                                   "Classifier: License :: OSI Approved :: MIT License\n" +
                                                   "Classifier: Operating System :: OS Independent\n" +
                                                   "Description-Content-Type: text/markdown\n" +
                                                   "\n" +
                                                   "Strongbox package for test";

    private static final String SETUP_PY_CONTENT = "import setuptools\n" +
                                                   "\n" +
                                                   "with open(\"README.md\", \"r\") as fh:\n" +
                                                   "    long_description = fh.read()\n" +
                                                   "\n" +
                                                   "setuptools.setup(\n" +
                                                   "     name='%s',\n" +
                                                   "     version='%s',\n" +
                                                   "     license='%s',\n" +
                                                   "     scripts=['%s'] ,\n" +
                                                   "     author=\"Strongbox\",\n" +
                                                   "     author_email=\"strongbox@carlspring.com\",\n" +
                                                   "     description=\"Test\",\n" +
                                                   "     long_description=long_description,\n" +
                                                   "     platforms=['windows'],\n" +
                                                   "     long_description_content_type=\"text/markdown\",\n" +
                                                   "     url=\"https://strongbox.github.io\",\n" +
                                                   "     packages=setuptools.find_packages(),\n" +
                                                   "     classifiers=[\n" +
                                                   "         \"Programming Language :: Python :: 3\",\n" +
                                                   "         \"License :: OSI Approved :: MIT License\",\n" +
                                                   "         \"Operating System :: OS Independent\",\n" +
                                                   "     ],\n" +
                                                   " )\n";

    private static final String SOURCES_CONTENT = "README.md\n" +
                                                  "setup.py\n" +
                                                  "%s\n" +
                                                  "%s\n" +
                                                  "%s\n" +
                                                  "%s\n" +
                                                  "%s";

    private Path basedir;

    private LicenseConfiguration[] licenses;
    
    public PypiArtifactGenerator(Path basedir)
    {
        this.basedir = basedir;
    }

    public PypiArtifactGenerator(String basedir)
    {
        this.basedir = Paths.get(basedir);
    }

    @Override
    public void setLicenses(LicenseConfiguration[] licenses)
            throws IOException
    {
        if (!ArrayUtils.isEmpty(licenses))
        {
            if (licenses.length > 1)
            {
                throw new IOException("PyPi doesn't support multiple licenses!");
            }

        }
        this.licenses = licenses;
    }

    @Override
    public Path generateArtifact(String id,
                                 String version,
                                 long byteSize)
            throws IOException
    {
        PypiArtifactCoordinates coordinates = new PypiArtifactCoordinates(id,
                                                                          version,
                                                                          null,
                                                                          "py3",
                                                                          "none",
                                                                          "any",
                                                                          "whl");
        return generateArtifact(coordinates, byteSize);
    }

    @Override
    public Path generateArtifact(URI uri,
                                 long byteSize)
            throws IOException
    {
        PypiArtifactCoordinates coordinates = PypiArtifactCoordinates.parse(FilenameUtils.getName(uri.toString()));
        return generateArtifact(coordinates, byteSize);
    }

    public Path generateArtifact(PypiArtifactCoordinates coordinates,
                                 long byteSize)
            throws IOException
    {
        String packagePath = coordinates.buildPath();

        Path fullPath = basedir.resolve(packagePath);

        Path fullPathParent = fullPath.getParent();
        Files.createDirectories(fullPathParent);

        try (OutputStream outputStream = Files.newOutputStream(fullPath, CREATE);
             ZipOutputStream zos = new ZipOutputStream(outputStream))
        {
            if (coordinates.isSourcePackage())
            {
                createSourcePackageFiles(zos, coordinates.getId(), coordinates.getVersion(), byteSize);
            }
            else
            {
                createWheelPackageFiles(zos, coordinates.getId(), coordinates.getVersion(), byteSize);
            }
        }
        return fullPath;
    }

    private void createSourcePackageFiles(ZipOutputStream zos,
                                          String name,
                                          String version,
                                          long byteSize)
            throws IOException
    {
        String licenseFileName = LicenseType.UNLICENSE.getName();
        if (!ArrayUtils.isEmpty(licenses))
        {
            licenseFileName = licenses[0].license().getName();
        }

        //create PKG-INFO zip entry
        String pkgInfoPath = "PKG-INFO";
        byte[] pkgInfoContent = String.format(METADATA_CONTENT, name, version, licenseFileName).getBytes();
        createZipEntry(zos, pkgInfoPath, pkgInfoContent);

        //create README.md zip entry
        String readmePath = "README.md";
        byte[] readmeContent = "Test package".getBytes();
        createZipEntry(zos, readmePath, readmeContent);

        //create setup.cfg zip entry
        String setupCfgPath= "setup.cfg";
        byte[] setupCfgContent = ("[egg_info]\n" +
                                 "tag_build = \n" +
                                 "tag_date = 0").getBytes();
        createZipEntry(zos, setupCfgPath, setupCfgContent);

        //create setup.py zip entry
        String setupPyPath = "setup.py";
        byte[] setupPyContent = String.format(SETUP_PY_CONTENT, name, version, licenseFileName, name).getBytes();
        createZipEntry(zos, setupPyPath, setupPyContent);

        //create script zip entry
        String scriptPath = name;
        byte[] scriptContent = "print(\"hello world\")".getBytes();
        createZipEntry(zos, scriptPath, scriptContent);

        String eggDirectory = String.format("%s.egg-info", name);

        //create dependency_links.txt zip entry
        String dependencyLinksPath = eggDirectory + "/dependency_links.txt";
        byte[] dependencyLinksContent = "".getBytes();
        createZipEntry(zos, dependencyLinksPath, dependencyLinksContent);

        //create egg dependency_links.txt zip entry
        String eggPkgInfoPath = eggDirectory + "/" + pkgInfoPath;
        byte[] eggPkgInfoContent = pkgInfoContent;
        createZipEntry(zos, eggPkgInfoPath, eggPkgInfoContent);

        //create egg top_level.txt zip entry
        String topLevelPath = eggDirectory + "/top_level.txt";
        byte[] topLevelContent = "".getBytes();
        createZipEntry(zos, topLevelPath, topLevelContent);

        //create egg SOURCES.txt zip entry
        String sourcesPath = eggDirectory + "/SOURCES.txt";
        byte[] sourcesContent = String.format(SOURCES_CONTENT,
                                              scriptPath,
                                              eggPkgInfoPath,
                                              sourcesPath,
                                              dependencyLinksPath,
                                              topLevelPath).getBytes();
        createZipEntry(zos, sourcesPath, sourcesContent);

        copyLicenseFiles(zos);

        String randomPath = eggDirectory + "/RANDOM.txt";
        TestFileUtils.generateFile(zos, byteSize, randomPath);
    }

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

    private void createWheelPackageFiles(ZipOutputStream zos,
                                         String name,
                                         String version,
                                         long byteSize)
            throws IOException
    {
        //create bin zip entry
        String binPath = String.format("%s-%s.%s", name, version, "data") + "/scripts/" + name;
        byte[] binContent = "print(\"hello world\")".getBytes();
        createZipEntry(zos, binPath, binContent);

        String dirPath = String.format("%s-%s.%s", name, version, "dist-info");

        //create METADATA zip entry
        String licenseFileName = LicenseType.UNLICENSE.getName();
        if (!ArrayUtils.isEmpty(licenses))
        {
            licenseFileName = licenses[0].license().getName();
        }
        String metadataPath = dirPath + "/" + "METADATA";
        byte[] metadataContent = String.format(METADATA_CONTENT, name, version, licenseFileName).getBytes();
        createZipEntry(zos, metadataPath, metadataContent);

        //create LICENSE zip entry
        String licensePath = "LICENSE";
        String licenseFilePath = LicenseType.UNLICENSE.getLicenseFileSourcePath();
        if (!ArrayUtils.isEmpty(licenses))
        {
            licenseFilePath = licenses[0].license().getLicenseFileSourcePath();
            licensePath = licenses[0].destinationPath();
        }
        licensePath = dirPath + "/" + licensePath;

        byte[] licenseContent = getLicenseFileContent(licenseFilePath);
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

        String randomPath = dirPath + "/RANDOM";
        TestFileUtils.generateFile(zos, byteSize, randomPath);
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
