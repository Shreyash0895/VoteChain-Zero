package com.votechainzero.blockchain;

import java.util.ArrayList;
import java.util.List;

final class MerkleTree {

    private MerkleTree() {
        // utility class
    }

    /** One step of a Merkle proof: the sibling hash and which side it sits on. */
    record ProofStep(String hash, String position) { }

    public static String computeMerkleRoot(List<String> transactionHashes) {
        if (transactionHashes == null || transactionHashes.isEmpty()) {
            return HashUtil.sha256("");
        }

        List<String> currentLevel = new ArrayList<>(transactionHashes);

        while (currentLevel.size() > 1) {
            List<String> nextLevel = new ArrayList<>();

            for (int i = 0; i < currentLevel.size(); i += 2) {
                String left = currentLevel.get(i);
                // duplicate the last node if this level has an odd count
                String right = (i + 1 < currentLevel.size()) ? currentLevel.get(i + 1) : left;
                nextLevel.add(HashUtil.sha256(left + right));
            }

            currentLevel = nextLevel;
        }

        return currentLevel.get(0);
    }

    /**
     * Builds the proof path for the leaf at {@code index}: the sibling hash at every
     * level from the leaves up to (but not including) the root. Mirrors
     * computeMerkleRoot exactly, including duplicating the last node on odd levels.
     */
    public static List<ProofStep> buildProof(List<String> leaves, int index) {
        List<ProofStep> proof = new ArrayList<>();
        List<String> level = new ArrayList<>(leaves);
        int idx = index;

        while (level.size() > 1) {
            boolean isRight = (idx % 2 == 1);
            int siblingIdx = isRight ? idx - 1 : idx + 1;
            String sibling = siblingIdx < level.size() ? level.get(siblingIdx) : level.get(idx);
            proof.add(new ProofStep(sibling, isRight ? "LEFT" : "RIGHT"));

            List<String> next = new ArrayList<>();
            for (int i = 0; i < level.size(); i += 2) {
                String left = level.get(i);
                String right = (i + 1 < level.size()) ? level.get(i + 1) : left;
                next.add(HashUtil.sha256(left + right));
            }
            level = next;
            idx = idx / 2;
        }
        return proof;
    }

    /** Folds a leaf up through a proof and returns the resulting root. */
    public static String applyProof(String leaf, List<ProofStep> proof) {
        String current = leaf;
        for (ProofStep step : proof) {
            current = "LEFT".equals(step.position())
                    ? HashUtil.sha256(step.hash() + current)
                    : HashUtil.sha256(current + step.hash());
        }
        return current;
    }
}