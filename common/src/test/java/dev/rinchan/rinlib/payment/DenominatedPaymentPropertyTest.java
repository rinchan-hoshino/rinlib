package dev.rinchan.rinlib.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class DenominatedPaymentPropertyTest {
    @Test
    void generatedPlansConserveValueRespectStackBoundsAndRemainDeterministic() {
        var random = new Random(0x52494E4C49424CL);

        for (int example = 0; example < 2_000; example++) {
            var stacks = generatedStacks(random);
            var original = List.copyOf(stacks);
            int available = stacks.stream()
                    .mapToInt(stack -> stack.itemCount() * stack.unitValue())
                    .sum();
            int required = random.nextInt(available + 1);

            var plan = DenominatedPayment.plan(required, stacks).orElseThrow();

            assertEquals(plan, DenominatedPayment.plan(required, stacks).orElseThrow());
            assertEquals(original, stacks);
            assertEquals(required + plan.changeUnits(), removedValue(plan, stacks));
            assertRemovalBoundsAndOrder(plan, stacks);
            assertThrows(UnsupportedOperationException.class, () ->
                    plan.removals().add(new DenominatedPayment.Removal(0, 1)));

            if (!plan.removals().isEmpty()) {
                int lastSlot = plan.removals().get(plan.removals().size() - 1).slot();
                assertTrue(plan.changeUnits() < stacks.get(lastSlot).unitValue());
            }
        }
    }

    @Test
    void underfundedInventoriesNeverExposeAPartialPlan() {
        var stacks = List.of(
                new DenominatedPayment.Stack(0, 3, 7),
                new DenominatedPayment.Stack(1, 2, 4));

        assertTrue(DenominatedPayment.plan(30, stacks).isEmpty());
    }

    private static List<DenominatedPayment.Stack> generatedStacks(Random random) {
        int size = 1 + random.nextInt(8);
        var stacks = new ArrayList<DenominatedPayment.Stack>(size);
        for (int slot = 0; slot < size; slot++) {
            stacks.add(new DenominatedPayment.Stack(
                    slot,
                    random.nextInt(21),
                    1 + random.nextInt(1_000)));
        }
        return stacks;
    }

    private static int removedValue(
            DenominatedPayment.Plan plan,
            List<DenominatedPayment.Stack> stacks) {
        return plan.removals().stream()
                .mapToInt(removal -> removal.itemCount() * stacks.get(removal.slot()).unitValue())
                .sum();
    }

    private static void assertRemovalBoundsAndOrder(
            DenominatedPayment.Plan plan,
            List<DenominatedPayment.Stack> stacks) {
        int previousSlot = -1;
        for (var removal : plan.removals()) {
            assertTrue(removal.slot() > previousSlot);
            assertTrue(removal.itemCount() >= 1);
            assertTrue(removal.itemCount() <= stacks.get(removal.slot()).itemCount());
            previousSlot = removal.slot();
        }
    }
}
