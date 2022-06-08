import loaders.CsvTradesLoader;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BarSeriesManager;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;

import org.ta4j.core.analysis.criteria.pnl.GrossReturnCriterion;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;
import result.ResultModel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


/**
 * 2-Period RSI Strategy
 *
 * @see <a href=
 * "http://stockcharts.com/school/doku.php?id=chart_school:trading_strategies:rsi2">
 * http://stockcharts.com/school/doku.php?id=chart_school:trading_strategies:rsi2</a>
 */
public class RSI2Strategy {

    /**
     * @param series a bar series
     * @return a 2-period RSI strategy
     */
    public static Strategy buildStrategy(BarSeries series, int barCountRSI,
                                         int sortSMABarCount,
                                         int longSMABarCount) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        SMAIndicator shortSma = new SMAIndicator(closePrice, sortSMABarCount);
        SMAIndicator longSma = new SMAIndicator(closePrice, longSMABarCount);

        // We use a 2-period RSI indicator to identify buying
        // or selling opportunities within the bigger trend.
        RSIIndicator rsi = new RSIIndicator(closePrice, barCountRSI);

        // Entry rule
        // The long-term trend is up when a security is above its 200-period SMA.
        Rule entryRule = new OverIndicatorRule(shortSma, longSma) // Trend
                .and(new CrossedDownIndicatorRule(rsi, 5)) // Signal 1
                .and(new OverIndicatorRule(shortSma, closePrice)); // Signal 2

        // Exit rule
        // The long-term trend is down when a security is below its 200-period SMA.
        Rule exitRule = new UnderIndicatorRule(shortSma, longSma) // Trend
                .and(new CrossedUpIndicatorRule(rsi, 95)) // Signal 1
                .and(new UnderIndicatorRule(shortSma, closePrice)); // Signal 2

        // TODO: Finalize the strategy

        return new BaseStrategy(entryRule, exitRule);
    }

    public static void main(String[] args) throws IOException {

        // Getting the bar series
        BarSeries series = CsvTradesLoader.loadBitstampSeries();
        List<ResultModel> resultModels = new ArrayList<>();


        ResultModel resultModel = null;
        for (int RSIBarCount = 1; RSIBarCount < 3; RSIBarCount++) {
            System.out.println("RSIBarCount = " + RSIBarCount);
            for (int shortSMABarCount = 1; shortSMABarCount < 300; shortSMABarCount++) {
                System.out.println("shortSMABarCount = " + shortSMABarCount);
                for (int longSMABarCount = 1; longSMABarCount < 300; longSMABarCount++) {

                    // Building the trading strategy
                    Strategy strategy = buildStrategy(series, RSIBarCount,
                            shortSMABarCount, longSMABarCount);

                    // Running the strategy
                    BarSeriesManager seriesManager = new BarSeriesManager(series);
                    TradingRecord tradingRecord = seriesManager.run(strategy);
//                    System.out.println("RSIBarCount = " + RSIBarCount);
//                    System.out.println("shortSMABarCount = " + shortSMABarCount);
//                    System.out.println("longSMABarCount = " + longSMABarCount);
//                    System.out.println("Number of positions for the strategy: " + tradingRecord.getPositionCount());
//
//                    // Analysis
                    Num result = new GrossReturnCriterion().calculate(series
                            , tradingRecord);


                    resultModel =
                            new ResultModel()
                                    .setRSIBarCount(RSIBarCount)
                                    .setLongsMABarCount(longSMABarCount)
                                    .setShortSMABarCount(shortSMABarCount)
                                    .setResult(result);
                    resultModels.add(resultModel);
                    System.out.println("resultModel = " + resultModel.toString());
                }
            }
        }

        resultModels.sort(Comparator.comparing(ResultModel::getResult).reversed());
        File file = new File("RSI2.txt");
        if (file.exists())
            file.delete();
        file.createNewFile();
        PrintWriter printWriter = new PrintWriter(file);
        for (ResultModel model : resultModels) {
            printWriter.println(model.toString());
        }
    }

}
