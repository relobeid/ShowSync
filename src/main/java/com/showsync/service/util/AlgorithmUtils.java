package com.showsync.service.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class for core AI recommendation algorithms.
 * Provides mathematical functions for similarity calculations, 
 * content analysis, and preference scoring.
 */
@Component
public class AlgorithmUtils {
    
    // === SIMILARITY CALCULATIONS ===
    
    /**
     * Calculate cosine similarity between two preference vectors
     * @param vector1 First preference vector (genre -> score)
     * @param vector2 Second preference vector (genre -> score)
     * @return Cosine similarity score (0.0 to 1.0)
     */
    public double cosineSimilarity(Map<String, Double> vector1, Map<String, Double> vector2) {
        if (vector1.isEmpty() || vector2.isEmpty()) {
            return 0.0;
        }
        
        Set<String> commonKeys = new HashSet<>(vector1.keySet());
        commonKeys.retainAll(vector2.keySet());
        
        if (commonKeys.isEmpty()) {
            return 0.0;
        }
        
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        
        for (String key : commonKeys) {
            double val1 = vector1.getOrDefault(key, 0.0);
            double val2 = vector2.getOrDefault(key, 0.0);
            
            dotProduct += val1 * val2;
            norm1 += val1 * val1;
            norm2 += val2 * val2;
        }
        
        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }
        
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
    
    /**
     * Calculate Jaccard similarity between two sets
     * @param set1 First set
     * @param set2 Second set
     * @return Jaccard similarity (0.0 to 1.0)
     */
    public double jaccardSimilarity(Set<String> set1, Set<String> set2) {
        if (set1.isEmpty() && set2.isEmpty()) {
            return 1.0;
        }
        
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        
        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);
        
