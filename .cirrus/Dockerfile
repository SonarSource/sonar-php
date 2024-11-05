ARG CIRRUS_AWS_ACCOUNT
FROM ${CIRRUS_AWS_ACCOUNT}.dkr.ecr.eu-central-1.amazonaws.com/base:j17-latest

USER root
RUN apt-get update && apt-get --no-install-recommends install -y gcc make && rm -rf -- /var/lib/apt/lists/* /var/cache/apt/archives/*

USER sonarsource
ENV PATH="/opt/go/bin:/home/sonarsource/go/bin:/opt/musl/bin:${PATH}"
