package com.rits.mfrservice.service;
import java.time.LocalDateTime;
import java.util.List;

import com.rits.mfrservice.dto.MFRResponse;
import com.rits.mfrservice.dto.MFRResponseList;
import com.rits.mfrservice.Exception.MfrException;
import com.rits.mfrservice.dto.*;
import com.rits.mfrservice.model.Mfr;
import com.rits.mfrservice.repository.MfrRepository;
import com.rits.mfrservice.model.MessageDetails;
import com.rits.mfrservice.model.MessageModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;


@Service
@RequiredArgsConstructor

public class MfrServiceImpl implements MfrService{
    private final MfrRepository repository;



    @Override
    public MessageModel createMfr(MfrRequest mfrRequest) throws Exception {
        int recordPresent = repository.countByMfrNoAndVersionAndActive(mfrRequest.getMfrNo(), mfrRequest.getVersion(), 1);
        if (recordPresent > 0) {
          //  throw new MfrException(8000, mfrRequest.getMfrNo());
            Mfr existingMfr = repository.findByMfrNoAndVersionAndSiteAndActive(mfrRequest.getMfrNo(), mfrRequest.getVersion(), mfrRequest.getSite(), 1);

             Mfr mfr = updateMfr(existingMfr, mfrRequest);
        }

        Mfr mfr = Mfr.builder()
                .site(mfrRequest.getSite())
                .handle("MfrBO:" + mfrRequest.getSite() + "," + mfrRequest.getMfrNo())
                .mfrNo(mfrRequest.getMfrNo())
                .productName(mfrRequest.getProductName())
                .version(mfrRequest.getVersion())
                .headerDetails(mfrRequest.getHeaderDetails())
                .footerDetails(mfrRequest.getFooterDetails())
                .sections(mfrRequest.getSections())
                .createdBy(mfrRequest.getCreatedBy())
                .modifiedBy(mfrRequest.getModifiedBy())
                .active(1)
                .createdDateTime(LocalDateTime.now())
                .modifiedDateTime(LocalDateTime.now())
                .build();


//&& !repository.findBySiteAndProcedureNameAndActiveEquals(mfrRequest.getSite(), mfrRequest.getProcedureName(), 1)
        if (mfr.getMfrNo() != "" ) {
            if(recordPresent == 0)
                return MessageModel.builder().message_details(new MessageDetails(mfrRequest.getMfrNo() + " with version " + mfr.getVersion() + " Created SuccessFully", "S")).response(repository.save(mfr)).build();
            else
                return MessageModel.builder().message_details(new MessageDetails(mfrRequest.getMfrNo() +  " with version " + mfr.getVersion() + " Updated SuccessFully", "S")).response(repository.save(mfr)).build();
        } else {
            throw new MfrException(8000, mfrRequest.getMfrNo());
        }
    }



    private Mfr updateMfr(Mfr existingMfr, MfrRequest mfrRequest) {

        return Mfr.builder()
                .footerDetails(mfrRequest.getFooterDetails())
                .sections(mfrRequest.getSections())
                .modifiedDateTime(LocalDateTime.now())
                .build();
    }

    @Override
    public Boolean isMfrExist(MfrRequest mfrRequest) {
        return repository.existsByMfrNoAndVersionAndSiteAndActive(mfrRequest.getMfrNo(),mfrRequest.getVersion(), mfrRequest.getSite() ,1);
    }




    @Override
    public Mfr retrieveMfr(MfrRequest mfrRequest) throws Exception{
        Mfr mfr =repository.findByActiveAndMfrNoAndVersion(  1,mfrRequest.getMfrNo(), mfrRequest.getVersion());
        if (mfr != null) {
            return mfr;
        } else {
            throw new MfrException(8002, mfrRequest.getMfrNo());
        }
    }




    @Override
    public MFRResponseList getMfrListByCreationDate(String site)  {
        List<MFRResponse> mfrResponseList = repository.findTop50ByActiveAndSiteOrderByCreatedDateTimeDesc(1, site);
        return MFRResponseList.builder().mfrList(mfrResponseList).build();
    }



}
