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
import org.ta4j.core.indicators.CCIIndicator;
import org.ta4j.core.num.Num;
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
 * CCI Correction Strategy
 *
 * @see <a href=
 * "http://stockcharts.com/school/doku.php?id=chart_school:trading_strategies:cci_correction">
 * http://stockcharts.com/school/doku.php?id=chart_school:trading_strategies:cci_correction</a>
 */
public class CCICorrectionStrategy {

    /**
     * @param series a bar series
     * @return a CCI correction strategy
     */
    public static Strategy buildStrategy(BarSeries series, int longCCi,
                                         int shortCCi, int plus, int minus) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        CCIIndicator longCci = new CCIIndicator(series, longCCi);
        CCIIndicator shortCci = new CCIIndicator(series, shortCCi);
        Num plus100 = series.numOf(plus);
        Num minus100 = series.numOf(minus);

        Rule entryRule = new OverIndicatorRule(longCci, plus100) // Bull trend
                .and(new UnderIndicatorRule(shortCci, minus100)); // Signal

        Rule exitRule = new UnderIndicatorRule(longCci, minus100) // Bear trend
                .and(new OverIndicatorRule(shortCci, plus100)); // Signal

        Strategy strategy = new BaseStrategy(entryRule, exitRule);
        strategy.setUnstablePeriod(5);
        return strategy;
    }

    public static void main(String[] args) throws IOException {

        // Getting the bar series
        BarSeries series = CsvTradesLoader.loadBitstampSeries();
        List<ResultModel> list = new ArrayList<>();
        ResultModel resultModel = null;
        for (int longCCI = 100; longCCI <= 200; longCCI+=5) {
            for (int shortCCI = 5; shortCCI <= 25; shortCCI+=5) {
                for (int plus = 100; plus <= 150; plus+=5) {
                    for (int minus = -150; minus <= (-100); minus+=5) {
                            Strategy strategy = buildStrategy(series, longCCI,
                                    shortCCI, plus, minus);
                            // Building the trading strategy

                            // Running the strategy
                            BarSeriesManager seriesManager = new BarSeriesManager(series);
                            TradingRecord tradingRecord = seriesManager.run(strategy);
                            int positionCount =
                                    tradingRecord.getPositionCount();

                            // Analysis
                            Num result = new GrossReturnCriterion().calculate(series
                                    , tradingRecord);
                            resultModel = ResultModel.builder()
                                    .longCCI(longCCI)
                                    .shortCCI(shortCCI)
                                    .plus(plus)
                                    .minus(minus)
                                    .numberOfPositions(positionCount)
                                    .result(result)
                                    .build();
                            System.out.println(resultModel.toString());
                            list.add(resultModel);


                    }
                }
            }
        }

        list.sort(Comparator.comparing(ResultModel::getResult).reversed());

        File file = new File("CCI.txt");
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