        return (double) intersection.size() / union.size();
    }
    
    /**
     * Calculate Pearson correlation coefficient
     * @param values1 First set of values
     * @param values2 Second set of values
     * @return Pearson correlation (-1.0 to 1.0)
     */
    public double pearsonCorrelation(List<Double> values1, List<Double> values2) {
        if (values1.size() != values2.size() || values1.size() < 2) {
            return 0.0;
        }
        
        double mean1 = values1.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double mean2 = values2.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        
        double numerator = 0.0;
        double sum1 = 0.0;
        double sum2 = 0.0;
        
        for (int i = 0; i < values1.size(); i++) {
            double diff1 = values1.get(i) - mean1;
            double diff2 = values2.get(i) - mean2;
            
            numerator += diff1 * diff2;
            sum1 += diff1 * diff1;
            sum2 += diff2 * diff2;
        }
        
        double denominator = Math.sqrt(sum1 * sum2);
        return denominator == 0.0 ? 0.0 : numerator / denominator;
    }
    
    // === SCORING AND NORMALIZATION ===
    
    /**
     * Apply time decay to a score based on age
     * @param score Original score
     * @param timestamp When the score was recorded
     * @param decayFactor Decay factor per day (0.0 to 1.0)
     * @return Time-decayed score
     */
    public double applyTimeDecay(double score, LocalDateTime timestamp, double decayFactor) {
        long daysOld = ChronoUnit.DAYS.between(timestamp, LocalDateTime.now());
        return score * Math.pow(decayFactor, daysOld);
    }
    
    /**
     * Normalize scores to 0.0-1.0 range using min-max normalization
     * @param scores Map of item -> score
     * @return Normalized scores
     */
    public Map<String, Double> normalizeScores(Map<String, Double> scores) {
        if (scores.isEmpty()) {
            return new HashMap<>();
        }
        
        double min = scores.values().stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
        double max = scores.values().stream().mapToDouble(Double::doubleValue).max().orElse(1.0);
        
        if (max == min) {
            return scores.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> 0.5));
        }
        
        return scores.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> (e.getValue() - min) / (max - min)
            ));
    }
    
    /**
     * Calculate weighted average of scores
     * @param scores List of scores
     * @param weights List of weights (must match scores length)
     * @return Weighted average
     */
    public double weightedAverage(List<Double> scores, List<Double> weights) {
        if (scores.size() != weights.size() || scores.isEmpty()) {
            return 0.0;
        }
        
        double weightedSum = 0.0;
        double totalWeight = 0.0;
        
        for (int i = 0; i < scores.size(); i++) {
            weightedSum += scores.get(i) * weights.get(i);
            totalWeight += weights.get(i);
        }
        
        return totalWeight == 0.0 ? 0.0 : weightedSum / totalWeight;
    }
    
    // === CONTENT ANALYSIS ===
    
    /**
     * Calculate TF-IDF scores for text content
     * @param document The text document
     * @param corpus All documents in the corpus
     * @return Map of term -> TF-IDF score
     */
    public Map<String, Double> calculateTfIdf(String document, List<String> corpus) {
        Map<String, Double> tfIdfScores = new HashMap<>();
        
        List<String> terms = tokenize(document);
        int documentLength = terms.size();
        
        // Calculate term frequencies
        Map<String, Integer> termCounts = new HashMap<>();
        for (String term : terms) {
            termCounts.put(term, termCounts.getOrDefault(term, 0) + 1);
        }
        
        // Calculate TF-IDF for each term
        for (Map.Entry<String, Integer> entry : termCounts.entrySet()) {
            String term = entry.getKey();
            int termCount = entry.getValue();
            
            // Term Frequency
            double tf = (double) termCount / documentLength;
            
            // Inverse Document Frequency
            long documentsWithTerm = corpus.stream()
                .mapToLong(doc -> tokenize(doc).contains(term) ? 1 : 0)
                .sum();
            
            double idf = Math.log((double) corpus.size() / (documentsWithTerm + 1));
            
            tfIdfScores.put(term, tf * idf);
        }
        
        return tfIdfScores;
    }
    
    /**
     * Simple tokenization for text analysis
     * @param text Input text
     * @return List of tokens
     */
    public List<String> tokenize(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        return Arrays.stream(text.toLowerCase()
                .replaceAll("[^a-zA-Z0-9\\s]", "")
                .split("\\s+"))
            .filter(token -> token.length() > 2) // Filter out short words
            .collect(Collectors.toList());
    }
    
    // === STATISTICAL FUNCTIONS ===
    
    /**
     * Calculate standard deviation
     * @param values List of values
     * @return Standard deviation
     */
    public double standardDeviation(List<Double> values) {
        if (values.size() < 2) {
            return 0.0;
        }
        
        double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double sumSquaredDiffs = values.stream()
            .mapToDouble(v -> Math.pow(v - mean, 2))
            .sum();
        
        return Math.sqrt(sumSquaredDiffs / (values.size() - 1));
    }
    
    /**
     * Calculate diversity score based on distribution entropy
     * @param distribution Map of category -> frequency
     * @return Diversity score (0.0 to 1.0)
     */
    public double calculateDiversity(Map<String, Double> distribution) {
        if (distribution.isEmpty()) {
            return 0.0;
        }
        
        double total = distribution.values().stream().mapToDouble(Double::doubleValue).sum();
        if (total == 0.0) {
            return 0.0;
        }
        
        double entropy = 0.0;
        for (double frequency : distribution.values()) {
            if (frequency > 0) {
                double probability = frequency / total;
                entropy -= probability * Math.log(probability) / Math.log(2);
            }
        }
        
        // Normalize entropy by maximum possible entropy
        double maxEntropy = Math.log(distribution.size()) / Math.log(2);
        return maxEntropy == 0.0 ? 0.0 : entropy / maxEntropy;
    }
    
    /**
     * Calculate confidence score based on data quantity and quality
     * @param interactionCount Number of user interactions
     * @param timeSpanDays Days over which interactions occurred
     * @param diversityScore How diverse the interactions are
     * @return Confidence score (0.0 to 1.0)
     */
    public double calculateConfidenceScore(int interactionCount, long timeSpanDays, double diversityScore) {
        // Base confidence from interaction count
        double countConfidence = Math.min(1.0, interactionCount / 50.0);
        
        // Time span factor (more spread out = better)
        double timeConfidence = Math.min(1.0, timeSpanDays / 30.0);
        
        // Diversity factor
        double diversityConfidence = diversityScore;
        
        // Weighted combination
        return (countConfidence * 0.5) + (timeConfidence * 0.3) + (diversityConfidence * 0.2);
    }
    
    /**
     * Apply sigmoid function for smooth transitions
     * @param x Input value
     * @param steepness Controls steepness of the curve
     * @return Sigmoid output (0.0 to 1.0)
     */
    public double sigmoid(double x, double steepness) {
        return 1.0 / (1.0 + Math.exp(-steepness * x));
    }
    
    /**
     * Rank items by score and apply position-based decay
     * @param scores Map of item -> score
     * @param decayRate How much each position decreases value
     * @return Ranked and decayed scores
     */
    public LinkedHashMap<String, Double> rankWithDecay(Map<String, Double> scores, double decayRate) {
        return scores.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .collect(LinkedHashMap::new, 
                (map, entry) -> {
                    int position = map.size();
                    double decayedScore = entry.getValue() * Math.pow(decayRate, position);
                    map.put(entry.getKey(), decayedScore);
                }, 
                LinkedHashMap::putAll);
    }
} 