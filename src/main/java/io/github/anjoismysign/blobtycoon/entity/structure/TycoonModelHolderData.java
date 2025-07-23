package io.github.anjoismysign.blobtycoon.entity.structure;

import io.github.anjoismysign.blobtycoon.entity.Sellable;

public record TycoonModelHolderData<T extends TycoonModel>(T getModel, Sellable getSellable) {
}
