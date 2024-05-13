package practice.virtualcurrency.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpbitCoinDTO {
    private String code;
    @JsonProperty("trade_price")
    private Double tradePrice;
    @JsonProperty("prev_closing_price")
    private Double prevClosingPrice;

    private String name;
    private Long price;
}
