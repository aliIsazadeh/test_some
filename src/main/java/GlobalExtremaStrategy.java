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
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.HighPriceIndicator;
import org.ta4j.core.indicators.helpers.HighestValueIndicator;
import org.ta4j.core.indicators.helpers.LowPriceIndicator;
import org.ta4j.core.indicators.helpers.LowestValueIndicator;
import org.ta4j.core.indicators.helpers.TransformIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;
import result.ResultModel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


/**
 * Strategies which compares current price to global extrema over a week.
 */
public class GlobalExtremaStrategy {

    // We assume that there were at least one position every 5 minutes during the
    // whole
    // week
    private static final int NB_BARS_PER_WEEK = 12 * 24 * 7;

    /**
     * @param series the bar series
     * @return the global extrema strategy
     */
    public static Strategy buildStrategy(BarSeries series, float up, float down) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        ClosePriceIndicator closePrices = new ClosePriceIndicator(series);

        // Getting the high price over the past week
        HighPriceIndicator highPrices = new HighPriceIndicator(series);
        HighestValueIndicator weekHighPrice = new HighestValueIndicator(highPrices, NB_BARS_PER_WEEK);
        // Getting the low price over the past week
        LowPriceIndicator lowPrices = new LowPriceIndicator(series);
        LowestValueIndicator weekLowPrice = new LowestValueIndicator(lowPrices, NB_BARS_PER_WEEK);

        // Going long if the close price goes below the low price
        TransformIndicator downWeek =
                TransformIndicator.multiply(weekLowPrice, down);
        Rule buyingRule = new UnderIndicatorRule(closePrices, downWeek);

        // Going short if the close price goes above the high price
        TransformIndicator upWeek = TransformIndicator.multiply(weekHighPrice
                , up);
        Rule sellingRule = new OverIndicatorRule(closePrices, upWeek);

        return new BaseStrategy(buyingRule, sellingRule);
    }

    public static void main(String[] args) throws IOException {

        // Getting the bar series
        List<ResultModel> list = new ArrayList<>();
        ResultModel resultModel = null;
        BarSeries series = CsvTradesLoader.loadBitstampSeries();
        for (float up = 0; up <= 2; up += 0.01) {
            for (float down = 3; down > 1; down -= 0.01) {
                // Building the trading strategy
                Strategy strategy = buildStrategy(series, up, down);

                // Running the strategy
                BarSeriesManager seriesManager = new BarSeriesManager(series);
                TradingRecord tradingRecord = seriesManager.run(strategy);
                int count = tradingRecord.getPositionCount();

                // Analysis
                Num result = new GrossReturnCriterion().calculate(series,
                        tradingRecord);
                resultModel = ResultModel.builder()
                        .up(up)
                        .down(down)
                        .numberOfPositions(count)
                        .result(result)
                        .build();
                list.add(resultModel);
                System.out.println(resultModel.toString());
            }
        }
        list.sort(Comparator.comparing(ResultModel::getResult).reversed());
        File file = new File("Global.txt");
        if (file.exists())
            file.delete();
        file.createNewFile();
        PrintWriter printWriter = new PrintWriter(file);

        for (ResultModel model : list) {
            printWriter.println(model.toString());
            printWriter.flush();
        }
        printWriter.close();
    }
}
