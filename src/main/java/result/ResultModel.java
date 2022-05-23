package result;

import org.ta4j.core.num.Num;

public class ResultModel {
    int RSIBarCount;
    int shortSMABarCount;
    int longsMABarCount;
    int numberOfPositions;
    Num result;

    @Override
    public String toString() {
        return "ResultModel{" +
                "RSIBarCount=" + RSIBarCount +
                ", shortSMABarCount=" + shortSMABarCount +
                ", longsMABarCount=" + longsMABarCount +
                ", numberOfPositions=" + numberOfPositions +
                ", result=" + result +
                '}';
    }

    public int getRSIBarCount() {
        return RSIBarCount;
    }

    public ResultModel setRSIBarCount(int RSIBarCount) {
        this.RSIBarCount = RSIBarCount;
        return this;
    }

    public int getShortSMABarCount() {
        return shortSMABarCount;
    }

    public ResultModel setShortSMABarCount(int shortSMABarCount) {
        this.shortSMABarCount = shortSMABarCount;
        return this;
    }

    public int getLongsMABarCount() {
        return longsMABarCount;
    }

    public ResultModel setLongsMABarCount(int longsMABarCount) {
        this.longsMABarCount = longsMABarCount;
        return this;
    }

    public int getNumberOfPositions() {
        return numberOfPositions;
    }

    public ResultModel setNumberOfPositions(int numberOfPositions) {
        this.numberOfPositions = numberOfPositions;
        return this;
    }

    public Num getResult() {
        return result;
    }

    public ResultModel setResult(Num result) {
        this.result = result;
        return this;
    }
}
