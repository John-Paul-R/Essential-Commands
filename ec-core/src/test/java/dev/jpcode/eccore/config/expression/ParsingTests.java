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

    private enum PlayerProperties
    {
        IsFast,
        CanHighJump,
        IsAncient,
        CanFly,
        IsInvisible,
        IsLarge,
    }

    @Test
    void PatternMatchingExpressionReader_matches_matchesCorrectly() {
        String isLegendaryExpression = "CanFly AND IsInvisible OR IsLarge AND IsAncient";

        var expression = PatternMatchingExpressionReader.parse(isLegendaryExpression, PlayerProperties::valueOf);

        var playerPropertiesContext = CollectionExpressionEvaluationContext.from(
            PlayerProperties.IsAncient,
            PlayerProperties.IsLarge
        );

        var outStr = expression.serialize();
        assertEquals("((CanFly AND IsInvisible) OR (IsLarge AND IsAncient))", outStr);

        var playerIsLegandary = expression.matches(playerPropertiesContext);

        assertEquals(true, playerIsLegandary);

    }

    @Test
    void Expression_empty_doesNotMatch() {
        var expression = Expression.<PlayerProperties>empty();

        var playerPropertiesContext = CollectionExpressionEvaluationContext.from(
            PlayerProperties.IsAncient,
            PlayerProperties.IsLarge
        );

        var outStr = expression.serialize();
        assertEquals("", outStr);

        var playerIsLegandary = expression.matches(playerPropertiesContext);

        assertEquals(false, playerIsLegandary);

    }

}
