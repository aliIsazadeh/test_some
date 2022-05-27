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
    private float up;
    private float down;
    private int numberOfPositions;
    private Num result;


}
