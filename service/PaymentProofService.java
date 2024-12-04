package za.co.nharire.order_ms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import za.co.nharire.order_ms.config.email.EmailDetails;
import za.co.nharire.order_ms.config.email.EmailService;
import za.co.nharire.order_ms.model.payment.PaymentDTO;
import za.co.nharire.order_ms.model.payment.PaymentProof;
import za.co.nharire.order_ms.repository.PaymentProofRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j

public class PaymentProofService {

    private final PaymentProofRepository paymentProofRepository;
    private final EmailService emailService;

    public PaymentDTO savePaymentProof(PaymentDTO paymentDTO) {

        PaymentProof paymentProof = new PaymentProof();
        BeanUtils.copyProperties(paymentDTO, paymentProof);

        log.info(" Save PaymentProof to DB");
        PaymentProof paymentProof1 = paymentProofRepository.save(paymentProof);
        EmailDetails emailDetails = new EmailDetails();
        emailDetails.setAttachment(paymentDTO.getFilePath());
        String status = emailService.sendMailWithAttachment(emailDetails);

        BeanUtils.copyProperties(paymentProof1, paymentDTO);
        return paymentDTO;
    }

    public List<PaymentDTO> getALlPayments() {

        log.info("IN SERVICE ");

        List<PaymentDTO> paymentDTOList = new ArrayList<>();

        List<PaymentProof> paymentProofList = paymentProofRepository.findAll();
        for (PaymentProof paymentProof : paymentProofList) {
            PaymentDTO paymentDTO = new PaymentDTO();
            BeanUtils.copyProperties(paymentProof, paymentDTO);
            paymentDTOList.add(paymentDTO);
        }
        return paymentDTOList;
    }

    public PaymentDTO getPaymentById(Integer proofId) {
        log.info("IN SERVICE ");

        PaymentDTO paymentDTO = new PaymentDTO();
        Optional<PaymentProof> paymentProof = paymentProofRepository.findById(proofId);

        if (paymentProof.isEmpty()) {
            return null;
        } else {
            BeanUtils.copyProperties(paymentProof.get(), paymentDTO);
            return paymentDTO;
        }
    }
}
