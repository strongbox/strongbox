FROM opensuse:latest

# Setup the tools and variables
RUN zypper --non-interactive install sudo curl tar git mc wget aaa_base java-1_8_0-openjdk java-1_8_0-openjdk-devel

ENV STRONGBOX_VERSION 1.0-SNAPSHOT
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

# Set up the sources
VOLUME /usr/src/strongbox

# Set up the distribution
ENV STRONGBOX_HOME /usr/local/strongbox
RUN mkdir -p /usr/local/strongbox \
  && curl -fsSL https://github.com/strongbox/strongbox-assembly/releases/download/$STRONGBOX_VERSION/strongbox-distribution-$STRONGBOX_VERSION.tar.gz \
  | tar -xzC /usr/local/strongbox --strip-components=1
RUN ln -s $STRONGBOX_HOME/bin/wrapper-linux-x86-64 $STRONGBOX_HOME/bin/wrapper
RUN groupadd strongbox
RUN useradd -g strongbox -d $STRONGBOX_HOME strongbox
RUN chown -R strongbox:strongbox $STRONGBOX_HOME
#RUN $STRONGBOX_HOME/bin/strongbox install
#RUN $STRONGBOX_HOME/bin/strongbox start

RUN ln -s $STRONGBOX_HOME/bin/strongbox /etc/init.d/strongbox

RUN chkconfig --add strongbox
RUN chkconfig --level 235 strongbox on
RUN systemctl enable strongbox

EXPOSE 48080

# ENTRYPOINT /etc/init.d/strongbox start && bash
