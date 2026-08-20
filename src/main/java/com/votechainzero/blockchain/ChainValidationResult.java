package com.votechainzero.blockchain;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;


@Getter
public class ChainValidationResult {

    private boolean valid = true;
    private final List<String> errors = new ArrayList<>();

    public void addError(String error) {
        this.valid = false;
        this.errors.add(error);
    }
}