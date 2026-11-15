package net.guizhanss.gcereborn.core.genetics;

import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.Nonnull;

import net.guizhanss.gcereborn.GeneticChickengineering;

import lombok.Getter;
import lombok.experimental.Accessors;

public class DNA {

    @Getter
    @Accessors(fluent = true)
    private static final char[] ALLELES = new char[] {'b', 'c', 'd', 'f', 's', 'w'};
    private static final boolean[] BOOL_CAST = new boolean[] {false, true};
    private final Gene[] sequence;
    private boolean learned;

    /**
     * Load DNA from state.
     *
     * @param state int[] of length 7, first 6 are alleles, last is learned
     */
    public DNA(int[] state) {
        // Load DNA from state
        this.sequence = new Gene[6];
        for (int i = 0; i < 6; i++) {
            this.sequence[i] = new Gene(ALLELES[i], state[i]);
        }
        this.learned = BOOL_CAST[state[6]];
    }

    /**
     * Load DNA from state.
     *
     * @param state String of length 7, first 6 are alleles, last is learned
     */
    public DNA(@Nonnull String state) {
        // Load DNA from a String state
        char[] stateChars = state.toCharArray();
        this.sequence = new Gene[6];
        for (int i = 0; i < 6; i++) {
            this.sequence[i] = new Gene(ALLELES[i], Character.getNumericValue(stateChars[i]));
        }
        this.learned = BOOL_CAST[Character.getNumericValue(stateChars[6])];
    }

    /**
     * Load DNA from a chicken type.
     *
     * @param typing The chicken type, which contains the state information.
     */
    public DNA(int typing) {
        this.sequence = new Gene[6];
        for (int i = 0; i < 6; i++) {
            // bit (5 - i) of typing determines whether gene i is dominant
            int bit = (typing >> (5 - i)) & 1;
            this.sequence[i] = new Gene(ALLELES[i], 3 * bit);
        }
        this.learned = true;
    }

    /**
     * Load DNA from 2 parents' alleles.
     *
     * @param half1 The first parent's alleles
     * @param half2 The second parent's alleles.
     */
    public DNA(char[] half1, char[] half2) {
        // New DNA from two parent halves
        this.sequence = new Gene[6];
        for (int i = 0; i < 6; i++) {
            this.sequence[i] = new Gene(new char[] {half1[i], half2[i]});
        }
        this.learned = false;
    }

    /**
     * Load DNA from notation.
     *
     * @param notation The notation of the DNA, should be 12 long and only contain alleles
     */
    public DNA(char[] notation) {
        this.sequence = new Gene[6];
        for (int i = 0; i < 6; i++) {
            this.sequence[i] = new Gene(new char[] {notation[2 * i], notation[2 * i + 1]});
        }
        this.learned = true;
    }

    /**
     * Create random DNA sequence, for new captured chickens.
     */
    public DNA() {
        var config = GeneticChickengineering.getConfigService();
        this.sequence = new Gene[6];
        ThreadLocalRandom random = ThreadLocalRandom.current();
        boolean[] mutated = new boolean[6];
        random.ints(0, 6).distinct().limit(config.getMaxMutation()).forEach(x -> mutated[x] = true);

        for (int i = 0; i < 6; i++) {
            char notation = Character.toUpperCase(ALLELES[i]);
            char[] markup = {notation, notation};
            // If this index was in the ones randomly chosen,
            // This allele is heterozygous
            if (mutated[i] && random.nextInt(100) < config.getMutationRate()) {
                markup[1] = ALLELES[i];
            }
            this.sequence[i] = new Gene(markup);
        }

        this.learned = false;
    }

    public static boolean isValidSequence(String seq) {
        return seq != null && isValidSequence(seq.toCharArray());
    }

    public static boolean isValidSequence(char[] seq) {
        if (seq == null || seq.length != 12) {
            return false;
        }
        for (int i = 0; i < 6; i++) {
            char allele = ALLELES[i];
            if (Character.toLowerCase(seq[2 * i]) != allele ||
                Character.toLowerCase(seq[2 * i + 1]) != allele) {
                return false;
            }
        }
        return true;
    }

    @Nonnull
    @Override
    public String toString() {
        StringBuilder out = new StringBuilder(12);
        for (int i = 0; i < 6; i++) {
            out.append(this.sequence[i].toString());
        }
        return out.toString();
    }

    public char[] split() {
        char[] out = new char[6];
        for (int i = 0; i < 6; i++) {
            out[i] = this.sequence[i].split();
        }
        return out;
    }

    public Gene[] getGenes() {
        return this.sequence;
    }

    public Gene getGene(int index) {
        return this.sequence[index];
    }

    public int[] getState() {
        int[] out = new int[7];
        for (int i = 0; i < 6; i++) {
            out[i] = this.sequence[i].getState();
        }
        out[6] = this.learned ? 1 : 0;
        return out;
    }

    public String getStateString() {
        char[] out = new char[7];
        int[] state = getState();
        for (int i = 0; i < 7; i++) {
            out[i] = (char) (state[i] + '0');
        }
        return new String(out);
    }

    public int getTyping() {
        int out = 0;
        for (int i = 0; i < 6; i++) {
            if (this.sequence[i].isDominant()) {
                out |= 1 << (5 - i);
            }
        }
        return out;
    }

    public int getTier() {
        int out = 0;
        for (int i = 0; i < 6; i++) {
            if (!this.sequence[i].isDominant()) {
                out++;
            }
        }
        return out;
    }

    public boolean isKnown() {
        return this.learned;
    }

    public void learn() {
        this.learned = true;
    }
}
