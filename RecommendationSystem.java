package Default;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class RecommendationSystem {
    private static Map<Integer, Map<Integer, Double>> userRatings = new HashMap<>();

    public static void main(String[] args) {
        String filePath = "data.csv";  // Ensure this file exists in the project directory

        // Load data
        loadRatings(filePath);

        // Get recommendations for a specific user
        int targetUser = 1;
        //Scanner sc = new Scanner(System.in);
        //System.out.println("Enter the Recommandations:");
        int numRecommendations = 3;

        List<Integer> recommendations = getRecommendations(targetUser, numRecommendations);

        // Print recommendations
        if (recommendations.isEmpty()) {
            System.out.println("No recommendations found for User ID: " + targetUser);
        } else {
            System.out.println("Recommended items for User ID " + targetUser + ": " + recommendations);
        }
    }

    // Load user-item ratings from CSV
    private static void loadRatings(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    int userId = Integer.parseInt(parts[0].trim());
                    int itemId = Integer.parseInt(parts[1].trim());
                    double rating = Double.parseDouble(parts[2].trim());

                    userRatings.putIfAbsent(userId, new HashMap<>());
                    userRatings.get(userId).put(itemId, rating);
                }
            }
            System.out.println("Data loaded successfully!");
        } catch (IOException e) {
            System.err.println("Error reading data file: " + e.getMessage());
        }
    }

    // Calculate similarity between users
    private static double computeSimilarity(int userA, int userB) {
        Map<Integer, Double> ratingsA = userRatings.get(userA);
        Map<Integer, Double> ratingsB = userRatings.get(userB);

        Set<Integer> commonItems = new HashSet<>(ratingsA.keySet());
        commonItems.retainAll(ratingsB.keySet());

        if (commonItems.isEmpty()) return 0.0; // No common items

        double dotProduct = 0, normA = 0, normB = 0;

        for (int item : commonItems) {
            double ratingA = ratingsA.get(item);
            double ratingB = ratingsB.get(item);

            dotProduct += ratingA * ratingB;
            normA += Math.pow(ratingA, 2);
            normB += Math.pow(ratingB, 2);
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    // Generate recommendations based on similar users
    private static List<Integer> getRecommendations(int userId, int numRecommendations) {
        if (!userRatings.containsKey(userId)) return Collections.emptyList();

        Map<Integer, Double> scores = new HashMap<>();
        Map<Integer, Double> similarities = new HashMap<>();

        for (int otherUser : userRatings.keySet()) {
            if (otherUser != userId) {
                double similarity = computeSimilarity(userId, otherUser);

                if (similarity > 0) {
                    for (Map.Entry<Integer, Double> entry : userRatings.get(otherUser).entrySet()) {
                        int item = entry.getKey();
                        double rating = entry.getValue();

                        if (!userRatings.get(userId).containsKey(item)) {
                            scores.put(item, scores.getOrDefault(item, 0.0) + similarity * rating);
                            similarities.put(item, similarities.getOrDefault(item, 0.0) + similarity);
                        }
                    }
                }
            }
        }

        // Compute final scores
        for (int item : scores.keySet()) {
            scores.put(item, scores.get(item) / similarities.get(item));
        }

        // Get top recommendations
        return scores.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(numRecommendations)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

    }
}