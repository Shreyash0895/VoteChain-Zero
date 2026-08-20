package com.votechainzero.blockchain;

import java.util.ArrayList;
import java.util.List;

final class MerkleTree {

    private MerkleTree() {
        // utility class
    }

  
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
}