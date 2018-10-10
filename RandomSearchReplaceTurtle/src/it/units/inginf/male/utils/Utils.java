/*
 * Copyright (C) 2018 Machine Learning Lab - University of Trieste, 
 * Italy (http://machinelearning.inginf.units.it/)  
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.units.inginf.male.utils;

import it.units.inginf.male.coevolution.Forest;
import it.units.inginf.male.inputs.Bounds;
import it.units.inginf.male.objective.Ranking;
import it.units.inginf.male.tree.Constant;
import it.units.inginf.male.tree.Node;
import it.units.inginf.male.tree.RegexRange;
import it.units.inginf.male.tree.operator.Concatenator;
import it.units.inginf.male.tree.operator.Quantifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Marco
 */
public class Utils {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    private static final List<String> colors = Arrays.asList(ANSI_GREEN, ANSI_PURPLE, ANSI_CYAN, ANSI_RED);

    public static String getCoolForestRapresentation(Forest forest) {
        StringBuilder sb = new StringBuilder();
        int color = 0;
        for (Node tree : forest) {
            sb.append(colors.get(color));
            tree.describe(sb);
            sb.append(ANSI_RESET);
            sb.append(" ");
            color = (color + 1) % colors.size();
        }

        return sb.toString();
    }

    public static String printableRegex(String regex) {
        if (regex.length() > 65) {
            return regex.substring(0, 64) + " [..]" + ANSI_RESET;
        }
        return regex;
    }

    public static int computeLevenshteinDistance(String s, String t) {
        if (s == null || t == null) {
            throw new IllegalArgumentException("Strings must not be null s: " + s + " t: " + t);
        }

        /*
        The difference between this impl. and the previous is that, rather
        than creating and retaining a matrix of size s.length()+1 by t.length()+1,
        we maintain two single-dimensional arrays of length s.length()+1.  The first, d,
        is the 'current working' distance array that maintains the newest distance cost
        counts as we iterate through the characters of String s.  Each time we increment
        the index of String t we are comparing, d is copied to p, the second int[].  Doing so
        allows us to retain the previous cost counts as required by the algorithm (taking
        the minimum of the cost count to the left, up one, and diagonally up and to the left
        of the current cost count being calculated).  (Note that the arrays aren't really
        copied anymore, just switched...this is clearly much better than cloning an array
        or doing a System.arraycopy() each time  through the outer loop.)

        Effectively, the difference between the two implementations is this one does not
        cause an out of memory condition when calculating the LD over two very large strings.
         */
        int n = s.length(); // length of s
        int m = t.length(); // length of t

        if (n == 0) {
            return m;
        } else if (m == 0) {
            return n;
        }

        int p[] = new int[n + 1]; //'previous' cost array, horizontally
        int d[] = new int[n + 1]; // cost array, horizontally
        int _d[]; //placeholder to assist in swapping p and d

        // indexes into strings s and t
        int i; // iterates through s
        int j; // iterates through t

        char t_j; // jth character of t

        int cost; // cost

        for (i = 0; i <= n; i++) {
            p[i] = i;
        }

        for (j = 1; j <= m; j++) {
            t_j = t.charAt(j - 1);
            d[0] = j;

            for (i = 1; i <= n; i++) {
                cost = s.charAt(i - 1) == t_j ? 0 : 1;
                // minimum of cell to the left+1, to the top+1, diagonally left and up +cost
                d[i] = Math.min(Math.min(d[i - 1] + 1, p[i] + 1), p[i - 1] + cost);
            }

            // copy current distance counts to 'previous row' distance counts
            _d = p;
            p = d;
            d = _d;
        }

        // our last action in the above loop was to switch d and p, so p now
        // actually has the most recent cost counts
        return p[n];
    }

    public static float[] calculateMeanFitness(List<Ranking> population) {
        float[] out = new float[population.get(0).getFitness().length];
        for (Ranking r : population) {
            double[] fitness = r.getFitness();
            for (int i = 0; i < out.length; i++) {
                out[i] += fitness[i];
            }
        }
        for (int i = 0; i < out.length; i++) {
            out[i] /= population.size();
        }
        return out;
    }

    public static boolean isAParetoDominateByB(double fitnessA[], double fitnessB[]) {
        boolean dominate = false;
        for (int i = 0; i < fitnessA.length; i++) {
            if (fitnessA[i] > fitnessB[i]) {
                dominate = true;
            } else if (fitnessA[i] < fitnessB[i]) {
                return false;
            }
        }
        return dominate;
    }

    public static List<Ranking> getFirstParetoFront(List<Ranking> tmp) {
        List<Ranking> front = new LinkedList<>();

        for (Ranking r1 : tmp) {
            boolean isDominate = false;
            for (Ranking r2 : tmp) {
                if (r1.equals(r2)) {
                    continue;
                }
                if (Utils.isAParetoDominateByB(r1.getFitness(), r2.getFitness())) {
                    isDominate = true;
                    break;
                }
            }
            if (!isDominate) {
                front.add(r1);
            }
        }
        return front;
    }

