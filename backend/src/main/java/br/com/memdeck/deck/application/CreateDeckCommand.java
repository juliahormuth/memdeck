package br.com.memdeck.deck.application;

import java.util.UUID;

import br.com.memdeck.deck.domain.Visibility;

public record CreateDeckCommand(UUID ownerId, String name, String description, Visibility visibility) {
}
