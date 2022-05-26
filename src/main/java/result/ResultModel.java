package result;

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
    private int adx;
    private int sma;
    private int over;
    private int numberOfPositions;
    private Num result;
}