    public static String cpuInfo() throws IOException {
        if (!System.getProperty("os.name").toLowerCase().contains("linux")) {
            return "Unaviable";
        }
        FileInputStream fis = new FileInputStream(new File("/proc/cpuinfo"));
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader bufferedReader = new BufferedReader(isr);
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            if (line.matches("model name.*")) {
                bufferedReader.close();
                return line.replace("model name	: ", "");
            }
        }
        return "";
    }

    public static double diversity(List<Ranking> population) {
        Set<String> tmp = new HashSet<>();
        for (Ranking r : population) {
            tmp.add(r.getDescription());
        }
        return 100 * tmp.size() / (double) population.size();
    }

    /**
     * Calculates how many individuals in populationB don't exist in
     * populationA.
     *
     * @param populationA
     * @param populationB
     * @return
     */
    public static double intraPopulationsDiversity(List<Ranking> populationA, List<Ranking> populationB) {
        Set<String> regexInASet = new HashSet<>();
        for (Ranking r : populationA) {
            regexInASet.add(r.getDescription());
        }
        int newRegexesCount = 0;
        for (Ranking r : populationB) {
            if (!regexInASet.contains(r.getDescription())) {
                newRegexesCount++;
            }
        }
        return 100 * (double) newRegexesCount / (double) populationB.size();
    }

    //remove empty extractions 
    public static void removeEmptyExtractions(List<Bounds> extractions) {
        for (Iterator<Bounds> it = extractions.iterator(); it.hasNext();) {
            Bounds bounds = it.next();
            if (bounds.size() == 0) {
                it.remove();
            }
        }
    }

    public static void saveFile(String text, String pathOfFile) {
        Writer writer;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(pathOfFile), "utf-8");
            writer.write(text);
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, "Cannot save:", ex);
        }
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    private static transient final Set<Character> quoteList = new TreeSet<Character>(
            Arrays.asList(new Character[]{'?', '+', '*', '.', '[', ']', '\\', '$', '(', ')', '^', '{', '|', '-', '&'}));

    /**
     * Returns a set with all n-grams; 1<n<4
     *
     * @param word
     * @return
     */
    public static Set<String> subparts(String word) {
        return Utils.subparts(word, 1, 4);
    }

    /**
     * Returns a set with all n-grams; 1<n<4
     *
     * @param word
     * @return
     */
    public static Set<String> subparts(String word, int nMin, int nMax) {
        Set<String> subparts = new HashSet<>();
        for (int n = nMin; n <= nMax; n++) {
            for (int i = 0; i < word.length(); i++) {
                StringBuilder builder = new StringBuilder();
                String w = word.substring(i, Math.min(i + n, word.length()));
                for (char c : w.toCharArray()) {
                    builder.append(escape(c));
                }
                subparts.add(builder.toString());
            }
        }
        return subparts;
    }

    public static String escape(char c) {
        if (quoteList.contains(c)) {
            return ("\\" + c);
        } else {
            switch (c) {
                case '\n':
                    return "\\n";
                case '\t':
                    return "\\t";
            }
        }
        return ("" + c);
    }

    public static String escape(String string) {
        StringBuilder stringBuilder = new StringBuilder();
        char[] stringChars = string.toCharArray();
        for (char character : stringChars) {
            stringBuilder.append(escape(character));
        }
        return stringBuilder.toString();
    }

    /**
     * Generates RegexRanges i.e. [a-z] from contiguous characters into the
     * <code>charset</code> list. Follows example where output is represented
     * with regex strings: When <code>charset</code> is {a,b,g,r,t,u,v,5}, the
     * return value is {[a-b],[t-v]}.
     *
     * @param charset the character list i.e. {a,b,g,r,t,u,v,5}
     * @return the contiguous character ranges i.e. {[a-b],[t-v]}
     */
    public static List<RegexRange> generateRegexRanges(Collection<Character> charset) {
        List<RegexRange> regexRangesList = new LinkedList<>();
        TreeSet<Character> orderedCharset = new TreeSet<>(charset);
        Character start = null;
        Character old = null;
        for (Character c : charset) {
            if (old == null) {
                //The first round
                old = orderedCharset.first();
                start = old;
                continue; //pick the next 
            }
            //when we have an "hole" or is the last character it checks if the previous range (old -start) is larger than 1; 
            //Ranges bigger than 1 char are saved
            if (((c - old) > 1 || Objects.equals(orderedCharset.last(), c))) {
                if ((old - start) > 1) {
                    regexRangesList.add(new RegexRange(escape(start) + "-" + escape(old)));
                }
                start = c;
            }
            old = c;
        }
        return regexRangesList;
    }

    public static String printPopulation(List<Ranking> rankings) {
        StringBuilder ret = new StringBuilder();
        int i = 0;
        if (rankings != null) {
            for (Ranking r : rankings) {
                StringBuilder b = new StringBuilder();
                r.getTree().describe(b);
                ret.append(i).append(" - ").append(Utils.printArray(r.getFitness())).append("\t").append(printableRegex(b.toString())).append((i % 4) == 3 ? "\n" : "");
                i++;
            }
        }
        return ret.toString();
    }

    public static String printArray(double[] fitness) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(Math.round(fitness[0] * 100) / 100f);
        for (int i = 1; i < fitness.length; i++) {
            sb.append(",");
            sb.append(Math.round(fitness[i] * 100) / 100f);
        }
        sb.append("]");
        return sb.toString();
    }

    //This version should be more efficient and takes into account the duplicates
    //We take the mappings that are longer and soruce is more on the right... and overllaping mappings are rejected (on both source and destination)
    public static List<Pair<Bounds, Bounds>> findLargestCommonSubstringNew(String s1, String s2) {
        List<Bounds> sourceCovered = new LinkedList();
        List<Bounds> targetCovered = new LinkedList();
        List<Pair<Bounds, Bounds>> mappings = new LinkedList<>();

        for (int i = 0; i < s1.length(); i++) {
            for (int j = i + 1; j <= s1.length(); j++) {

                String subString = s1.substring(i, j);
                int findIndex = -1;
                do {
                    findIndex = s2.indexOf(subString, findIndex + 1);
                    if (findIndex >= 0) {
                        mappings.add(new Pair<>(new Bounds(i, j), new Bounds(findIndex, findIndex + subString.length())));
                    }
                } while (findIndex >= 0);
            }
        }

        Collections.sort(mappings, (Pair<Bounds, Bounds> f1, Pair<Bounds, Bounds> f2) -> {
            //forces descendind order per bounds size and when equals for ascending bounds start index
            int deltaLength = f2.getFirst().size() - f1.getFirst().size();
            int outcome;
            if (deltaLength != 0) {
                outcome = deltaLength;
            } else {
                outcome = f1.getFirst().start - f2.getFirst().start;
            }
            return (outcome);
        });

        //filter
        for (Iterator<Pair<Bounds, Bounds>> iterator = mappings.iterator(); iterator.hasNext();) {
            Pair<Bounds, Bounds> next = iterator.next();
            Bounds sourcePiece = next.getFirst();
            Bounds targetPiece = next.getSecond();
            //When we do not overlap source and target, already picked peaces
            if (!(sourcePiece.overlaps(sourceCovered) || targetPiece.overlaps(targetCovered))) {
                sourceCovered.add(sourcePiece);
                targetCovered.add(targetPiece);
            } else {
                iterator.remove();
            }
        }

        //sorts for startindex
        //if(mappingOrderLikeSource){
        Collections.sort(mappings, (Pair<Bounds, Bounds> f1, Pair<Bounds, Bounds> f2) -> (f1.getFirst().compareTo(f2.getFirst())));
        //} else {
        //    Collections.sort(mappings, (Pair<Bounds, Bounds> f1, Pair<Bounds, Bounds> f2) -> (f1.getSecond().compareTo(f2.getSecond())));
        //}

        return mappings;
    }

    public static double complexityRegex(Node node, boolean pushGeneralization) {
        int quantifierWeigth = pushGeneralization ? 6 : 10;
        int charClassWeight = pushGeneralization ? 8 : 10;
        Queue<Node> currentNodes = new LinkedList<>();
        currentNodes.offer(node);
        int complexity = 0;
        while (!currentNodes.isEmpty()) {
            Node pollNode = currentNodes.poll();
            currentNodes.addAll(pollNode.getChildrens());
            if (pollNode instanceof Constant) {
                Constant constant = (Constant) pollNode;
                complexity += constant.isCharacterClass() ? charClassWeight : constant.toString().length() * 10;
            } else if (pollNode instanceof Quantifier) {
                complexity += quantifierWeigth;
            } else if (!(pollNode instanceof Concatenator)) {
                complexity += 10;
            }
            //ListMatch and ListUnMatch are considered like "other things" at the moment
        }
        return (complexity / 10.0);
    }

    public static double complexityReplace(Node node) {
        int replaceWeight = 6;
        Queue<Node> currentNodes = new LinkedList<>();
        currentNodes.offer(node);
        int complexity = 0;
        while (!currentNodes.isEmpty()) {
            Node pollNode = currentNodes.poll();
            currentNodes.addAll(pollNode.getChildrens());
            if (pollNode instanceof Constant) {
                Constant constant = (Constant) pollNode;
                if (isStringAReplaceExpression(constant.toString())) {
                    complexity += replaceWeight;
                } else {
                    complexity += constant.toString().length() * 10;
                }
            } //only constants and concatenations
        }
        return (complexity / 10.0);
    }

    private static boolean isStringAReplaceExpression(String string) {
        if (string.length() <= 1) {
            return false;
        }
        char[] toCharArray = string.toCharArray();
        boolean isReplace = toCharArray[0] == '$';
        for (int i = 1; i < toCharArray.length; i++) {
            isReplace &= Character.isDigit(toCharArray[i]);
        }
        return isReplace;
    }

}
