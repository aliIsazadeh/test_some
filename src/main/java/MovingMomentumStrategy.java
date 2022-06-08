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
import org.ta4j.core.BarSeries;
import org.ta4j.core.BarSeriesManager;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.analysis.criteria.pnl.GrossReturnCriterion;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.StochasticOscillatorKIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;
import result.ResultModel;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


/**
 * Moving momentum strategy.
 *
 * @see <a href=
 * "http://stockcharts.com/help/doku.php?id=chart_school:trading_strategies:moving_momentum">
 * http://stockcharts.com/help/doku.php?id=chart_school:trading_strategies:moving_momentum</a>
 */
public class MovingMomentumStrategy {

    /**
     * @param series the bar series
     * @return the moving momentum strategy
     */
    public static Strategy buildStrategy(BarSeries series, int shortEMABarCount, int longEMABarCount, int stochasticOscillKBarCount, int MACDShortBarCount, int MACDLongBarCount, int EMAMACDBarCount, int entryThreshold, int exitThreshold) {
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

        StochasticOscillatorKIndicator stochasticOscillK = new StochasticOscillatorKIndicator(series, stochasticOscillKBarCount);

        MACDIndicator macd = new MACDIndicator(closePrice, MACDShortBarCount, MACDLongBarCount);
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

    public static void main(String[] args) throws IOException {

        // Getting the bar series
        BarSeries series = CsvTradesLoader.loadBitstampSeries();
        //idea: use fibonachi values for this array
        // maybe we found sexy results :)
//        int[] EMAValues = new int[]{9, 12, 15};
        int[] EMAValues1 = new int[]{
                9, 12, 15,
//                24, 26, 28,
//                30, 50, 80,
//                150, 200, 250
        };
        int[] EMAValues = new int[]{9, 12, 15, 24, 26, 28, 30, 50, 80, 150, 200, 250};

        long l = System.currentTimeMillis();
        // Building the trading strategy
        Strategy strategy = null;
        ResultModel resultModel = null;
        List<ResultModel> resultModels = new ArrayList<>();

        for (int shortEMABarCount : EMAValues1) {
            for (int longEMABarCount : EMAValues) {
                for (int stoch = 2; stoch <= 30; stoch += 2) {
                    for (int shortMACD = 7; shortMACD < 33; shortMACD += 2) {
                        for (int longMACD = 7; longMACD < 33; longMACD += 2) {
                            if (longMACD < shortMACD) continue;

                            for (int EMAMACD : EMAValues) {
                                for (int entry = 10; entry <= 150; entry += 10) {
                                    for (int exit = 10; exit <= 150; exit += 10) {
                                        strategy = buildStrategy(series, shortEMABarCount, longEMABarCount, stoch, shortMACD, longMACD, EMAMACD, entry, exit);
                                        BarSeriesManager seriesManager = new BarSeriesManager(series);
                                        TradingRecord tradingRecord = seriesManager.run(strategy);
                                        if (tradingRecord.getPositionCount() > 0) {
                                            Num result = new GrossReturnCriterion().calculate(series, tradingRecord);
                                            resultModel = ResultModel.builder().shortEMABarCount(shortEMABarCount).longEMABarCount(longEMABarCount).stoch(stoch).shortMACD(shortMACD).longMACD(longMACD).EMAMACD(EMAMACD).entry(entry).exit(exit).numberOfPositions(tradingRecord.getPositionCount()).result(result).build();
                                            System.out.println(resultModel.toString());
                                            resultModels.add(resultModel);
                                        }
                                    }
                                }
                            }

                            resultModels.sort(Comparator.comparing(ResultModel::getResult).reversed());
                            File file = new File("MM.txt");
                            if (file.exists()) file.delete();
                            file.createNewFile();

                            PrintWriter printWriter = new PrintWriter(file);
                            resultModels.forEach(resultModel1 -> printWriter.println(resultModel1.toString()));

                        }

                    }
                }
            }
        }
    }
}
