package org.carlspring.strongbox.artifact.generator;

import org.carlspring.maven.commons.model.ModelWriter;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.io.MultipleDigestInputStream;
import org.carlspring.strongbox.io.RandomInputStream;
import org.carlspring.strongbox.resource.ResourceCloser;
import org.carlspring.strongbox.util.MessageDigestUtils;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Model;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mtodorov
 */
public class ArtifactGenerator
{

    private static final Logger logger = LoggerFactory.getLogger(ArtifactGenerator.class);

    private String basedir;

    private Artifact artifact;

    private String gavtc;


    public ArtifactGenerator(String basedir, String gavtc)
    {
        this.basedir = basedir;
        this.gavtc = gavtc;
        this.artifact = ArtifactUtils.getArtifactFromGAVTC(gavtc);
    }

    public void generate()
            throws IOException,
                   XmlPullParserException,
                   NoSuchAlgorithmException
    {
        generatePom();
        createArchive();
    }

    private void createArchive()
            throws NoSuchAlgorithmException,
                   IOException
    {
        ZipOutputStream zos = null;

        try
        {
            File artifactFile = new File(basedir, ArtifactUtils.convertArtifactToPath(artifact));

            // Make sure the artifact's parent directory exists before writing the model.
            //noinspection ResultOfMethodCallIgnored
            artifactFile.getParentFile().mkdirs();

            zos = new ZipOutputStream(new FileOutputStream(artifactFile));

            createMavenPropertiesFile(zos);
            addMavenPomFile(zos);
            createRandomSizeFile(zos);

            generateChecksumsForArtifact(artifactFile);
        }
        finally
        {
            ResourceCloser.close(zos, logger);
        }
    }

    private void addMavenPomFile(ZipOutputStream zos)
            throws IOException
    {
        final Artifact pomArtifact = ArtifactUtils.getPOMArtifact(artifact);
        File pomFile = new File(basedir, ArtifactUtils.convertArtifactToPath(pomArtifact));

        ZipEntry ze = new ZipEntry("META-INF/maven/" +
                                   artifact.getGroupId() + "/" +
                                   artifact.getArtifactId() + "/" +
                                   "pom.xml");
        zos.putNextEntry(ze);

        FileInputStream fis = new FileInputStream(pomFile);

        byte[] buffer = new byte[4096];
        int len;
        while ((len = fis.read(buffer)) > 0)
        {
            zos.write(buffer, 0, len);
        }

        fis.close();
        zos.closeEntry();
    }

    private void createMavenPropertiesFile(ZipOutputStream zos)
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

    private void generatePom()
            throws IOException,
                   XmlPullParserException,
                   NoSuchAlgorithmException
    {
        final Artifact pomArtifact = ArtifactUtils.getPOMArtifact(artifact);
        File pomFile = new File(basedir, ArtifactUtils.convertArtifactToPath(pomArtifact));

        // Make sure the artifact's parent directory exists before writing the model.
        //noinspection ResultOfMethodCallIgnored
        pomFile.getParentFile().mkdirs();

        Model model = new Model();
        model.setGroupId(artifact.getGroupId());
        model.setArtifactId(artifact.getArtifactId());
        model.setVersion(artifact.getVersion());
        model.setPackaging(artifact.getType()); // This is not exactly correct.

        System.out.println("Generating pom file for " + artifact.toString() + "...");
        ModelWriter writer = new ModelWriter(model, pomFile);
        writer.write();

        generateChecksumsForArtifact(pomFile);
    }

    private void generateChecksumsForArtifact(File artifactFile)
            throws NoSuchAlgorithmException, IOException
    {
        InputStream is = new FileInputStream(artifactFile);
        MultipleDigestInputStream mdis = new MultipleDigestInputStream(is, new String[]{ "MD5", "SHA-1" });

        byte[] bytes = new byte[4096];

        //noinspection StatementWithEmptyBody
        while (mdis.read(bytes, 0, bytes.length) != -1);

        final MessageDigest md5Digest = mdis.getMessageDigest("MD5");
        final MessageDigest sha1Digest = mdis.getMessageDigest("SHA-1");

        writeDigestAsHexadecimalString(md5Digest, artifactFile, "md5");
        writeDigestAsHexadecimalString(sha1Digest, artifactFile, "sha1");
    }

    private void writeDigestAsHexadecimalString(MessageDigest digest,
                                                File artifactFile,
                                                String checksumFileExtension)
            throws IOException
    {
        String checksum = MessageDigestUtils.convertToHexadecimalString(digest);

        final File checksumFile = new File(artifactFile.getAbsolutePath() + "." + checksumFileExtension);

        FileOutputStream fos = null;

        try
        {
            fos = new FileOutputStream(checksumFile);

            fos.write((checksum + "\n").getBytes());
            fos.flush();
            fos.close();
        }
        finally
        {
            ResourceCloser.close(fos, logger);
        }
    }

    public Artifact getArtifact()
    {
        return artifact;
    }

    public void setArtifact(Artifact artifact)
    {
        this.artifact = artifact;
    }

    public String getGavtc()
    {
        return gavtc;
    }

    public void setGavtc(String gavtc)
    {
        this.gavtc = gavtc;
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
