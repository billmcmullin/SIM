package com.sim.chatserver.web.dashboard.inactiveusers;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class InactiveUsersFrustrationTextUtilTest {

    @Test
    void hasExplicitFrustrationSignal_detectsProfanityAndKnownPhrases() {
        assertTrue(InactiveUsersFrustrationTextUtil.hasExplicitFrustrationSignal("this is bullshit"));
        assertTrue(InactiveUsersFrustrationTextUtil.hasExplicitFrustrationSignal("that is not what i asked"));
        assertFalse(InactiveUsersFrustrationTextUtil.hasExplicitFrustrationSignal("thanks for the update"));
    }

    @Test
    void isNonFrustrationContext_detectsCodeLogsAndSafeAcronyms() {
        assertTrue(InactiveUsersFrustrationTextUtil.isNonFrustrationContext("```java\\npublic class A {}\\n```"));
        assertTrue(InactiveUsersFrustrationTextUtil.isNonFrustrationContext("2026-08-18 10:10:10 ERROR at com.sim.Service"));
        assertTrue(InactiveUsersFrustrationTextUtil.isNonFrustrationContext("HTTP API JSON TLS"));
        assertFalse(InactiveUsersFrustrationTextUtil.isNonFrustrationContext("Please help me with this request"));
    }

    @Test
    void isConsistentCapsStyle_requiresEnoughNonCodeSamplesAndRatio() {
        List<String> mostlyCaps = List.of(
                "THIS IS BROKEN NOW",
                "PLEASE FIX THIS ASAP",
                "WHY WRONG ANSWER"
        );
        assertTrue(InactiveUsersFrustrationTextUtil.isConsistentCapsStyle(mostlyCaps));

        List<String> mixed = List.of(
                "this is normal",
                "maybe fine",
                "ONE CAPS TOKEN"
        );
        assertFalse(InactiveUsersFrustrationTextUtil.isConsistentCapsStyle(mixed));

        assertFalse(InactiveUsersFrustrationTextUtil.isConsistentCapsStyle(List.of("ONLY ONE")));
    }
}
