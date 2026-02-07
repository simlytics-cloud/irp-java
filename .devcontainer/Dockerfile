FROM mcr.microsoft.com/devcontainers/java:21

ARG MAVEN_VERSION=3.9.9

# Install Maven from Apache binaries (avoids GHCR "features")
RUN set -eux; \
    apt-get update; \
    apt-get install -y --no-install-recommends ca-certificates curl tar; \
    rm -rf /var/lib/apt/lists/*; \
    curl -fsSL "https://dlcdn.apache.org/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz" -o /tmp/maven.tgz; \
    tar -xzf /tmp/maven.tgz -C /opt; \
    ln -s "/opt/apache-maven-${MAVEN_VERSION}" /opt/maven; \
    ln -s /opt/maven/bin/mvn /usr/local/bin/mvn; \
    rm -f /tmp/maven.tgz

ENV MAVEN_HOME=/opt/maven
ENV PATH="${MAVEN_HOME}/bin:${PATH}"
