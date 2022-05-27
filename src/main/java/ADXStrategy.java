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
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.adx.ADXIndicator;
import org.ta4j.core.indicators.adx.MinusDIIndicator;
import org.ta4j.core.indicators.adx.PlusDIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;
import result.ResultModel;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


/**
 * ADX indicator based strategy
 *
 * @see <a href=
 * "http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:average_directional_index_adx">
 * http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:average_directional_index_adx</a>
 */
public class ADXStrategy {

    /**
     * @param series a bar series
     * @return an adx indicator based strategy
     */
    public static Strategy buildStrategy(BarSeries series, int sma, int adx, int over) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        final ClosePriceIndicator closePriceIndicator = new ClosePriceIndicator(series);
        final SMAIndicator smaIndicator = new SMAIndicator(closePriceIndicator, sma);

        final ADXIndicator adxIndicator = new ADXIndicator(series, adx);
        final OverIndicatorRule adxOver20Rule = new OverIndicatorRule(adxIndicator, over);

        final PlusDIIndicator plusDIIndicator = new PlusDIIndicator(series, adx);
        final MinusDIIndicator minusDIIndicator = new MinusDIIndicator(series, adx);

        final Rule plusDICrossedUpMinusDI = new CrossedUpIndicatorRule(plusDIIndicator, minusDIIndicator);
        final Rule plusDICrossedDownMinusDI = new CrossedDownIndicatorRule(plusDIIndicator, minusDIIndicator);
        final OverIndicatorRule closePriceOverSma = new OverIndicatorRule(closePriceIndicator, smaIndicator);
        final Rule entryRule = adxOver20Rule.and(plusDICrossedUpMinusDI).and(closePriceOverSma);

        final UnderIndicatorRule closePriceUnderSma = new UnderIndicatorRule(closePriceIndicator, smaIndicator);
        final Rule exitRule = adxOver20Rule.and(plusDICrossedDownMinusDI).and(closePriceUnderSma);

        return new BaseStrategy("ADX", entryRule, exitRule, adx);
    }


    // each test time is 35 ms
    public static void main(String[] args) {
        // Getting the bar series
        BarSeries series = CsvTradesLoader.loadBitstampSeries();
        Date start = new Date();
        test(
                new int[]{100, 150},
                new int[]{1, 10},
                new int[]{1, 10},
                series);
        Date finish = new Date();
        System.out.println("start = " + start);
        System.out.println("finish = " + finish);
    }

    private static void test(int[] rangeADX, int[] rangeSMA, int[] rangeOver, BarSeries series) {
        long lt = 0;
        long count = 0;
        List<ResultModel> resultModels = new ArrayList<>();
        ResultModel resultModel = null;


        for (int adx = rangeADX[0]; adx < rangeADX[1]; adx++) {
            for (int sma = rangeSMA[0]; sma < rangeSMA[1]; sma++) {
                for (int over = rangeOver[0]; over < rangeOver[1]; over++) {
                    // Building the trading strategy
                    long l = System.currentTimeMillis();
                    Strategy strategy = buildStrategy(series, sma, adx, over);

                    // Running the strategy
                    BarSeriesManager seriesManager = new BarSeriesManager(series);
                    TradingRecord tradingRecord = seriesManager.run(strategy);
                    int positionCount = tradingRecord.getPositionCount();

                    Num result = new GrossReturnCriterion().calculate(series, tradingRecord);

                    long l2 = System.currentTimeMillis();
                    lt += (l2 - l);
                    count++;
                    resultModel = ResultModel.builder().result(result).adx(adx).sma(sma).over(over).numberOfPositions(positionCount).build();
                    System.out.println(resultModel.toString());
                    resultModels.add(resultModel);
                }
            }
        }

        System.out.println("lt = " + lt);
        System.out.println("count = " + count);
        System.out.println("average = " + lt / count);
        System.out.println("sorting results");
        resultModels.sort(Comparator.comparing(ResultModel::getResult).reversed());
        System.out.println("write to file");
        try {
            Date now = new Date();
            File file = new File("ADXResults-"+rangeADX[0]+"-"+rangeADX[1]+
                    ".txt");
            if (file.exists())
                file.delete();
            file.createNewFile();
            PrintWriter printWriter = new PrintWriter(file);
            resultModels.forEach(resultModel1 -> {
                printWriter.println(resultModel1.toString());
                printWriter.flush();
            });
            printWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
