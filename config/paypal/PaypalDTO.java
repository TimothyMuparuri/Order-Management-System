package za.co.nharire.order_ms.config.paypal;

import lombok.Data;

import java.io.Serializable;

@Data
public class PaypalDTO implements Serializable {

    private String method;
    private double amount;
    private String currency;
    private String description;
}
