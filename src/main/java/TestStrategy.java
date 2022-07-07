//import loaders.CsvTradesLoader;
//import org.ta4j.core.BarSeries;
//import org.ta4j.core.BarSeriesManager;
//import org.ta4j.core.BaseStrategy;
//import org.ta4j.core.Indicator;
//import org.ta4j.core.Rule;
//import org.ta4j.core.Strategy;
//import org.ta4j.core.TradingRecord;
//import org.ta4j.core.analysis.criteria.pnl.GrossReturnCriterion;
//import org.ta4j.core.indicators.AroonOscillatorIndicator;
//import org.ta4j.core.indicators.HMAIndicator;
//import org.ta4j.core.indicators.RSIIndicator;
//import org.ta4j.core.indicators.SMAIndicator;
//import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator;
//import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
//import org.ta4j.core.indicators.helpers.HighPriceIndicator;
//import org.ta4j.core.indicators.helpers.LowPriceIndicator;
//import org.ta4j.core.indicators.helpers.OpenPriceIndicator;
//import org.ta4j.core.indicators.ichimoku.IchimokuChikouSpanIndicator;
//import org.ta4j.core.indicators.ichimoku.IchimokuKijunSenIndicator;
//import org.ta4j.core.indicators.ichimoku.IchimokuLineIndicator;
//import org.ta4j.core.indicators.ichimoku.IchimokuTenkanSenIndicator;
//import org.ta4j.core.indicators.volume.MVWAPIndicator;
//import org.ta4j.core.rules.CrossedDownIndicatorRule;
//import org.ta4j.core.rules.CrossedUpIndicatorRule;
//import org.ta4j.core.rules.OverIndicatorRule;
//import org.ta4j.core.rules.StopLossRule;
//import org.ta4j.core.rules.UnderIndicatorRule;
//
//public class TestStrategy {
//    public static Strategy buildStrategy(BarSeries series) {
//
//        if (series == null) {
//            throw new IllegalArgumentException("Series cannot be null");
//        }
//
////
////        AroonOscillatorIndicator aroonOscillatorIndicator =
////                new AroonOscillatorIndicator(series,14);
////        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
////        RSIIndicator rsiIndicator = new RSIIndicator(closePrice,14);
////        SMAIndicator smaIndicator = new SMAIndicator(closePrice,10);
////
////        Rule entryRule = new CrossedUpIndicatorRule(rsiIndicator,50)
////                .and(new OverIndicatorRule(closePrice,smaIndicator))
////                .and(new CrossedUpIndicatorRule(aroonOscillatorIndicator.getAroonUpIndicator(),aroonOscillatorIndicator.getAroonDownIndicator()));
////
////        Rule exitRule = new UnderIndicatorRule(closePrice,smaIndicator)
////                .or(new CrossedUpIndicatorRule(aroonOscillatorIndicator.getAroonUpIndicator(),80));
////
//
//
//        //scalping 12
//
////        ClosePriceIndicator closePriceIndicator =
////                new ClosePriceIndicator(series);
////        SMAIndicator sma9Indicator = new SMAIndicator(closePriceIndicator,9);
////        SMAIndicator sma18Indicator = new SMAIndicator(closePriceIndicator,18);
////
////        Rule entryRule = new CrossedUpIndicatorRule(sma9Indicator,sma18Indicator)
////                .and(new CrossedUpIndicatorRule(closePriceIndicator,
////                        sma18Indicator));
////
////        Rule exitRule  = new UnderIndicatorRule(closePriceIndicator,
////                sma9Indicator);
//
//
////        // scalping 10
////
////                ClosePriceIndicator closePriceIndicator =
////                new ClosePriceIndicator(series);
////        SMAIndicator sma15Indicator = new SMAIndicator(closePriceIndicator,15);
////        SMAIndicator sma45Indicator = new SMAIndicator(closePriceIndicator,45);
////        RSIIndicator rsiIndicator = new RSIIndicator(closePriceIndicator,5);
////
////
////        Rule entryRule = new CrossedUpIndicatorRule(ma,sma18Indicator)
////                .and(new CrossedUpIndicatorRule(closePriceIndicator,
////                        sma18Indicator));
////
////        Rule exitRule  = new UnderIndicatorRule(closePriceIndicator,
////                sma9Indicator);
//
//        //blue
//        IchimokuTenkanSenIndicator ichimokuTenkanSenIndicator =
//                new IchimokuTenkanSenIndicator(series,9);
//        //red
//        IchimokuKijunSenIndicator ichimokuKijunSenIndicator =
//                new IchimokuKijunSenIndicator(series,26);
//
//        Rule entryRule =
//                new CrossedUpIndicatorRule(ichimokuTenkanSenIndicator,
//                        ichimokuKijunSenIndicator);
//        Rule exitRule = new CrossedUpIndicatorRule(ichimokuKijunSenIndicator,
//                ichimokuTenkanSenIndicator);
//
//        entryRule.and(new StopLossRule())
//
//
////
////        HighPriceIndicator highPriceIndicator = new HighPriceIndicator(series);
////        OpenPriceIndicator openPriceIndicator = new OpenPriceIndicator(series);
////        LowPriceIndicator lowPriceIndicator = new LowPriceIndicator(series);
////
////
////        SMAIndicator shortSma = new SMAIndicator(closePrice, 5);
////        SMAIndicator longSma = new SMAIndicator(closePrice, 200);
////
////        // We use a 2-period RSI indicator to identify buying
////        // or selling opportunities within the bigger trend.
////        RSIIndicator rsi = new RSIIndicator(closePrice, 2);
////
////        // Entry rule
////        // The long-term trend is up when a security is above its 200-period SMA.
////        Rule entryRule = new OverIndicatorRule(shortSma, longSma) // Trend
////                .and(new CrossedDownIndicatorRule(rsi, 5)) // Signal 1
////                .and(new OverIndicatorRule(shortSma, closePrice)); // Signal 2
////
////        // Exit rule
////        // The long-term trend is down when a security is below its 200-period SMA.
////        Rule exitRule = new UnderIndicatorRule(shortSma, longSma) // Trend
////                .and(new CrossedUpIndicatorRule(rsi, 95)) // Signal 1
////                .and(new UnderIndicatorRule(shortSma, closePrice)); // Signal 2
//        // TODO: Finalize the strategy
//
//        return new BaseStrategy(entryRule, exitRule);
////        return new BaseStrategy(exitRule,entryRule);
//    }
//
//    public static void main(String[] args) {
//
//        // Getting the bar series
//        BarSeries series = CsvTradesLoader.loadBitstampSeries();
////        series.setMaximumBarCount(400);
//        // Building the trading strategy
//        Strategy strategy = buildStrategy(series);
//
//
//        // Running the strategy
//        BarSeriesManager seriesManager = new BarSeriesManager(series);
//        TradingRecord tradingRecord = seriesManager.run(strategy);
//        tradingRecord.getPositions().forEach(System.out::println);
//
//        System.out.println("Number of positions for the strategy: " + tradingRecord.getPositionCount());
//
//        // Analysis
//        System.out.println("Total return for the strategy: " + new GrossReturnCriterion().calculate(series, tradingRecord));
//    }
//
//}
