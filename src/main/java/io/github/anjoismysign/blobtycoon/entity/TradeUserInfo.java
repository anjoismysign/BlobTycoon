package io.github.anjoismysign.blobtycoon.entity;

import io.github.anjoismysign.blobtycoon.entity.plothelper.PlotHelperContainer;
import io.github.anjoismysign.blobtycoon.entity.plothelper.PlotHelperTrade;
import org.jetbrains.annotations.Nullable;

public class TradeUserInfo {
    @Nullable
    protected String tradeQuery;
    @Nullable
    protected PlotHelperTrade trade;
    @Nullable
    protected PlotHelperContainer communityTrade;

    protected TradeUserInfo() {
    }
}
