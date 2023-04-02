package dev.jpcode.eccore.config.expression;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.gradle.internal.impldep.org.junit.Assert.assertEquals;

@DisplayName("ParsingTests")
public class ParsingTests {

    @Test
    @DisplayName("PatternMatchingExpressionReader parse - base")
    void PatternMatchingExpressionReader_parse_parsesCorrectly() {
        String input = "1 OR 2 AND (3 OR 4)";

        var retVal = PatternMatchingExpressionReader.parse(input, Integer::parseInt);

        var outStr = retVal.serialize();
        assertEquals("(1 OR (2 AND (3 OR 4)))", outStr);
    }

}
