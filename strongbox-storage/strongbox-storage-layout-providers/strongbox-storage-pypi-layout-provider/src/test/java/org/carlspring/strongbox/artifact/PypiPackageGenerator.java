package org.carlspring.strongbox.artifact;

import org.carlspring.commons.io.RandomInputStream;
import org.carlspring.strongbox.io.LayoutOutputStream;
import org.carlspring.strongbox.resource.ResourceCloser;
import org.carlspring.strongbox.util.MessageDigestUtils;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.semver.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author carlspring
 */
public class PypiPackageGenerator
{

    private static final Logger logger = LoggerFactory.getLogger(PypiPackageGenerator.class);

    private String basedir;

    public PypiPackageGenerator()
    {
    }

    public PypiPackageGenerator(String basedir)
    {
        this.basedir = basedir;
    }

    public PypiPackageGenerator(File basedir)
    {
        this.basedir = basedir.getAbsolutePath();
    }

    public void generatePypiPackage(String id,
                                     String versionString,
                                     String... dependencyList)
            throws JAXBException, IOException, NoSuchAlgorithmException
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
                   NoSuchAlgorithmException
    {
        createArchive(nupkgFile, dependencyList);
    }

    public void createArchive(File packageFile, String... dependencyList)
            throws IOException,
                   NoSuchAlgorithmException
    {
        ZipOutputStream zos = null;

        LayoutOutputStream layoutOutputStream = null;
        try
        {
            // Make sure the artifact's parent directory exists before writing the model.
            //noinspection ResultOfMethodCallIgnored
            packageFile.getParentFile().mkdirs();

            FileOutputStream fileOutputStream = new FileOutputStream(packageFile);
            
            layoutOutputStream = new LayoutOutputStream(fileOutputStream);
            layoutOutputStream.addAlgorithm(MessageDigestAlgorithms.SHA_512);
            //! layoutOutputStream.setDigestStringifier(this::toBase64);
            
            zos = new ZipOutputStream(layoutOutputStream);

            createRandomPypiFile(zos);
            
            createContentType(zos);
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

    private void createRandomPypiFile(ZipOutputStream zos)
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

    private void generateChecksum(File file,
                                  LayoutOutputStream layoutOutputStream)
        throws IOException
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
