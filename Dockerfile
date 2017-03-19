FROM jboss/wildfly:10.1.0.Final

MAINTAINER "Alex Eng" <aeng@redhat.com>

EXPOSE 8080

# Parent image defaults to POSIX(ASCII). This ensures that java uses UTF-8
ENV LANG=en_US.UTF-8

ARG MYSQL_DRIVER_VERSION=5.1.41

USER root

# create mysql module
COPY docker/conf/mysql-module/ $JBOSS_HOME/modules/

# Install mysql driver
RUN curl -L -o $JBOSS_HOME/modules/com/mysql/main/mysql-connector-java.jar \
    https://repo1.maven.org/maven2/mysql/mysql-connector-java/${MYSQL_DRIVER_VERSION}/mysql-connector-java-${MYSQL_DRIVER_VERSION}.jar

# Copy config script
COPY docker/conf/zanata-mt-config.cli /tmp/

RUN chown -R jboss.jboss $JBOSS_HOME /tmp/zanata-mt-config.cli

USER jboss

# run config script to edit standalone-full.xml
RUN $JBOSS_HOME/bin/jboss-cli.sh --file=/tmp/zanata-mt-config.cli

# use standalone.xml
CMD $JBOSS_HOME/bin/standalone.sh -c  standalone.xml -b 0.0.0.0

# copy war file
ADD target/deployments/ROOT.war $JBOSS_HOME/standalone/deployments/ROOT.war
