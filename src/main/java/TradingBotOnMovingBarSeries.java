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

import java.io.FileNotFoundException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;

import loaders.CsvBarsLoader;
import loaders.CsvTradesLoader;
import org.ta4j.core.*;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.ichimoku.IchimokuKijunSenIndicator;
import org.ta4j.core.indicators.ichimoku.IchimokuTenkanSenIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;


/**
 * This class is an example of a dummy trading bot using ta4j.
 * <p/>
 */
public class TradingBotOnMovingBarSeries {

    /**
     * Close price of the last bar
     */
    private static Num LAST_BAR_CLOSE_PRICE;

    /**
     * Builds a moving bar series (i.e. keeping only the maxBarCount last bars)
     *
     * @param maxBarCount the number of bars to keep in the bar series (at maximum)
     * @return a moving bar series
     */
    private static BarSeries initMovingBarSeries(int maxBarCount) throws FileNotFoundException {
        BarSeries series = CsvBarsLoader.loadXAUData();
        System.out.print("Initial bar count: " + series.getBarCount());
        // Limitating the number of bars to maxBarCount
        series.setMaximumBarCount(maxBarCount);
        LAST_BAR_CLOSE_PRICE = series.getBar(series.getEndIndex()).getClosePrice();
        System.out.println(" (limited to " + maxBarCount + "), close price = " + LAST_BAR_CLOSE_PRICE);
        return series;
    }

    private static BarSeries getAllBarSeries() throws FileNotFoundException {
        return CsvBarsLoader.loadXAUData();
    }

    /**
     * @param series a bar series
     * @return a dummy strategy
     */
    private static Strategy buildStrategy(BarSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        IchimokuTenkanSenIndicator ichimokuTenkanSenIndicator =
                new IchimokuTenkanSenIndicator(series,9);
        IchimokuKijunSenIndicator ichimokuKijunSenIndicator =
                new IchimokuKijunSenIndicator(series,26);

        Rule entryRule = new CrossedUpIndicatorRule(ichimokuKijunSenIndicator
                ,ichimokuTenkanSenIndicator);
        Rule exitRule = new CrossedUpIndicatorRule(ichimokuTenkanSenIndicator,ichimokuKijunSenIndicator);

        // Signals
        // Buy when SMA goes over close price
        // Sell when close price goes over SMA
        return new BaseStrategy(entryRule,exitRule);
    }

    /**
     * Generates a random decimal number between min and max.
     *
     * @param min the minimum bound
     * @param max the maximum bound
     * @return a random decimal number between min and max
     */
    private static Num randDecimal(Num min, Num max) {
        Num randomDecimal = null;
        if (min != null && max != null && min.isLessThan(max)) {
            Num range = max.minus(min);
            Num position = range.multipliedBy(DecimalNum.valueOf(Math.random()));
            randomDecimal = min.plus(position);
        }
        return randomDecimal;
    }

    /**
     * Generates a random bar.
     *
     * @return a random bar
     */
    private static Bar generateRandomBar() {
        final Num maxRange = DecimalNum.valueOf("0.03"); // 3.0%
        Num openPrice = LAST_BAR_CLOSE_PRICE;
        Num lowPrice = openPrice.minus(maxRange.multipliedBy(DecimalNum.valueOf(Math.random())));
        Num highPrice = openPrice.plus(maxRange.multipliedBy(DecimalNum.valueOf(Math.random())));
        Num closePrice = randDecimal(lowPrice, highPrice);
        LAST_BAR_CLOSE_PRICE = closePrice;
        return new BaseBar(Duration.ofDays(1), ZonedDateTime.now(), openPrice, highPrice, lowPrice, closePrice,
                DecimalNum.valueOf(1), DecimalNum.valueOf(1));
    }

    private static BarSeries cutFromTo(BarSeries barSeries ,int from, int to){
        List<Bar> barList = barSeries.getBarData();
        BarSeries result = new BaseBarSeries(barSeries.getName());
        for (int i = from; i <to ; i++) {
            result.addBar(barList.get(i));
        }
        return result;

    }

    public static void main(String[] args) throws InterruptedException, FileNotFoundException {

        List<Bar> barList;
        System.out.println("********************** Initialization **********************");
        // Getting the bar series
        BarSeries series = getAllBarSeries();
        barList = series.getBarData();
        series = cutFromTo(series,0,26);

        // Building the trading strategy
        Strategy strategy = buildStrategy(series);

        // Initializing the trading history
        TradingRecord tradingRecord = new BaseTradingRecord();
        System.out.println("************************************************************");

        /*
         * We run the strategy for the 50 next bars.
         */
        for (int i = 26; i < 395; i++) {

            // New bar
            Thread.sleep(300); // I know...
            Bar newBar = barList.get(i);
            System.out.println("------------------------------------------------------\n" + "Bar " + i
                    + " added, close price = " + newBar.getClosePrice().doubleValue());
            series.addBar(newBar);

            int endIndex = series.getEndIndex();
            if (strategy.shouldEnter(endIndex)) {
                // Our strategy should enter
                System.out.println("Strategy should ENTER on " + endIndex);
                boolean entered = tradingRecord.enter(endIndex, newBar.getClosePrice(), DecimalNum.valueOf(10));
                if (entered) {
                    Trade entry = tradingRecord.getLastEntry();
                    System.out.println("Entered on " + entry.getIndex() + " (price=" + entry.getNetPrice().doubleValue()
                            + ", amount=" + entry.getAmount().doubleValue() + ")");
                    System.out.println("Bar: "+series.getLastBar());
                }
            } else if (strategy.shouldExit(endIndex)) {
                // Our strategy should exit
                System.out.println("Strategy should EXIT on " + endIndex);
                boolean exited = tradingRecord.exit(endIndex, newBar.getClosePrice(), DecimalNum.valueOf(10));
                if (exited) {
                    Trade exit = tradingRecord.getLastExit();
                    System.out.println("Exited on " + exit.getIndex() + " (price=" + exit.getNetPrice().doubleValue()
                            + ", amount=" + exit.getAmount().doubleValue() + ")");
                    System.out.println("Bar: "+series.getLastBar());
                }
            }
        }
    }
}