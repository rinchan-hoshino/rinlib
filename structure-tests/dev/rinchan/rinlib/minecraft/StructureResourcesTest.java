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
    void fallbackOnlyNamespaceSharesTheLayerWithDiskEntriesWithoutOwningSourcePack() throws Exception {
        String disk = "example:" + StructureResources.directory() + "/healthy.nbt";
        String delegated = "fallback:" + StructureResources.directory() + "/unreadable.nbt";
        Path path = StructureResources.resourcePath(root, disk);
        Files.createDirectories(path.getParent());
        Files.write(path, new byte[] {1, 2, 3});
        Path partial = StructureResources.resourcePath(root, delegated);
        Files.createDirectories(partial.getParent());
        Files.write(partial, new byte[] {99}); // A failed copy must never shadow its original resource.
        AtomicInteger opens = new AtomicInteger();
        AtomicInteger closes = new AtomicInteger();
        var source = (net.minecraft.server.packs.PackResources) java.lang.reflect.Proxy.newProxyInstance(
            getClass().getClassLoader(), new Class<?>[] {net.minecraft.server.packs.PackResources.class},
            (proxy, method, arguments) -> {
                if (method.getName().equals("close")) { closes.incrementAndGet(); return null; }
                throw new UnsupportedOperationException(method.getName());
            });
        var resource = new net.minecraft.server.packs.resources.Resource(source,
            () -> { opens.incrementAndGet(); return new java.io.ByteArrayInputStream(new byte[] {4, 5}); });
        try (var manager = StructureResources.overlay(root, "hybrid", java.util.Map.of(delegated, resource))) {
            var listed = StructureResources.list(manager);
            assertEquals(java.util.Set.of(disk, delegated), listed.keySet());
            for (int read = 0; read < 2; read++) {
                try (var input = listed.get(delegated).open()) { assertArrayEquals(new byte[] {4, 5}, input.readAllBytes()); }
            }
            var id = manager.listResources(StructureResources.directory(), value -> true).keySet().stream()
                .filter(value -> value.toString().equals(delegated)).findFirst().orElseThrow();
            try (var input = manager.getResource(id).orElseThrow().open()) {
                assertArrayEquals(new byte[] {4, 5}, input.readAllBytes());
            }
            assertEquals(3, opens.get());
        }
        assertEquals(0, closes.get());
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
        var notice = new java.util.concurrent.atomic.AtomicReference<net.minecraft.network.chat.Component>(
            net.minecraft.network.chat.Component.literal("cache repair notice"));
        StructureLifecycle.register(() -> new StructureLifecycle.Preparation() {
            @Override public ResourceManager prepare(ResourceManager resources, com.mojang.datafixers.DataFixer fixer) {
                return resources;
            }
            @Override public void close() { closed.incrementAndGet(); }
            @Override public net.minecraft.network.chat.Component operatorNotice() { return notice.get(); }
        });
        var first = StructureLifecycle.create();
        var second = StructureLifecycle.create();
        assertNotSame(first, second);
        java.util.List<String> messages = new java.util.ArrayList<>();
        StructureLifecycle.onOperatorLogin(false, message -> messages.add(message.getString()));
        assertTrue(messages.isEmpty());
        StructureLifecycle.onOperatorLogin(true, message -> messages.add(message.getString()));
        assertEquals(java.util.List.of("cache repair notice"), messages);
        notice.set(null);
        StructureLifecycle.onOperatorLogin(true, message -> messages.add(message.getString()));
        assertEquals(1, messages.size());
        StructureLifecycle.closeAll();
        assertEquals(2, closed.get());
        StructureLifecycle.closeAll();
        assertEquals(2, closed.get());
    }
}
