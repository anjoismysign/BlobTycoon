package io.github.anjoismysign.entity.structure;

import io.github.anjoismysign.entity.Sellable;

public record TycoonModelHolderData<T extends TycoonModel>(T getModel, Sellable getSellable) {
}
