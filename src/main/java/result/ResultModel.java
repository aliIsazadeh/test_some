package result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.ta4j.core.num.Num;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ResultModel {
    private int longCCI;
    private int shortCCI;
    private int plus;
    private int minus;
    private int numberOfPositions;
    private Num result;

}
