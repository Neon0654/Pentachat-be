package com.hdtpt.pentachat.games.poker.util;

import com.hdtpt.pentachat.games.poker.util.PokerHandEvaluator;
import org.junit.jupiter.api.Test;
import java.util.Arrays;

public class PokerHandEvaluatorTest {
    @Test
    public void testEvaluate() {
        PokerHandEvaluator.HandScore score = PokerHandEvaluator.evaluate(Arrays.asList("2H", "3H", "4H", "5H", "6H", "7H", "8H"));
        System.out.println("Score: " + score.getRank().getDescription());
    }
}
