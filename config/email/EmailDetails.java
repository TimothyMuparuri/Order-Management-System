package za.co.nharire.order_ms.config.email;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

@Data
@RequiredArgsConstructor
public class EmailDetails implements Serializable {

    private String recipient;
    private String msgBody;
    private String subject;
    private String attachment;
}

