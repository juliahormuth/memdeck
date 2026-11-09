package br.com.memdeck.deck.application;

import br.com.memdeck.deck.domain.Visibility;

public record CreateDeckCommand(String name, String description, Visibility visibility) {
}
