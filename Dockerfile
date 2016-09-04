FROM opensuse:latest

RUN zypper --non-interactive install curl tar git mc wget java-1_8_0-openjdk java-1_8_0-openjdk-devel

ENV MAVEN_VERSION 3.3.9

ENV M2_HOME /usr/local/maven-$MAVEN_VERSION
ENV JAVA_HOME /usr/lib64/jvm/java-1.8.0
ENV PATH $JAVA_HOME/bin:$M2_HOME:$PATH

RUN mkdir -p /usr/local/maven-$MAVEN_VERSION \
  && curl -fsSL http://apache.osuosl.org/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz \
    | tar -xzC /usr/local/maven-$MAVEN_VERSION --strip-components=1 \
  && ln -s /usr/local/maven-$MAVEN_VERSION/bin/mvn /usr/local/bin/mvn

ENV MAVEN_HOME /usr/local/maven

RUN mkdir /root/.m2
RUN wget https://github.com/strongbox/strongbox/wiki/resources/maven/settings.xml -O /root/.m2/settings.xml

VOLUME /usr/src/strongbox
