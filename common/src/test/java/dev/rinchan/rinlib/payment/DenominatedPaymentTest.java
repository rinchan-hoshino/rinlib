package dev.rinchan.rinlib.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class DenominatedPaymentTest {
    @Test
    void plansExactPaymentInInventoryOrder() {
        var plan = DenominatedPayment.plan(10, List.of(
            new DenominatedPayment.Stack(0, 1, 9),
            new DenominatedPayment.Stack(1, 4, 1)
        )).orElseThrow();

        assertEquals(List.of(
            new DenominatedPayment.Removal(0, 1),
            new DenominatedPayment.Removal(1, 1)
        ), plan.removals());
        assertEquals(0, plan.changeUnits());
    }

    @Test
    void returnsChangeWhenAHighValueStackOverpays() {
        var plan = DenominatedPayment.plan(10, List.of(
            new DenominatedPayment.Stack(0, 2, 9),
            new DenominatedPayment.Stack(1, 10, 1)
        )).orElseThrow();

        assertEquals(List.of(new DenominatedPayment.Removal(0, 2)), plan.removals());
        assertEquals(8, plan.changeUnits());
    }

    @Test
    void rejectsAnInsufficientInventoryWithoutPartialPlan() {
        assertTrue(DenominatedPayment.plan(10, List.of(
            new DenominatedPayment.Stack(0, 1, 9)
        )).isEmpty());
    }

    @Test
    void zeroCostNeedsNoInventory() {
        var plan = DenominatedPayment.plan(0, List.of()).orElseThrow();

        assertEquals(List.of(), plan.removals());
        assertEquals(0, plan.changeUnits());
    }

    @Test
    void totalValueCalculationDoesNotOverflowInt() {
        var plan = DenominatedPayment.plan(Integer.MAX_VALUE, List.of(
            new DenominatedPayment.Stack(0, Integer.MAX_VALUE, 9)
        )).orElseThrow();

        assertEquals(238_609_295, plan.removals().get(0).itemCount());
        assertEquals(8, plan.changeUnits());
    }
}
