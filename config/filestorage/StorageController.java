//package za.co.nharire.order_ms.config.filestorage;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@Slf4j
//@RequiredArgsConstructor
//
//public class StorageController {
//
//    private final StorageService storageService;
//
//    public ResponseEntity<StorageResponse> saveFile(File file){
//        return new ResponseEntity<>(storageService.uploadFile(file), HttpStatus.OK)
//
//    }
//}
