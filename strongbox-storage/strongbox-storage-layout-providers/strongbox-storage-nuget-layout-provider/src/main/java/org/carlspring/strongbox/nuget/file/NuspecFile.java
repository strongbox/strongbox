package org.carlspring.strongbox.nuget.file;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipInputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.transform.sax.SAXSource;

import org.apache.commons.io.IOUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

@XmlRootElement(
        name = "package",
        namespace = "http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd"
)
@XmlAccessorType(XmlAccessType.NONE)
public class NuspecFile
        implements Serializable
{

    public static final String NUSPEC_XML_NAMESPACE_2012 = "http://schemas.microsoft.com/packaging/2011/10/nuspec.xsd";
    public static final String NUSPEC_XML_NAMESPACE_2013 = "http://schemas.microsoft.com/packaging/2012/06/nuspec.xsd";
    public static final String NUSPEC_XML_NAMESPACE_2016 = "http://schemas.microsoft.com/packaging/2013/05/nuspec.xsd";
    public static final String NUSPEC_XML_NAMESPACE_2013_01 = "http://schemas.microsoft.com/packaging/2013/01/nuspec.xsd";
    public static final String NUSPEC_XML_NAMESPACE_2011 = "http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd";
    public static final String NUSPEC_XML_NAMESPACE_2010 = "http://schemas.microsoft.com/packaging/2010/07/nuspec.xsd";
    public static final String NUSPEC_XML_NAMESPACE_2017 = "http://schemas.microsoft.com/packaging/2015/06/nuspec.xsd";
    public static final String NUSPEC_XML_NAMESPACE_EMPTY = "";
    public static final String DEFAULT_FILE_EXTENSION = ".nuspec";
    @XmlElement(
            name = "metadata",
            namespace = "http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd"
    )
    private NuspecFile.Metadata metadata;
    @XmlElement(
            name = "file",
            namespace = "http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd"
    )
    @XmlElementWrapper(
            name = "files",
            namespace = "http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd"
    )
    private List<NuspecFile.NugetFile> files;

    public static NuspecFile Parse(InputStream inputStream)
            throws NugetFormatException
    {
        try
        {
            JAXBContext context = JAXBContext.newInstance(NuspecFile.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            unmarshaller.setEventHandler(new NuspecFile.NuspecXmlValidationEventHandler());
            XMLReader reader = XMLReaderFactory.createXMLReader();
            NugetNamespaceFilter inFilter = new NugetNamespaceFilter();
            inFilter.setParent(reader);
            InputSource inputSource = new InputSource(inputStream);
            SAXSource saxSource = new SAXSource(inFilter, inputSource);
            NuspecFile result = (NuspecFile) unmarshaller.unmarshal(saxSource);
            return result;
        }
        catch (SAXException | JAXBException jaxbException)
        {
            throw new NugetFormatException("Can not read nuspec from XML stream", jaxbException);
        }
    }

    public static NuspecFile ParseZipStream(final ZipInputStream in)
            throws NugetFormatException, IOException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.copy(in, out);
        InputStream is = new ByteArrayInputStream(out.toByteArray());
        return Parse(is);
    }

    public NuspecFile()
    {
    }

    public NuspecFile.Metadata getMetadata()
    {
        if (this.metadata == null)
        {
            this.metadata = new NuspecFile.Metadata();
        }

        return this.metadata;
    }

    public List<NuspecFile.NugetFile> getFiles()
    {
        if (this.files == null)
        {
            this.files = new ArrayList();
        }

        return this.files;
    }

    public String getId()
    {
        return this.getMetadata().id;
    }

    public Version getVersion()
    {
        return this.getMetadata().version;
    }

    public String getTitle()
    {
        return this.getMetadata().title;
    }

    public String getAuthors()
    {
        return this.getMetadata().authors;
    }

    public String getOwners()
    {
        return this.getMetadata().owners;
    }

    public boolean isRequireLicenseAcceptance()
    {
        return this.getMetadata().requireLicenseAcceptance == null ? false :
               this.getMetadata().requireLicenseAcceptance;
    }

    public String getDescription()
    {
        return this.getMetadata().description;
    }

    public String getLicenseUrl()
    {
        return this.getMetadata().licenseUrl;
    }

    public String getProjectUrl()
    {
        return this.getMetadata().projectUrl;
    }

    public String getProjectSourceUrl()
    {
        return this.getMetadata().projectSourceUrl;
    }

    public String getPackageSourceUrl()
    {
        return this.getMetadata().packageSourceUrl;
    }

    public String getDocsUrl()
    {
        return this.getMetadata().docsUrl;
    }

    public String getMailingListUrl()
    {
        return this.getMetadata().mailingListUrl;
    }

    public String getBugTrackerUrl()
    {
        return this.getMetadata().bugTrackerUrl;
    }

    public String getIconUrl()
    {
        return this.getMetadata().iconUrl;
    }

    public String getReleaseNotes()
    {
        return this.getMetadata().releaseNotes;
    }

    public void setReleaseNotes(String releaseNotes)
    {
        this.getMetadata().releaseNotes = releaseNotes;
    }

    public String getSummary()
    {
        return this.getMetadata().summary;
    }

    public String getCopyright()
    {
        return this.getMetadata().copyright;
    }

    public String getLanguage()
    {
        return this.getMetadata().language;
    }

    public List<String> getTags()
    {
        return (List) (this.getMetadata().tags == null ? new ArrayList() : this.getMetadata().tags);
    }

    public List<Reference> getReferences()
    {
        return (List) (this.getMetadata().references == null ? new ArrayList() : this.getMetadata().references);
    }

    public List<Dependency> getDependencies()
    {
        return (List) (this.getMetadata().dependencies == null ? new ArrayList() :
                       this.getMetadata().dependencies.getDependencies());
    }

    public List<DependenciesGroup> getDependenciesGroups()
    {
        return this.getMetadata().dependencies.getGroups();
    }

    public List<FrameworkAssembly> getFrameworkAssembly()
    {
        if (this.getMetadata().frameworkAssembly == null)
        {
            this.getMetadata().frameworkAssembly = new ArrayList();
        }

        return this.getMetadata().frameworkAssembly;
    }

    public void setFrameworkAssembly(List<FrameworkAssembly> frameworkAssembly)
    {
        this.getMetadata().frameworkAssembly = frameworkAssembly;
    }

    public void saveTo(OutputStream outputStream)
            throws JAXBException
    {
        JAXBContext context = JAXBContext.newInstance(this.getClass());
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty("jaxb.formatted.output", true);
        marshaller.marshal(this, outputStream);
    }

    public static class NugetFile
    {

        @XmlAttribute(
                name = "src"
        )
        public String src;
        @XmlAttribute(
                name = "target"
        )
        public String target;
        @XmlAttribute(
                name = "exclude"
        )
        public String exclude;

        public NugetFile()
        {
        }
    }

    public static class Metadata
            implements Serializable
    {

        @XmlElement(
                name = "id",
                namespace = "http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd"
        )
        public String id;
        @XmlElement(
                name = "version",
                namespace = "http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd"
        )
        @XmlJavaTypeAdapter(VersionTypeAdapter.class)
        public Version version;
        @XmlElement(
                name = "title",
                namespace = "http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd"
        )
        public String title;
        @XmlElement(
                name = "authors",
                namespace = "http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd"
        )
        public String authors;
        @XmlElement(
                name = "owners",
                namespace = "http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd"
        )
        public String owners;
        @XmlElement(
                name = "licenseUrl",
                namespace = "http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd"
        )
        public String licenseUrl;
        @XmlElement(
                name = "projectUrl",
                namespace = "http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd"
        )
        public String projectUrl;
        @XmlElement(
                name = "projectSourceUrl",
                namespace = "http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd"
        )
        public String projectSourceUrl;
        @XmlElement(
                name = "packageSourceUrl",
                namespace = "http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd"
        )
        public String packageSourceUrl;
        @XmlElement(
                name = "docsUrl",
                namespace = "http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd"
        )
        public String docsUrl;
        @XmlElement(
                name = "mailingListUrl",
                namespace = "http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd"
        )
        public String mailingListUrl;
        @XmlElement(
                name = "bugTrackerUrl",
                namespace = "http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd"
        )
        public String bugTrackerUrl;
        @XmlElement(
                name = "iconUrl",
                namespace = "http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd"
        )
        public String iconUrl;
        @XmlElement(
                name = "frameworkAssembly",
                namespace = "http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd"
        )
        @XmlElementWrapper(
                name = "frameworkAssemblies",
                namespace = "http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd"
        )
        public List<FrameworkAssembly> frameworkAssembly;
        @XmlElement(
                name = "requireLicenseAcceptance",
                namespace = "http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd"
        )
        public Boolean requireLicenseAcceptance;
        @XmlElement(
                name = "description",
                namespace = "http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd"
        )
        public String description;
        @XmlElement(
                name = "releaseNotes",
                namespace = "http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd"
        )
        public String releaseNotes;
        @XmlElement(
                name = "summary",
                namespace = "http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd"
        )
        public String summary;
        @XmlElement(
                name = "copyright",
                namespace = "http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd"
        )
        public String copyright;
        @XmlElement(
                name = "language",
                namespace = "http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd"
        )
        public String language;
        @XmlElement(
                name = "tags",
                namespace = "http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd"
        )
        @XmlJavaTypeAdapter(StringListTypeAdapter.class)
        public List<String> tags;
        @XmlElementWrapper(
                name = "references",
                namespace = "http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd"
        )
        @XmlElement(
                name = "reference",
                namespace = "http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd"
        )
        public List<Reference> references;
        @XmlElement(
                name = "dependencies",
                namespace = "http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd"
        )
        public Dependencies dependencies;
        @XmlElement(
                name = "developmentDependency",
                namespace = "http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd"
        )
        public Boolean developmentDependency;
        @XmlElement(
                name = "serviceable",
                namespace = "http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd"
        )
        public Boolean serviceable;
        @XmlElement(
                name = "minClientVersion",
                namespace = "http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd"
        )
        public Boolean minClientVersion;
        @XmlElementWrapper(
                name = "packageTypes",
                namespace = "http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd"
        )
        @XmlElement(
                name = "packageType",
                namespace = "http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd"
        )
        public List<PackageType> packageType;
        @XmlElementWrapper(
                name = "contentFiles",
                namespace = "http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd"
        )
        @XmlElement(
                name = "files",
                namespace = "http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd"
        )
        public List<ContentFile> contentFile;
        @XmlElement(
                name = "repository",
                namespace = "http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd"
        )
        public Repository repository;
        @XmlElementWrapper(
                name = "files",
                namespace = "http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd"
        )
        @XmlElement(
                name = "file",
                namespace = "http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd"
        )
        public List<File> file;

        public Metadata()
        {
        }
    }

    private static class NuspecXmlValidationEventHandler
            implements ValidationEventHandler
    {

        private NuspecXmlValidationEventHandler()
        {
        }

        public boolean handleEvent(ValidationEvent event)
        {
            return false;
        }
    }
}