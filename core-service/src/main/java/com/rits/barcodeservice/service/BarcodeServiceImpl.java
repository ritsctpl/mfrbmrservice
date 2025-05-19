package com.rits.barcodeservice.service;

import com.rits.barcodeservice.dto.*;
import com.rits.barcodeservice.exception.BarCodeException;
import com.rits.barcodeservice.model.MessageModel;
import com.rits.barcodeservice.model.Barcode;
import com.rits.barcodeservice.model.ListDetails;
import com.rits.barcodeservice.model.MessageDetails;
import com.rits.barcodeservice.repository.BarcodeRepository;

import com.rits.barcodeservice.dto.Extension;

import com.rits.barcodeservice.dto.BarcodeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class BarcodeServiceImpl implements BarcodeService{
    private final BarcodeRepository barcodeRepository;
    private final WebClient.Builder webClientBuilder;
    @Value("${extension-service.url}/addExtension")
    private String extensionUrl;

//    @Value("${auditlog-service.url}/producer")
//    private String auditlogUrl;
   /* @Override
    public Barcode retrieveBarcode(String code, String site) throws Exception {
        if (BarcodeRepository.existsByActiveAndSiteAndDataField(1, site, dataField)) {
            return BarcodeRepository.findByActiveAndSiteAndDataField(1, site, dataField);
        } else {
            throw new DataFieldException(402, dataField);
        }

    }*/


    @Override
    public String callExtension(Extension extension) throws Exception {
        String extensionResponse = webClientBuilder.build()
                .post()
                .uri(extensionUrl)
                .bodyValue(extension)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        if (extensionResponse == null) {
            throw new BarCodeException(800);
        }
        return extensionResponse;
    }




    @Override
    public MessageModel createBarcode(BarcodeRequest barcodeRequest) throws Exception {
        if (barcodeRepository.existsByActiveAndSiteAndCode(1, barcodeRequest.getSite(), barcodeRequest.getCode())) {
            throw new BarCodeException(5001, barcodeRequest.getCode());
        } else {
            for(ListDetails codeList : barcodeRequest.getCodeList()) {
                if (codeList.getDescription() == null || codeList.getDescription().isEmpty()) {
                    codeList.setDescription(codeList.getDataField());
                }
            }

            Barcode code = Barcode.builder()
                    .handle("BarcodeBo:" + barcodeRequest.getSite() + "," + barcodeRequest.getCode())
                    .site(barcodeRequest.getSite())
                    .code(barcodeRequest.getCode())
                    .codeList(barcodeRequest.getCodeList())
                    .createdBy(barcodeRequest.getUserId())
                    .active(1)
                    .createdDateTime(LocalDateTime.now())
                    .build();

//            AuditLogRequest activityLog = AuditLogRequest.builder()
//                    .site(barcodeRequest.getSite())
//                    .change_stamp("Create")
//                    .action_code("BARCODE-CREATE")
//                    .action_detail("Barcode Created "+barcodeRequest.getCode())
//                    .action_detail_handle("ActionDetailBO:"+barcodeRequest.getSite()+","+"BARCODE-CREATE"+","+barcodeRequest.getUserId()+":"+"com.rits.barcodeservice.service")
//                    .date_time(String.valueOf(LocalDateTime.now()))
//                    .userId(barcodeRequest.getUserId())
//                    .txnId("BARCODE-CREATE"+String.valueOf(LocalDateTime.now())+barcodeRequest.getUserId())
//                    .created_date_time(String.valueOf(LocalDateTime.now()))
//                    .build();
//
//            webClientBuilder.build()
//                    .post()
//                    .uri(auditlogUrl)
//                    .bodyValue(activityLog)
//                    .retrieve()
//                    .bodyToMono(AuditLogRequest.class)
//                    .block();

            return MessageModel.builder().message_details(new MessageDetails(barcodeRequest.getCode()+" Created SuccessFully","S")).response(barcodeRepository.save(code)).build();
        }
    }



    @Override
    public MessageModel updateBarcode(BarcodeRequest barcodeRequest) throws Exception {
        boolean result =barcodeRepository.existsByActiveAndSiteAndCode(1, barcodeRequest.getSite(), barcodeRequest.getCode());
        if (result) {
            for(ListDetails codeList : barcodeRequest.getCodeList())
            {
                if (codeList.getDescription() == null || codeList.getDescription().isEmpty()) {
                    codeList.setDescription(codeList.getDataField());
                }
            }
            Barcode existingCode = barcodeRepository.findByActiveAndSiteAndCode(1, barcodeRequest.getSite(), barcodeRequest.getCode());
            Barcode updatedCode = Barcode.builder()
                    .handle(existingCode.getHandle())
                    .code(barcodeRequest.getCode())
                    .codeList(barcodeRequest.getCodeList())
                    .site(barcodeRequest.getSite())
                    .modifiedBy(barcodeRequest.getUserId())
                    .active(1)
                    .modifiedDateTime(LocalDateTime.now())
                    .build();

//            AuditLogRequest activityLog = AuditLogRequest.builder()
//                    .site(barcodeRequest.getSite())
//                    .change_stamp("Update")
//                    .action_code("BARCODE-UPDATE")
//                    .action_detail("Barcode Updated "+barcodeRequest.getCode())
//                    .action_detail_handle("ActionDetailBO:"+barcodeRequest.getSite()+","+"BARCODE-UPDATE"+","+barcodeRequest.getUserId()+":"+"com.rits.barcodeservice.service")
//                    .date_time(String.valueOf(LocalDateTime.now()))
//                    .userId(barcodeRequest.getUserId())
//                    .txnId("BARCODE-UPDATE"+String.valueOf(LocalDateTime.now())+barcodeRequest.getUserId())
//                    .created_date_time(String.valueOf(LocalDateTime.now()))
//                    .build();
//
//            webClientBuilder.build()
//                    .post()
//                    .uri(auditlogUrl)
//                    .bodyValue(activityLog)
//                    .retrieve()
//                    .bodyToMono(AuditLogRequest.class)
//                    .block();

            return MessageModel.builder().message_details(new MessageDetails(barcodeRequest.getCode()+" updated SuccessFully","S")).response(barcodeRepository.save(updatedCode)).build();

        } else {
            throw new BarCodeException(5000, barcodeRequest.getCode());
        }
    }




    @Override
    public MessageModel deleteCode(String code, String site,String userId) throws Exception {

        if (barcodeRepository.existsByActiveAndSiteAndCode(1, site, code)) {
            Barcode existingBarcode = barcodeRepository.findByActiveAndSiteAndCode(1, site, code);
            existingBarcode.setActive(0);
            existingBarcode.setModifiedDateTime(LocalDateTime.now());
            existingBarcode.setModifiedBy(userId);
            barcodeRepository.save(existingBarcode);

//            AuditLogRequest activityLog = AuditLogRequest.builder()
//                    .site(site)
//                    .change_stamp("Delete")
//                    .action_code("BARCODE-DELETE")
//                    .action_detail("Barcode Delete "+code)
//                    .action_detail_handle("ActionDetailBO:"+site+","+"BARCODE-DELETE"+","+userId+":"+"com.rits.barcodeservice.service")
//                    .date_time(String.valueOf(LocalDateTime.now()))
//                    .userId(userId)
//                    .txnId("BARCODE-DELETE"+String.valueOf(LocalDateTime.now())+userId)
//                    .created_date_time(String.valueOf(LocalDateTime.now()))
//                    .build();
//
//            webClientBuilder.build()
//                    .post()
//                    .uri(auditlogUrl)
//                    .bodyValue(activityLog)
//                    .retrieve()
//                    .bodyToMono(AuditLogRequest.class)
//                    .block();
            return MessageModel.builder().message_details(new MessageDetails(code+" deleted SuccessFully","S")).build();

        } else {
            throw new BarCodeException(5000, code);
        }
    }

    @Override
    public Barcode retrieveCode(String code, String site) throws Exception {
        if (barcodeRepository.existsByActiveAndSiteAndCode(1, site, code)) {
            return barcodeRepository.findByActiveAndSiteAndCode(1, site, code);
        } else {
            throw new BarCodeException(5000, code);
        }

    }

    @Override
    public BarcodeAllCodeList getAllCode(String site) throws Exception {
        BarcodeAllCodeList barcodeAllCodeResponse;
        List<BarcodeAllCodeResponse> barcodeResponseLists=barcodeRepository.findByActiveAndSite(1,site);
        return BarcodeAllCodeList.builder().barcodeAllCodeResponses(barcodeResponseLists).build();
    }


    @Override
    public BarcodeResponse getCodeList(String code, String site) {
            Barcode barcode = barcodeRepository.findByActiveAndSiteAndCode(1, site, code);

            if (barcode == null) {
                throw new BarCodeException(5000, code);
            }
        List<ListDetails> listDetailsList = new ArrayList<>();
            for(ListDetails listRecord : barcode.getCodeList())
            {
                listDetailsList.add(listRecord);
            }
            return BarcodeResponse.builder().codeList(listDetailsList).build();
    }

}
