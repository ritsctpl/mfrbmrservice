package com.rits.mfrscreenconfigurationservice.service;

import com.rits.activitygroupservice.dto.ActivityGroupRequest;
import com.rits.activitygroupservice.dto.ActivityGroupResponse;
import com.rits.activitygroupservice.dto.ActivityGroupResponseList;
import com.rits.activitygroupservice.exception.ActivityGroupException;
import com.rits.mfrscreenconfigurationservice.dto.ProductResponse;
import com.rits.mfrscreenconfigurationservice.dto.ProductResponseList;
import com.rits.mfrscreenconfigurationservice.model.MessageModel;
import com.rits.mfrscreenconfigurationservice.dto.MfrScreenConfigurationRequest;
import com.rits.mfrscreenconfigurationservice.model.MFRScreenConfiguration;
import com.rits.mfrscreenconfigurationservice.repository.MFRScreenConfigurationRepository;
import com.rits.mfrscreenconfigurationservice.exception.MFRScreenConfigurationException;
import com.rits.mfrscreenconfigurationservice.model.MessageDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MFRScreenConfigurationServiceImpl implements MFRScreenConfigurationService{
    private final MFRScreenConfigurationRepository repository;
    @Override
    public MessageModel createMfrScreenConfiguration(MfrScreenConfigurationRequest mfrScreenConfigurationRequest) {
        int recordPresent = repository.countByProductNameAndActive(mfrScreenConfigurationRequest.getProductName(), 1);
        if (recordPresent > 0) {
             // throw new MFRScreenConfigurationException(8000, mfrScreenConfigurationRequest.getName());
            MFRScreenConfiguration existingProduct = repository.findByProductNameAndSiteAndActive(mfrScreenConfigurationRequest.getProductName(),  mfrScreenConfigurationRequest.getSite(), 1);

            MFRScreenConfiguration mfrScreenConfiguration = updateMfrScreenConfiguration(existingProduct, mfrScreenConfigurationRequest);
        }
        if(mfrScreenConfigurationRequest.getDescription() == null || mfrScreenConfigurationRequest.getDescription().isEmpty() ){
            mfrScreenConfigurationRequest.setDescription(mfrScreenConfigurationRequest.getProductName());
        }
        MFRScreenConfiguration mfrScreenConfiguration = MFRScreenConfiguration.builder()
                .site(mfrScreenConfigurationRequest.getSite())
                .handle("Product:" + mfrScreenConfigurationRequest.getSite() + "," + mfrScreenConfigurationRequest.getProductName())
                .product(mfrScreenConfigurationRequest.getProduct())
                .productName(mfrScreenConfigurationRequest.getProductName())
                .description(mfrScreenConfigurationRequest.getDescription())
                .configType(mfrScreenConfigurationRequest.getConfigType())
                .defaultMfr(mfrScreenConfigurationRequest.getDefaultMfr())
                .version(mfrScreenConfigurationRequest.getVersion())
                .mrfRefList(mfrScreenConfigurationRequest.getMrfRefList())
                .createdBy(mfrScreenConfigurationRequest.getCreatedBy())
                .modifiedBy(mfrScreenConfigurationRequest.getModifiedBy())
                .active(1)
                .createdDateTime(LocalDateTime.now())
                .modifiedDateTime(LocalDateTime.now())
                .build();


//&& !repository.findBySiteAndProcedureNameAndActiveEquals(mfrScreenConfigurationRequest.getSite(), mfrScreenConfigurationRequest.getProcedureName(), 1)
        if (mfrScreenConfiguration.getProductName() != "" ) {
            if(recordPresent == 0)
                return com.rits.mfrscreenconfigurationservice.model.MessageModel.builder().message_details(new MessageDetails(mfrScreenConfigurationRequest.getProductName()  +  " Created SuccessFully", "S")).response(repository.save(mfrScreenConfiguration)).build();
            else
                return com.rits.mfrscreenconfigurationservice.model.MessageModel.builder().message_details(new MessageDetails(mfrScreenConfigurationRequest.getProductName()  + " Updated SuccessFully", "S")).response(repository.save(mfrScreenConfiguration)).build();
        } else {
            throw new MFRScreenConfigurationException(9002, mfrScreenConfigurationRequest.getProductName());
        }
    }

    @Override
    public MFRScreenConfiguration retrieveProduct(MfrScreenConfigurationRequest mfrScreenConfigurationRequest) {
        MFRScreenConfiguration mfrScreenConfiguration =repository.findByActiveAndProductName(  1,mfrScreenConfigurationRequest.getProductName());
        if (mfrScreenConfiguration != null) {
            return mfrScreenConfiguration;
        } else {
            throw new MFRScreenConfigurationException(9003, mfrScreenConfigurationRequest.getProductName());
        }
    }

    private MFRScreenConfiguration updateMfrScreenConfiguration(MFRScreenConfiguration existingMfr, MfrScreenConfigurationRequest mfrScreenConfigurationRequest) {

        return MFRScreenConfiguration.builder()
                .product(mfrScreenConfigurationRequest.getProduct())
                .description(mfrScreenConfigurationRequest.getDescription())

                .configType(mfrScreenConfigurationRequest.getConfigType())
                .defaultMfr(mfrScreenConfigurationRequest.getDefaultMfr())
                .version(mfrScreenConfigurationRequest.getVersion())
                .mrfRefList(mfrScreenConfigurationRequest.getMrfRefList())

                .modifiedBy((mfrScreenConfigurationRequest.getModifiedBy()))
                .createdBy(mfrScreenConfigurationRequest.getCreatedBy())
                .createdDateTime(LocalDateTime.now())
                .modifiedDateTime(LocalDateTime.now())
                .build();
    }


    @Override
    public MessageModel deleteProductName(MfrScreenConfigurationRequest mfrScreenConfigurationRequest) {
        int count = repository.countByProductNameAndActive(mfrScreenConfigurationRequest.getProductName(), 1);
        if (count > 0) {
            MFRScreenConfiguration existingProduct = repository.findByActiveAndProductName(  1,mfrScreenConfigurationRequest.getProductName());
            existingProduct.setActive(0);
            existingProduct.setModifiedDateTime(LocalDateTime.now());
            existingProduct.setModifiedBy(mfrScreenConfigurationRequest.getModifiedBy());

            repository.save(existingProduct);
            return MessageModel.builder().message_details(new com.rits.mfrscreenconfigurationservice.model.MessageDetails(existingProduct.getProductName() + " is deleted successfully" ,"S")).build();
        } else {
            throw new MFRScreenConfigurationException(9003, mfrScreenConfigurationRequest.getProductName());

        }
    }

    @Override
    public ProductResponseList getProductListListByCreationDate(MfrScreenConfigurationRequest mfrScreenConfigurationRequest) {
        List<ProductResponse> productResponse = repository.findTop50ByActiveOrderByCreatedDateTimeDesc(1);
        return ProductResponseList.builder().productList(productResponse).build();
    }


}
