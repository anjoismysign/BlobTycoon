package us.mytheria.blobtycoon.entity.structure;

import us.mytheria.blobtycoon.entity.Sellable;

public record TycoonModelHolderData<T extends TycoonModel>(T getModel, Sellable getSellable) {
}
