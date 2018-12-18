package org.duh102.duhbot.fortune;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.regex.Matcher;
import java.util.stream.Stream;

import static org.duh102.duhbot.fortune.FortunePlugin.FORTUNE_COMMAND;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestFortunePlugin {
    private static Stream<Arguments> fortuneCommandMatcherArgs() {
        return Stream.of(
                Arguments.of(".notafortune", false, null),
                Arguments.of("  .fortune", false, null),
                Arguments.of(".fortune", true, null),
                Arguments.of(".fortune db", true, "db"),
                Arguments.of(".fortune   db", true, "db"),
                Arguments.of(".fortune\tdb", true, "db"),
                Arguments.of(".fortune db a", true, "db"),
                Arguments.of("!fortune db", false, null),
                Arguments.of("ffortune db", false, null)
        );
    }

    @ParameterizedTest(name = "{index}: {0} match? {1}; give {2}")
    @MethodSource("fortuneCommandMatcherArgs")
    public void testFortuneCommandMatcher(String input, boolean shouldMatch,
                                          String dbExpected) {
        Matcher match = FORTUNE_COMMAND.matcher(input);
        assertEquals(shouldMatch, match.find());
        if( shouldMatch ) {
            assertEquals(dbExpected, match.group("db"));
        }
    }
}
