FROM jboss/wildfly:10.1.0.Final

MAINTAINER "Alex Eng" <aeng@redhat.com>

EXPOSE 8080

# Install postgresql driver
RUN curl -L -o $JBOSS_HOME/standalone/deployments/postgresql-connector.jar https://repo1.maven.org/maven2/org/postgresql/postgresql/9.4.1212/postgresql-9.4.1212.jar

# Parent image defaults to POSIX(ASCII). This ensures that java uses UTF-8
ENV LANG=en_US.UTF-8

# Copy config script
COPY docker/conf/zanata-mt-config.cli /tmp/

# run config script to edit standalone-full.xml
RUN $JBOSS_HOME/bin/jboss-cli.sh --file=/tmp/zanata-mt-config.cli

# use standalone.xml
CMD $JBOSS_HOME/bin/standalone.sh -c  standalone.xml -b 0.0.0.0

# copy war file
ADD target/deployments/ROOT.war $JBOSS_HOME/standalone/deployments/ROOT.war
