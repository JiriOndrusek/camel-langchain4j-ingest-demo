# camel-langchain4j-ingest-demo

Document ingestion for RAG with Apache Camel 4.23, LangChain4j and Camel JBang.

The files in `docs/` (PDF, Markdown, text) go through Tika and `langchain4j-ingest` into an in-memory embedding store. Every 20 seconds five questions search the store and the log shows the best hit for each. It runs fully local: the embedding model (all-MiniLM-L6-v2) runs in-process, so there is no API key and no Docker.

This README only covers how to run the example. The explanation is in the blog post [Document Ingestion for RAG in Camel 4.23: From Folder to Vector Store](https://camel.apache.org/blog/2026/11/camel-document-ingestion-rag/).

This is a community example, not an official Apache Camel release. Apache Camel is a trademark of The Apache Software Foundation.

## Prerequisites

- Java 21 or newer (tested on 21 and 25)
- [JBang](https://www.jbang.dev)

The first run downloads about 250 MB.

## 1. Run the example

```bash
./run.sh
```

`run.sh` calls Camel JBang with pinned versions (the launcher defaults to Camel 4.22). `Ctrl+C` stops it. `./run.sh --max-seconds=40` stops by itself.

```bash
jbang camel@apache/camel run ingest.camel.yaml ask.camel.yaml Beans.java \
  --camel-version=4.23.0 --kamelets-version=4.23.0 \
  --dep=org.apache.tika:tika-parser-pdf-module:4.1.0,dev.langchain4j:langchain4j-embeddings-all-minilm-l6-v2:1.21.0-beta31
```

## 2. Check the output

The log prefix is trimmed here. Full runs are in [`sample-output.txt`](sample-output.txt).

```
ingest: q3-report.pdf -> INGESTED (1 segments)    (one such line per file, in any order)
ask: How much did revenue grow? -> q3-report.pdf (score 0.7306...): Q3 report Revenue grew twelve percent in ...
ask: Is there a sabbatical? -> vacation-policy.md (score 0.6362...): # Vacation policy Every employee gets ...
```

It worked if you see four `ingest: ... INGESTED` lines and every `ask:` names the right file. `data/register.dat` lists the four files read, and `grep -o '"text"' data/store.json | wc -l` prints `4`.

## 3. Edit a document and restart

1. While it runs, in a second terminal in the same folder:

   ```bash
   echo 'Sabbatical: after 5 years you can take 3 months of unpaid leave.' >> docs/vacation-policy.md
   ```

   Within about 2 seconds only `vacation-policy.md` is ingested again (the register key is path, modified time and size). The next `ask:` round finds the new sentence (score 0.64 -> 0.77). The old segment stays in the store.
2. Stop with `Ctrl+C` and run again: no `ingest:` lines, and the answers still come back, because the register and the store live in `data/`.
3. `rm data/store.json` and run again: still no `ingest:` lines, but `ask:` says the store is empty. A persistent register needs a persistent store. `rm -r data` starts over.

## How it maps to the blog post

| File | Blog section |
|---|---|
| `ingest.camel.yaml` | Kamelets: The Same Pipeline, Declared |
| `Beans.java`, `ask.camel.yaml`, `run.sh` | Run It Anywhere |
| `application.properties`, section 3 above | Good to Know (Registers) |

`Beans.java` also saves the store to `data/store.json` after each document, which the post does not show. The Camel Quarkus section is not covered here.

## Troubleshooting

- **`Embedding model file is not available`** (the rest of the message points at LangChain4j sources, which is misleading): under JBang the thread context class loader cannot see the `--dep` jar that holds the model file. `Beans.java` sets the class loader before it creates the model. Keep that line if you change the bean.
- **The first run asks whether to trust `github.com/apache/camel`:** approve it, or trust it once up front with `jbang trust add https://github.com/apache/camel/`.
- **You want the plain `camel` command of the post:** install it once with `jbang app install camel@apache/camel`. `run.sh` does not need it.

## License

[Apache License 2.0](LICENSE)
