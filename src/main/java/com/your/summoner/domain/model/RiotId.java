package com.your.summoner.domain.model;

import com.your.summoner.domain.exception.InvalidInputException;

/** Identificador público do jogador: {@code gameName#tagLine}. */
public record RiotId(String gameName, String tagLine) {

    public RiotId {
        if (gameName == null || gameName.isBlank()) {
            throw new InvalidInputException("Nickname is empty or blank");
        }
        if (tagLine == null || tagLine.isBlank() || tagLine.trim().equals("#")) {
            throw new InvalidInputException("Tag is empty or blank");
        }
        gameName = gameName.trim();
        tagLine = tagLine.trim().replaceFirst("^#", "");
    }

    @Override
    public String toString() {
        return gameName + "#" + tagLine;
    }
}
