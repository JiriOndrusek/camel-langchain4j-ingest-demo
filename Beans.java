import java.nio.file.Files;
import java.nio.file.Path;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.apache.camel.BindToRegistry;
import org.apache.camel.Configuration;
import org.apache.camel.Processor;

@Configuration
public class Beans {

    static final Path SNAPSHOT = Path.of("data/store.json");

    // In-memory store, restored from the snapshot so it stays in step with the persistent register
    @BindToRegistry
    public InMemoryEmbeddingStore<TextSegment> store() {
        return Files.exists(SNAPSHOT) ? InMemoryEmbeddingStore.fromFile(SNAPSHOT) : new InMemoryEmbeddingStore<>();
    }

    // Called by the ingest route after each document
    @BindToRegistry
    public Processor snapshot(InMemoryEmbeddingStore<TextSegment> store) {
        return exchange -> store.serializeToFile(SNAPSHOT);
    }

    @BindToRegistry
    public AllMiniLmL6V2EmbeddingModel model() {
        // The model reads its file through the context class loader, which cannot see the --dep jar under JBang
        Thread.currentThread().setContextClassLoader(AllMiniLmL6V2EmbeddingModel.class.getClassLoader());
        return new AllMiniLmL6V2EmbeddingModel();
    }
}
