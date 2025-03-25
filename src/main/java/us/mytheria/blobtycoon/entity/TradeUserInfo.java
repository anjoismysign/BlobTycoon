package us.mytheria.blobtycoon.entity;

import org.jetbrains.annotations.Nullable;
import us.mytheria.blobtycoon.entity.plothelper.PlotHelperContainer;
import us.mytheria.blobtycoon.entity.plothelper.PlotHelperTrade;

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
