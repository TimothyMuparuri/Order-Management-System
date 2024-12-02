package za.co.nharire.order_ms.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.co.nharire.order_ms.model.payment.PaymentDTO;
import za.co.nharire.order_ms.service.PaymentProofService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j

public class PaymentProofController {

    private final PaymentProofService paymentProofService;

    @PostMapping("payment/save")
    public ResponseEntity<PaymentDTO> savePaymentProof(@RequestBody PaymentDTO paymentDTO) {

        log.info("SAVING PAYMENT", paymentDTO.toString());

        PaymentDTO paymentDTO1 = paymentProofService.savePaymentProof(paymentDTO);
        if (paymentDTO1 != null) {
            return new ResponseEntity<>(paymentDTO1, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/payment/all")
    public ResponseEntity<List<PaymentDTO>> getAllPayments() {
        log.info("GETTING ALL PAYMENTS IN DB ");

        List<PaymentDTO> paymentDTOList = paymentProofService.getALlPayments();

        if (paymentDTOList != null) {
            return new ResponseEntity<>(paymentDTOList, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/payment/get/{proofId}")
    public ResponseEntity<PaymentDTO> getPaymentById(@PathVariable Integer proofId) {
        log.info("GETTING A PAYMENT IN DB ");

        PaymentDTO paymentDTO = paymentProofService.getPaymentById(proofId);

        if (paymentDTO != null) {
            return new ResponseEntity<>(paymentDTO, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
