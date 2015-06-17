package org.carlspring.strongbox.artifact.generator;

import org.carlspring.maven.commons.model.ModelWriter;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.io.MultipleDigestInputStream;
import org.carlspring.strongbox.io.RandomInputStream;
import org.carlspring.strongbox.resource.ResourceCloser;
import org.carlspring.strongbox.security.encryption.EncryptionAlgorithmsEnum;
import org.carlspring.strongbox.util.MessageDigestUtils;

import java.io.*;
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

    public static final String PACKAGING_JAR = "jar";

    private String basedir;


    public ArtifactGenerator()
    {
    }

    public ArtifactGenerator(String basedir)
    {
        this.basedir = basedir;
    }

    public ArtifactGenerator(File basedir)
    {
        this.basedir = basedir.getAbsolutePath();
    }

    public void generate(String gavtc, String packaging, String... versions)
            throws IOException,
                   XmlPullParserException,
                   NoSuchAlgorithmException
    {
        if (packaging == null)
        {
            packaging = PACKAGING_JAR;
        }

        for (String version : versions)
        {
            Artifact artifact = ArtifactUtils.getArtifactFromGAVTC(gavtc);
            artifact.setVersion(version);
            artifact.setFile(new File(getBasedir() + "/" + ArtifactUtils.convertArtifactToPath(artifact)));

            generate(artifact, packaging);
        }
    }

    public void generate(String gavtc, String... versions)
            throws IOException,
                   XmlPullParserException,
                   NoSuchAlgorithmException
    {
        for (String version : versions)
        {
            Artifact artifact = ArtifactUtils.getArtifactFromGAVTC(gavtc);
            artifact.setVersion(version);
            artifact.setFile(new File(getBasedir() + "/" + ArtifactUtils.convertArtifactToPath(artifact)));

            generate(artifact);
        }
    }

    public void generate(Artifact artifact)
            throws IOException,
                   XmlPullParserException,
                   NoSuchAlgorithmException
    {
        generatePom(artifact, PACKAGING_JAR);
        createArchive(artifact);
    }

    public void generate(Artifact artifact, String packaging)
            throws IOException,
                   XmlPullParserException,
                   NoSuchAlgorithmException
    {
        generatePom(artifact, packaging);
        createArchive(artifact);
    }

    protected void createArchive(Artifact artifact)
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

            createMavenPropertiesFile(artifact, zos);
            addMavenPomFile(artifact, zos);
            createRandomSizeFile(zos);

            generateChecksumsForArtifact(artifactFile);
        }
        finally
        {
            ResourceCloser.close(zos, logger);
        }
    }

    private void addMavenPomFile(Artifact artifact, ZipOutputStream zos)
            throws IOException
    {
        final Artifact pomArtifact = ArtifactUtils.getPOMArtifact(artifact);
        File pomFile = new File(basedir, ArtifactUtils.convertArtifactToPath(pomArtifact));

        ZipEntry ze = new ZipEntry("META-INF/maven/" +
                                   artifact.getGroupId() + "/" +
                                   artifact.getArtifactId() + "/" +
                                   "pom.xml");
        zos.putNextEntry(ze);

        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream(pomFile);

            byte[] buffer = new byte[4096];
            int len;
            while ((len = fis.read(buffer)) > 0)
            {
                zos.write(buffer, 0, len);
            }
        }
        finally
        {
            ResourceCloser.close(fis, logger);

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

    protected void generatePom(Artifact artifact, String packaging)
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
        model.setPackaging(packaging);

        logger.info("Generating pom file for " + artifact.toString() + "...");

        ModelWriter writer = new ModelWriter(model, pomFile);
        writer.write();

        generateChecksumsForArtifact(pomFile);
    }

    private void generateChecksumsForArtifact(File artifactFile)
            throws NoSuchAlgorithmException, IOException
    {
        InputStream is = new FileInputStream(artifactFile);
        MultipleDigestInputStream mdis = new MultipleDigestInputStream(is);

        int size = 4096;
        byte[] bytes = new byte[size];

        //noinspection StatementWithEmptyBody
        while (mdis.read(bytes, 0, size) != -1);

        mdis.close();

        String md5 = mdis.getMessageDigestAsHexadecimalString(EncryptionAlgorithmsEnum.MD5.getAlgorithm());
        String sha1 = mdis.getMessageDigestAsHexadecimalString(EncryptionAlgorithmsEnum.SHA1.getAlgorithm());

        MessageDigestUtils.writeChecksum(artifactFile, EncryptionAlgorithmsEnum.MD5.getExtension(), md5);
        MessageDigestUtils.writeChecksum(artifactFile, EncryptionAlgorithmsEnum.SHA1.getExtension(), sha1);
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
