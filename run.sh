#!/usr/bin/env bash
# Runs the example with Camel JBang, versions pinned. Extra arguments go to `camel run`.
set -e
cd "$(dirname "$0")"

jbang camel@apache/camel run ingest.camel.yaml ask.camel.yaml Beans.java \
  --camel-version=4.23.0 --kamelets-version=4.23.0 \
  --dep=org.apache.tika:tika-parser-pdf-module:4.1.0,dev.langchain4j:langchain4j-embeddings-all-minilm-l6-v2:1.21.0-beta31 \
  "$@"
