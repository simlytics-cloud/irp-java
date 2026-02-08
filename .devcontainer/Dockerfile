FROM mcr.microsoft.com/devcontainers/java:21

ARG MAVEN_VERSION=3.9.9

RUN set -eux; \
    apt-get update; \
    apt-get install -y --no-install-recommends ca-certificates curl tar; \
    rm -rf /var/lib/apt/lists/*; \
    curl -fsSL "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/${MAVEN_VERSION}/apache-maven-${MAVEN_VERSION}-bin.tar.gz" -o /tmp/maven.tgz; \
    tar -xzf /tmp/maven.tgz -C /opt; \
    ln -s "/opt/apache-maven-${MAVEN_VERSION}" /opt/maven; \
    ln -s /opt/maven/bin/mvn /usr/local/bin/mvn; \
    rm -f /tmp/maven.tgz

ENV MAVEN_HOME=/opt/maven
ENV PATH="${MAVEN_HOME}/bin:${PATH}"

