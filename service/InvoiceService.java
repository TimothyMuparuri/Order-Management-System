package za.co.nharire.order_ms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import za.co.nharire.order_ms.model.invoice.Invoice;
import za.co.nharire.order_ms.model.invoice.InvoiceDTO;
import za.co.nharire.order_ms.repository.InvoiceRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j

public class InvoiceService {

    private  final InvoiceRepository invoiceRepository;

    public InvoiceDTO saveInvoice(InvoiceDTO invoiceDTO) {

        Invoice invoice = new Invoice();
        BeanUtils.copyProperties(invoiceDTO, invoice);

        log.info(" Save Invoice to DB");
        Invoice invoice1 = invoiceRepository.save(invoice);

        BeanUtils.copyProperties(invoice1, invoiceDTO);
        return invoiceDTO;
    }

    public InvoiceDTO getInvoiceById(Integer invoiceId) {
        log.info("IN SERVICE ");

        InvoiceDTO invoiceDTO = new InvoiceDTO();
        Optional<Invoice> invoice = invoiceRepository.findById(invoiceId);

        if (invoice.isEmpty()) {
            return null;
        } else {
            BeanUtils.copyProperties(invoice.get(), invoiceDTO);
            return invoiceDTO;
        }
    }
}
