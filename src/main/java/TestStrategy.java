import loaders.CsvTradesLoader;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BarSeriesManager;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Indicator;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.analysis.criteria.pnl.GrossReturnCriterion;
import org.ta4j.core.indicators.AroonOscillatorIndicator;
import org.ta4j.core.indicators.HMAIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.HighPriceIndicator;
import org.ta4j.core.indicators.helpers.LowPriceIndicator;
import org.ta4j.core.indicators.helpers.OpenPriceIndicator;
import org.ta4j.core.indicators.ichimoku.IchimokuChikouSpanIndicator;
import org.ta4j.core.indicators.ichimoku.IchimokuKijunSenIndicator;
import org.ta4j.core.indicators.ichimoku.IchimokuLineIndicator;
import org.ta4j.core.indicators.ichimoku.IchimokuTenkanSenIndicator;
import org.ta4j.core.indicators.volume.MVWAPIndicator;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.StopLossRule;
import org.ta4j.core.rules.UnderIndicatorRule;

public class TestStrategy {
    public static Strategy buildStrategy(BarSeries series) {

        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        //blue
        IchimokuTenkanSenIndicator ichimokuTenkanSenIndicator =
                new IchimokuTenkanSenIndicator(series,9);
        //red
        IchimokuKijunSenIndicator ichimokuKijunSenIndicator =
                new IchimokuKijunSenIndicator(series,26);

        Rule entryRule =
                new CrossedUpIndicatorRule(ichimokuTenkanSenIndicator,
                        ichimokuKijunSenIndicator);
        Rule exitRule = new CrossedUpIndicatorRule(ichimokuKijunSenIndicator,
                ichimokuTenkanSenIndicator);


        return new BaseStrategy(entryRule, exitRule);
//        return new BaseStrategy(exitRule,entryRule);
    }

    public static void main(String[] args) {

        // Getting the bar series
        BarSeries series = CsvTradesLoader.loadBitstampSeries();
//        series.setMaximumBarCount(400);
        // Building the trading strategy
        Strategy strategy = buildStrategy(series);


        // Running the strategy
        BarSeriesManager seriesManager = new BarSeriesManager(series);
        TradingRecord tradingRecord = seriesManager.run(strategy);
        tradingRecord.getPositions().forEach(System.out::println);

        System.out.println("Number of positions for the strategy: " + tradingRecord.getPositionCount());

        // Analysis
        System.out.println("Total return for the strategy: " + new GrossReturnCriterion().calculate(series, tradingRecord));
    }

}
