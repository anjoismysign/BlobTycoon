package io.github.anjoismysign.blobtycoon.entity;

import org.jetbrains.annotations.Nullable;
import io.github.anjoismysign.blobtycoon.entity.plothelper.PlotHelperContainer;
import io.github.anjoismysign.blobtycoon.entity.plothelper.PlotHelperTrade;

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
