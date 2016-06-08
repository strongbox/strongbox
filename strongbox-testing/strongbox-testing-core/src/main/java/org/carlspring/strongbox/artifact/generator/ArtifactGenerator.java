package org.carlspring.strongbox.artifact.generator;

import org.carlspring.commons.encryption.EncryptionAlgorithmsEnum;
import org.carlspring.commons.io.MultipleDigestInputStream;
import org.carlspring.commons.io.MultipleDigestOutputStream;
import org.carlspring.commons.io.RandomInputStream;
import org.carlspring.maven.commons.model.ModelWriter;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.resource.ResourceCloser;
import org.carlspring.strongbox.util.MessageDigestUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Writer;
import org.apache.maven.model.Model;
import org.codehaus.plexus.util.WriterFactory;
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

        File artifactFile = null;

        try
        {
            artifactFile = new File(basedir, ArtifactUtils.convertArtifactToPath(artifact));

            // Make sure the artifact's parent directory exists before writing the model.
            //noinspection ResultOfMethodCallIgnored
            artifactFile.getParentFile().mkdirs();

            zos = new ZipOutputStream(new FileOutputStream(artifactFile));

            createMavenPropertiesFile(artifact, zos);
            addMavenPomFile(artifact, zos);
            createRandomSizeFile(zos);
        }
        finally
        {
            ResourceCloser.close(zos, logger);

            generateChecksumsForArtifact(artifactFile);
        }
    }

    protected void createMetadataArchive(Metadata metadata, String metadataPath)
            throws NoSuchAlgorithmException, IOException
    {
        OutputStream os = null;
        Writer writer = null;

        File metadataFile = null;

        try
        {
            metadataFile = new File(basedir, metadataPath);
            
            if (metadataFile.exists()) {
                metadataFile.delete();
            }

            // Make sure the artifact's parent directory exists before writing
            // the model.
            // noinspection ResultOfMethodCallIgnored
            metadataFile.getParentFile().mkdirs();

            os = new MultipleDigestOutputStream(metadataFile, new FileOutputStream(metadataFile));
            writer = WriterFactory.newXmlWriter(os);
            MetadataXpp3Writer mappingWriter = new MetadataXpp3Writer();
            mappingWriter.write(writer, metadata);

            os.flush();
        }
        finally
        {
            ResourceCloser.close(os, logger);

            generateChecksumsForArtifact(metadataFile);
        }
    }

    private void addMavenPomFile(Artifact artifact, ZipOutputStream zos) throws IOException
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

        logger.debug("Generating pom file for " + artifact.toString() + "...");

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
