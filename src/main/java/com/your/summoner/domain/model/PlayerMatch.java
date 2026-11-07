package com.your.summoner.domain.model;

/** Partida junto com o participante que é o dono do histórico. */
public record PlayerMatch(Match match, Match.Participant player) {
}
