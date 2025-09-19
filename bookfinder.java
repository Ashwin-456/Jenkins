import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.util.Scanner;

/**
 * Simple BookFinder CLI using Google Books API.
 * Usage:
 *   javac bookfinder.java
 *   java bookfinder "harry potter"
 * If no argument is given, the program prompts for a query.
 */
public class bookfinder {
    private static final String API = "https://www.googleapis.com/books/v1/volumes?q=";

    public static void main(String[] args) {
        String query;
        if (args.length > 0) {
            query = String.join(" ", args);
        } else {
            System.out.print("Enter search terms: ");
            Scanner sc = new Scanner(System.in, StandardCharsets.UTF_8);
            query = sc.nextLine().trim();
            sc.close();
            if (query.isEmpty()) {
                System.out.println("No query provided. Exiting.");
                return;
            }
        }

        try {
            String json = fetchBooks(query);
            if (json == null || json.isEmpty()) {
                System.out.println("No response from API.");
                return;
            }
            printResults(json);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String fetchBooks(String q) throws IOException, InterruptedException {
        String encoded = URLEncoder.encode(q, StandardCharsets.UTF_8);
        String url = API + encoded + "&maxResults=5";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
            return resp.body();
        } else {
            System.err.println("HTTP error: " + resp.statusCode());
            return null;
        }
    }

    // Minimal JSON extraction to avoid external dependencies. Looks for items[*].volumeInfo fields.
    private static void printResults(String json) {
        // Very small and forgiving parsing using indexOf. Not a full JSON parser,
        // but adequate for extracting common fields from Google Books simple responses.
        int idx = 0;
        int itemCount = 0;
        while (true) {
            int itemsPos = json.indexOf("\"items\":", idx);
            if (itemsPos == -1) break;
            int itemsStart = json.indexOf('[', itemsPos);
            if (itemsStart == -1) break;
            idx = itemsStart + 1;
            break; // move into scanning items from idx
        }

        int pos = json.indexOf("\"items\":");
        if (pos == -1) {
            System.out.println("No items found for query.");
            return;
        }

        int itemsArrayStart = json.indexOf('[', pos);
        int itemsArrayEnd = findMatchingBracket(json, itemsArrayStart);
        if (itemsArrayStart == -1 || itemsArrayEnd == -1) {
            System.out.println("No items array found.");
            return;
        }

        String items = json.substring(itemsArrayStart + 1, itemsArrayEnd);
        int i = 0;
        while (i < items.length()) {
            int objStart = items.indexOf('{', i);
            if (objStart == -1) break;
            int objEnd = findMatchingBracket(items, objStart);
            if (objEnd == -1) break;
            String item = items.substring(objStart, objEnd + 1);
            String volumeInfo = extractField(item, "volumeInfo");
            if (volumeInfo == null) {
                i = objEnd + 1;
                continue;
            }

            String title = extractString(volumeInfo, "title");
            String authors = extractArrayAsString(volumeInfo, "authors");
            String publisher = extractString(volumeInfo, "publisher");
            String description = extractString(volumeInfo, "description");

            System.out.println("--- Book " + (++itemCount) + " ---");
            System.out.println("Title: " + (title != null ? title : "(no title)"));
            System.out.println("Authors: " + (authors != null ? authors : "(no authors)"));
            System.out.println("Publisher: " + (publisher != null ? publisher : "(no publisher)"));
            System.out.println("Description: " + (description != null ? description.replaceAll("\\s+", " ").strip() : "(no description)"));
            System.out.println();

            i = objEnd + 1;
        }

        if (itemCount == 0) System.out.println("No books returned.");
    }

    // Find matching '}' for object starting at pos, or -1
    private static int findMatchingBracket(String s, int startPos) {
        if (startPos < 0 || startPos >= s.length() || s.charAt(startPos) != '{' && s.charAt(startPos) != '[') return -1;
        char open = s.charAt(startPos);
        char close = open == '{' ? '}' : ']';
        int depth = 0;
        boolean inString = false;
        for (int i = startPos; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"') {
                // check for escaped quote
                int back = i - 1;
                boolean escaped = false;
                while (back >= 0 && s.charAt(back) == '\\') { escaped = !escaped; back--; }
                if (!escaped) inString = !inString;
            }
            if (inString) continue;
            if (c == open) depth++;
            else if (c == close) {
                depth--;
                if (depth == 0) return i;
            }
        }
        return -1;
    }

    // Extract a top-level field that is an object: "field": { ... }
    private static String extractField(String json, String fieldName) {
        String key = "\"" + fieldName + "\"\s*:";
        int pos = indexOfRegexLike(json, key, 0);
        if (pos == -1) return null;
        int colon = json.indexOf(':', pos);
        if (colon == -1) return null;
        int start = json.indexOf('{', colon);
        if (start == -1) return null;
        int end = findMatchingBracket(json, start);
        if (end == -1) return null;
        return json.substring(start, end + 1);
    }

    // Very small helper to find a quoted string field value: "name": "value"
    private static String extractString(String json, String fieldName) {
        String key = "\"" + fieldName + "\"";
        int pos = json.indexOf(key);
        if (pos == -1) return null;
        int colon = json.indexOf(':', pos + key.length());
        if (colon == -1) return null;
        int quote = json.indexOf('"', colon);
        if (quote == -1) return null;
        int i = quote + 1;
        StringBuilder sb = new StringBuilder();
        boolean escaped = false;
        for (; i < json.length(); i++) {
            char c = json.charAt(i);
            if (escaped) { sb.append(c); escaped = false; continue; }
            if (c == '\\') { escaped = true; continue; }
            if (c == '"') break;
            sb.append(c);
        }
        return sb.toString();
    }

    // Extract a simple JSON array of strings and join them
    private static String extractArrayAsString(String json, String fieldName) {
        String key = "\"" + fieldName + "\"";
        int pos = json.indexOf(key);
        if (pos == -1) return null;
        int colon = json.indexOf(':', pos + key.length());
        if (colon == -1) return null;
        int start = json.indexOf('[', colon);
        if (start == -1) return null;
        int end = findMatchingBracket(json, start);
        if (end == -1) return null;
        String arr = json.substring(start + 1, end);
        // split on quoted strings
        int i = 0;
        StringBuilder out = new StringBuilder();
        boolean first = true;
        while (i < arr.length()) {
            int q1 = arr.indexOf('"', i);
            if (q1 == -1) break;
            int j = q1 + 1;
            StringBuilder sb = new StringBuilder();
            boolean esc = false;
            for (; j < arr.length(); j++) {
                char c = arr.charAt(j);
                if (esc) { sb.append(c); esc = false; continue; }
                if (c == '\\') { esc = true; continue; }
                if (c == '"') break;
                sb.append(c);
            }
            if (!first) out.append(", ");
            out.append(sb.toString());
            first = false;
            i = j + 1;
        }
        return out.length() > 0 ? out.toString() : null;
    }

    // Helper that approximates regex match for key existence; returns index or -1
    private static int indexOfRegexLike(String s, String pattern, int from) {
        return s.indexOf(pattern.replaceAll("\\s\\*", ""), from);
    }
}
