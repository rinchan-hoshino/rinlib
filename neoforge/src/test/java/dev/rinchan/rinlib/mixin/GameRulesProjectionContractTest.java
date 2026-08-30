package dev.rinchan.rinlib.mixin;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameRulesProjectionContractTest {
    @Test
    void projectionOwnsTheWholeReadInsteadOfDependingOnCallbackCancellationOrder() throws Exception {
        String source = Files.readString(Path.of("src/main/java/dev/rinchan/rinlib/mixin/GameRulesMixin.java"));

        assertTrue(source.contains("@WrapMethod(method = \"getBoolean\")"));
        assertTrue(source.contains("Operation<Boolean> original"));
        assertTrue(source.contains("return true;"));
        assertTrue(source.contains("return original.call(key);"));
        assertFalse(source.contains("CallbackInfoReturnable"));
        assertFalse(source.contains("setReturnValue"));
    }
}
