package us.mytheria.blobtycoon.entity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.mytheria.blobtycoon.entity.plothelper.PlotHelperContainer;
import us.mytheria.blobtycoon.entity.plothelper.PlotHelperTrade;

import java.util.Objects;

public interface TradeUser {
    /**
     * Used internally.
     *
     * @return The TradeUserInfo
     */
    @NotNull
    TradeUserInfo getTradeUserInfo();

    /**
     * The trade query used for marketplace.
     *
     * @return null if no query is set.
     */
    @Nullable
    default String getTradeQuery() {
        return getTradeUserInfo().tradeQuery;
    }

    /**
     * The trade query used for marketplace.
     *
     * @param tradeQuery null to clear the query
     */
    default void setTradeQuery(@Nullable String tradeQuery) {
        getTradeUserInfo().tradeQuery = tradeQuery;
    }

    /**
     * The trade that is currently being viewed.
     *
     * @return null if player has not viewed any trade yet (lasts per session)
     */
    @Nullable
    default PlotHelperTrade getTrade() {
        return getTradeUserInfo().trade;
    }

    /**
     * The trade that is currently being viewed.
     *
     * @param trade The trade that will be viewed.
     */
    default void setTrade(@NotNull PlotHelperTrade trade) {
        Objects.requireNonNull(trade, "'trade' cannot be null");
        getTradeUserInfo().trade = trade;
    }

    /**
     * The community trade that is currently being viewed
     *
     * @return null if player is not viewing a community trade at this time
     */
    @Nullable
    default PlotHelperContainer getCommunityTrade() {
        return getTradeUserInfo().communityTrade;
    }

    /**
     * The community trade that is currently being viewed
     *
     * @param communityTrade the community trade to use, or null to clear
     */
    default void setCommunityTrade(@Nullable PlotHelperContainer communityTrade) {
        getTradeUserInfo().communityTrade = communityTrade;
    }
}
