package dev.rinchan.rinlib.minecraft;

import static org.junit.jupiter.api.Assertions.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.packs.resources.ResourceManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class StructureResourcesTest {
    @TempDir Path root;

    @Test
    void vanillaPackLayerEnumeratesAndOpensTheSameEffectiveStructure() throws Exception {
        String key = "example:" + StructureResources.directory() + "/house.nbt";
        Path path = StructureResources.resourcePath(root, key);
        assertEquals(root.resolve("data/example/" + StructureResources.directory() + "/house.nbt"), path);
        Files.createDirectories(path.getParent());
        Files.write(path, new byte[] {1, 2, 3});
        try (var pack = StructureResources.overlay(root, "fixture")) {
            var resolved = StructureResources.list(pack);
            assertEquals(java.util.Set.of(key), resolved.keySet());
            try (var stream = resolved.values().iterator().next().open()) {
                assertArrayEquals(new byte[] {1, 2, 3}, stream.readAllBytes());
            }
        }
    }

    @Test
    void compressedNbtReaderEnforcesTheDecodedMemoryBudget() throws Exception {
        CompoundTag tag = new CompoundTag();
        tag.putString("payload", "x".repeat(16_000));
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        NbtIo.writeCompressed(tag, bytes);
        assertEquals(tag, StructureResources.readCompressed(bytes.toByteArray(), 64_000));
        assertThrows(Exception.class, () -> StructureResources.readCompressed(bytes.toByteArray(), 128));
    }

    @Test
    void preparationInstancesAreScopedAndClosedOnceOnServerStop() {
        assertNull(StructureLifecycle.create());
        AtomicInteger closed = new AtomicInteger();
        StructureLifecycle.register(() -> new StructureLifecycle.Preparation() {
            @Override public ResourceManager prepare(ResourceManager resources, com.mojang.datafixers.DataFixer fixer) {
                return resources;
            }
            @Override public void close() { closed.incrementAndGet(); }
        });
        var first = StructureLifecycle.create();
        var second = StructureLifecycle.create();
        assertNotSame(first, second);
        StructureLifecycle.closeAll();
        assertEquals(2, closed.get());
        StructureLifecycle.closeAll();
        assertEquals(2, closed.get());
    }
}
