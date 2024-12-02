package za.co.nharire.order_ms.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.co.nharire.order_ms.model.invoice.InvoiceDTO;
import za.co.nharire.order_ms.service.InvoiceService;

@RestController
@RequiredArgsConstructor
@Slf4j

public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping("invoice/save")
    public ResponseEntity<InvoiceDTO> saveInvoice(@RequestBody InvoiceDTO invoiceDTO) {

        log.info("SAVING INVOICE", invoiceDTO.toString());

        InvoiceDTO invoiceDTO1 = invoiceService.saveInvoice(invoiceDTO);
        if (invoiceDTO1 != null) {
            return new ResponseEntity<>(invoiceDTO1, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/invoice/get/{invoiceId}")
    public ResponseEntity<InvoiceDTO> getInvoiceById(@PathVariable Integer invoiceId) {
        log.info("GETTING A Invoice IN DB ");

        InvoiceDTO invoiceDTO = invoiceService.getInvoiceById(invoiceId);

        if (invoiceDTO != null) {
            return new ResponseEntity<>(invoiceDTO, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
