/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2017 Marc de Verdelhan, 2017-2021 Ta4j Organization & respective
 * authors (see AUTHORS)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import loaders.CsvTradesLoader;
import org.ta4j.core.*;
import org.ta4j.core.analysis.criteria.pnl.GrossReturnCriterion;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.StochasticOscillatorKIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;


/**
 * Moving momentum strategy.
 *
 * @see <a href=
 *      "http://stockcharts.com/help/doku.php?id=chart_school:trading_strategies:moving_momentum">
 *      http://stockcharts.com/help/doku.php?id=chart_school:trading_strategies:moving_momentum</a>
 */
public class MovingMomentumStrategy {

    /**
     * @param series the bar series
     * @return the moving momentum strategy
     */
    public static Strategy buildStrategy(BarSeries series,
                                         int shortEMABarCount,
                                         int longEMABarCount,
                                         int stochasticOscillKBarCount,
                                         int MACDShortBarCount,
                                         int MACDLongBarCount,
                                         int EMAMACDBarCount,
                                         int entryThreshold,
                                         int exitThreshold
                                         ) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        // The bias is bullish when the shorter-moving average moves above the longer
        // moving average.
        // The bias is bearish when the shorter-moving average moves below the longer
        // moving average.
        EMAIndicator shortEma = new EMAIndicator(closePrice, shortEMABarCount);
        EMAIndicator longEma = new EMAIndicator(closePrice, longEMABarCount);

        StochasticOscillatorKIndicator stochasticOscillK =
                new StochasticOscillatorKIndicator(series, stochasticOscillKBarCount);

        MACDIndicator macd = new MACDIndicator(closePrice, MACDShortBarCount,
                MACDLongBarCount);
        EMAIndicator emaMacd = new EMAIndicator(macd, EMAMACDBarCount);

        // Entry rule
        Rule entryRule = new OverIndicatorRule(shortEma, longEma) // Trend
                .and(new CrossedDownIndicatorRule(stochasticOscillK, entryThreshold)) //
                // Signal 1
                .and(new OverIndicatorRule(macd, emaMacd)); // Signal 2

        // Exit rule
        Rule exitRule = new UnderIndicatorRule(shortEma, longEma) // Trend
                .and(new CrossedUpIndicatorRule(stochasticOscillK, exitThreshold)) //
                // Signal 1
                .and(new UnderIndicatorRule(macd, emaMacd)); // Signal 2

        return new BaseStrategy(entryRule, exitRule);
    }

    public static void main(String[] args) {

        // Getting the bar series
        BarSeries series = CsvTradesLoader.loadBitstampSeries();

        long l = System.currentTimeMillis();
        // Building the trading strategy
        Strategy strategy = buildStrategy(series,0,0,0,0,0,0
        ,0,0);

        // Running the strategy
        BarSeriesManager seriesManager = new BarSeriesManager(series);
        TradingRecord tradingRecord = seriesManager.run(strategy);
        System.out.println("Number of positions for the strategy: " + tradingRecord.getPositionCount());
        System.out.println(System.currentTimeMillis()-l);
        // Analysis
        System.out.println(
                "Total profit for the strategy: " + new GrossReturnCriterion().calculate(series, tradingRecord));
    }
}
