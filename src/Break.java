import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class Break {
    public static void main(String[] args) {
        String text = "🍋‍🟩\u304B\u30993️⃣個ある!";
        System.out.println(text);
        List<String> clusters = deconstruct(text);
        for (String cluster : clusters) {
            String codePoints = cluster.codePoints()
                    .mapToObj(cp -> "U+" + Integer.toHexString(cp).toUpperCase())
                    .collect(Collectors.joining(" "));
            System.out.println("Cluster: " + cluster);
            System.out.println("Code points: " + codePoints);
            System.out.println("----");
        }
    }

    public static List<String> deconstruct(String text) {        
        BreakIterator it = BreakIterator.getCharacterInstance();
        it.setText(text);

        List<String> clusters = new ArrayList<>();
        int prev = 0;
        while (it.next() != BreakIterator.DONE) {
            clusters.add(text.substring(prev, it.current()));
            prev = it.current();
        }
        return clusters;
    }

    /**
     * 直接ループで回せるように、Iterable<String> でラップしたバージョン
     * 
     * <pre>{@code
     * for (String cluster : new GraphemeCluster(text)) {
     *   // cluster を処理
     * }
     * }</pre>
     */ 
    static class GraphemeCluster implements Iterable<String> {
        private final BreakIterator it;
        private final String text;

        public GraphemeCluster(String text) {
            this.text = text;
            this.it = BreakIterator.getCharacterInstance();
            this.it.setText(text);
        }

        public Iterator<String> iterator() {
            return new Iterator<String>() {
                private int prev = 0;

                @Override
                public boolean hasNext() {
                    return it.next() != BreakIterator.DONE;
                }

                @Override
                public String next() {
                    String cluster = text.substring(prev, it.current());
                    prev = it.current();
                    return cluster;
                }
            };
        }
    }
}
