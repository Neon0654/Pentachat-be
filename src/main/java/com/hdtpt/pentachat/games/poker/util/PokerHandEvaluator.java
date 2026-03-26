package com.hdtpt.pentachat.games.poker.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Đánh giá bộ bài Poker: Thung, Sanh, Sam, Doi...
 */
public class PokerHandEvaluator {

    public enum HandRank {
        HIGH_CARD(0, "Mậu thầu"),
        PAIR(1, "Một đôi"),
        TWO_PAIR(2, "Thú (2 đôi)"),
        THREE_OF_A_KIND(3, "Sám cô"),
        STRAIGHT(4, "Sảnh"),
        FLUSH(5, "Thùng"),
        FULL_HOUSE(6, "Cù lũ"),
        FOUR_OF_A_KIND(7, "Tứ quý"),
        STRAIGHT_FLUSH(8, "Sảnh thùng"),
        ROYAL_FLUSH(9, "Sảnh rồng (Royal Flush)");

        private final int value;
        private final String description;

        HandRank(int value, String description) {
            this.value = value;
            this.description = description;
        }

        public int getValue() { return value; }
        public String getDescription() { return description; }
    }

    @Data
    @AllArgsConstructor
    public static class HandScore implements Comparable<HandScore> {
        private HandRank rank;
        private List<Integer> tieBreakers; // Thêm kicker để phân thắng bại

        @Override
        public int compareTo(HandScore other) {
            int rankCompare = Integer.compare(this.rank.getValue(), other.rank.getValue());
            if (rankCompare != 0) return rankCompare;
            for (int i = 0; i < this.tieBreakers.size(); i++) {
                int compare = Integer.compare(this.tieBreakers.get(i), other.tieBreakers.get(i));
                if (compare != 0) return compare;
            }
            return 0;
        }
    }

    private static final String RANKS = "23456789TJQKA";

    public static HandScore evaluate(List<String> cards) {
        if (cards == null || cards.size() < 5) return new HandScore(HandRank.HIGH_CARD, Collections.emptyList());
        
        // Chuyển card thành số
        List<Card> cardObjects = cards.stream().map(Card::new).collect(Collectors.toList());
        
        // Thử tất cả bộ 5 lá từ 7 lá (nếu có 7 lá)
        List<List<Card>> combinations = getCombinations(cardObjects, 5);
        HandScore maxScore = null;
        
        for (List<Card> combo : combinations) {
            HandScore score = evaluateFiveCards(combo);
            if (maxScore == null || score.compareTo(maxScore) > 0) {
                maxScore = score;
            }
        }
        return maxScore;
    }

    private static HandScore evaluateFiveCards(List<Card> hand) {
        hand.sort(Comparator.comparingInt(Card::getRankValue).reversed());
        
        boolean isFlush = isFlush(hand);
        boolean isStraight = isStraight(hand);
        
        Map<Integer, Integer> counts = getCounts(hand);
        List<Integer> sortedCounts = counts.values().stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList());
        List<Integer> valuesByCount = counts.entrySet().stream()
                .sorted((e1, e2) -> {
                    int c = e2.getValue().compareTo(e1.getValue());
                    if (c != 0) return c;
                    return e2.getKey().compareTo(e1.getKey());
                })
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // Royal/Straight Flush
        if (isFlush && isStraight) {
            if (hand.get(0).getRankValue() == 12 && hand.get(1).getRankValue() == 11) 
                return new HandScore(HandRank.ROYAL_FLUSH, valuesByCount);
            return new HandScore(HandRank.STRAIGHT_FLUSH, valuesByCount);
        }
        
        // Four of a kind
        if (sortedCounts.get(0) == 4) return new HandScore(HandRank.FOUR_OF_A_KIND, valuesByCount);
        
        // Full house
        if (sortedCounts.get(0) == 3 && sortedCounts.get(1) == 2) return new HandScore(HandRank.FULL_HOUSE, valuesByCount);
        
        // Flush
        if (isFlush) return new HandScore(HandRank.FLUSH, valuesByCount);
        
        // Straight
        if (isStraight) return new HandScore(HandRank.STRAIGHT, valuesByCount);
        
        // Three of a kind
        if (sortedCounts.get(0) == 3) return new HandScore(HandRank.THREE_OF_A_KIND, valuesByCount);
        
        // Two pair
        if (sortedCounts.get(0) == 2 && sortedCounts.get(1) == 2) return new HandScore(HandRank.TWO_PAIR, valuesByCount);
        
        // Pair
        if (sortedCounts.get(0) == 2) return new HandScore(HandRank.PAIR, valuesByCount);

        return new HandScore(HandRank.HIGH_CARD, valuesByCount);
    }

    private static boolean isFlush(List<Card> hand) {
        char suit = hand.get(0).suit;
        return hand.stream().allMatch(c -> c.suit == suit);
    }

    private static boolean isStraight(List<Card> hand) {
        // Đặc biệt cho sảnh A-2-3-4-5
        if (hand.get(0).getRankValue() == 12 && hand.get(1).getRankValue() == 3 
            && hand.get(2).getRankValue() == 2 && hand.get(3).getRankValue() == 1 
            && hand.get(4).getRankValue() == 0) return true;
        
        for (int i = 0; i < 4; i++) {
            if (hand.get(i).getRankValue() - hand.get(i+1).getRankValue() != 1) return false;
        }
        return true;
    }

    private static Map<Integer, Integer> getCounts(List<Card> hand) {
        Map<Integer, Integer> counts = new HashMap<>();
        for (Card c : hand) {
            counts.put(c.getRankValue(), counts.getOrDefault(c.getRankValue(), 0) + 1);
        }
        return counts;
    }

    private static List<List<Card>> getCombinations(List<Card> list, int k) {
        List<List<Card>> results = new ArrayList<>();
        generateCombinations(list, k, 0, new ArrayList<>(), results);
        return results;
    }

    private static void generateCombinations(List<Card> list, int k, int start, List<Card> current, List<List<Card>> results) {
        if (current.size() == k) {
            results.add(new ArrayList<>(current));
            return;
        }
        for (int i = start; i < list.size(); i++) {
            current.add(list.get(i));
            generateCombinations(list, k, i + 1, current, results);
            current.remove(current.size() - 1);
        }
    }

    private static class Card {
        char rank;
        char suit;
        Card(String s) {
            rank = s.charAt(0);
            suit = s.charAt(1);
        }
        int getRankValue() { return RANKS.indexOf(rank); }
    }
}
