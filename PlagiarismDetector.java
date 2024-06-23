import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/*
 * This class implements a simple plagiarism detection algorithm.
 */
public class PlagiarismDetector {

	/*
	 * Returns a Map (sorted by the value of the Integer, in non-ascending order) indicating
	 * the number of matches of phrases of size windowSize or greater between each document in the corpus
	 *
	 * Note that you may NOT remove this method or change its signature or specification!
	 */
	public static Map<String, Integer> detectPlagiarism(String dirName, int windowSize, int threshold) {
		File dirFile = new File(dirName);
		String[] files = dirFile.list();
		if (files == null) throw new IllegalArgumentException();

		Map<String, Set<String>> filePhrasesMap = new HashMap<>();
		for (String file : files) {
			Set<String> phrases = createPhrases(dirName + "/" + file, windowSize);
			if (phrases != null) {
				filePhrasesMap.put(file, phrases);
			}
		}

		Map<String, Integer> numberOfMatches = new HashMap<>();

		// Compare each file to all other files
		for (int i = 0; i < files.length; i++) {
			for (int j = i + 1; j < files.length; j++) {
				String file1 = files[i];
				String file2 = files[j];

				Set<String> file1Phrases = filePhrasesMap.get(file1);
				Set<String> file2Phrases = filePhrasesMap.get(file2);

				if (file1Phrases != null && file2Phrases != null) {
					Set<String> matches = findMatches(file1Phrases, file2Phrases);

					if (matches.size() > threshold) {  // Ensure strictly greater than threshold
						String key = file1 + "-" + file2;
						numberOfMatches.put(key, matches.size());
					}
				}
			}
		}

		// Sort the results based on the number of matches
		return sortResults(numberOfMatches);
	}

	/*
	 * This method reads the given file and then converts it into a List of Strings.
	 * It excludes punctuation and converts all words in the file to uppercase.
	 */
	private static List<String> readFile(String filename) {
		if (filename == null) return null;

		List<String> words = new ArrayList<>();
		StringBuilder content = new StringBuilder();

		try (BufferedReader in = new BufferedReader(new FileReader(filename))) {
			String line;
			while ((line = in.readLine()) != null) {
				content.append(line).append(" ");
			}
			String[] tokens = content.toString().split("\\s+");
			for (String token : tokens) {
				// This strips punctuation and converts to uppercase
				words.add(token.replaceAll("[^a-zA-Z]", "").toUpperCase());
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return words;
	}

	/*
	 * This method reads a file and converts it into a Set of distinct phrases,
	 * each of size "window". The Strings in each phrase are whitespace-separated.
	 */
	private static Set<String> createPhrases(String filename, int window) {
		List<String> words = readFile(filename);
		if (words == null || window < 1) return null;

		Set<String> phrases = new HashSet<>();
		for (int i = 0; i <= words.size() - window; i++) {
			StringBuilder phrase = new StringBuilder();
			for (int j = 0; j < window; j++) {
				phrase.append(words.get(i + j)).append(" ");
			}
			phrases.add(phrase.toString().trim());
		}
		return phrases;
	}

	/*
	 * Returns a Set of Strings that occur in both of the Set parameters.
	 * However, the comparison is case-insensitive.
	 */
	private static Set<String> findMatches(Set<String> myPhrases, Set<String> yourPhrases) {
		Set<String> matches = new HashSet<>();
		Set<String> lowerCaseYourPhrases = yourPhrases.stream()
				.map(String::toLowerCase)
				.collect(Collectors.toSet());
		for (String phrase : myPhrases) {
			if (lowerCaseYourPhrases.contains(phrase.toLowerCase())) {
				matches.add(phrase);
			}
		}
		return matches;
	}

	/*
	 * Returns a LinkedHashMap in which the elements of the Map parameter
	 * are sorted according to the value of the Integer, in non-ascending order.
	 */
	private static LinkedHashMap<String, Integer> sortResults(Map<String, Integer> possibleMatches) {
		return possibleMatches.entrySet()
				.stream()
				.sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
						(e1, e2) -> e1, LinkedHashMap::new));
	}
}
