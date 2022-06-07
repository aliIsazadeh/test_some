package result;

import jdk.jfr.DataAmount;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.ta4j.core.num.Num;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ResultModel {
    private int shortEMABarCount;
    private int longEMABarCount;
    private int stoch;
    private int shortMACD;
    private int longMACD;
    private int EMAMACD;
    private int entry;
    private int exit;
    private int numberOfPositions;
    private Num result;
}
