FROM jboss/wildfly:10.1.0.Final

MAINTAINER "Alex Eng" <aeng@redhat.com>

EXPOSE 8080
EXPOSE 8787

# Install postgresql driver
RUN curl -s -v --retry 3 -L -o $JBOSS_HOME/standalone/deployments/postgresql-connector.jar https://repo1.maven.org/maven2/org/postgresql/postgresql/9.4.1212/postgresql-9.4.1212.jar

# Parent image defaults to POSIX(ASCII). This ensures that java uses UTF-8
ENV LANG=en_US.UTF-8

# Copy config script
COPY docker/mt-config.cli /tmp/

# run config script to edit standalone-full.xml
RUN $JBOSS_HOME/bin/jboss-cli.sh --file=/tmp/mt-config.cli

# use standalone.xml
CMD $JBOSS_HOME/bin/standalone.sh -c  standalone-full-ha.xml -b 0.0.0.0 --debug

# copy war file
COPY target/deployments/ROOT.war $JBOSS_HOME/standalone/deployments/ROOT.war
