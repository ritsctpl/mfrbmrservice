package com.rits.sectionbuilderservice.service;

import com.rits.sectionbuilderservice.dto.SectionBuilderRequest;
import com.rits.sectionbuilderservice.exception.SectionBuilderException;
import com.rits.sectionbuilderservice.model.MessageDetails;
import com.rits.sectionbuilderservice.model.MessageModel;
import com.rits.sectionbuilderservice.model.SectionBuilder;
import com.rits.sectionbuilderservice.repository.SectionBuilderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
@Service
@RequiredArgsConstructor
public class SectionBuilderServiceImpl implements SectionBuilderService {
    private final SectionBuilderRepository sectionBuilderRepository;
    private final MessageSource localMessageSource;

    public String getFormattedMessage(int code, Object... args) {
        return localMessageSource.getMessage(String.valueOf(code), args, Locale.getDefault());
    }

    @Override
    public MessageModel create(SectionBuilderRequest request) {
        String handle = createHandle(request);
        SectionBuilder existingSectionBuilder = sectionBuilderRepository.findByHandleAndSiteAndActive(handle, request.getSite(), 1);

        if (existingSectionBuilder != null) {
            throw new SectionBuilderException(2002, request.getSectionLabel());
        }
        SectionBuilder sectionBuilder = sectionBuilder(request);
        sectionBuilder.setHandle(handle);
        sectionBuilder.setCreatedBy(request.getUserId());
        sectionBuilder.setCreatedDateTime(LocalDateTime.now());
        sectionBuilder.setModifiedBy(request.getUserId());
        sectionBuilder.setModifiedDateTime(LocalDateTime.now());


        sectionBuilderRepository.save(sectionBuilder);

        String createMessage = getFormattedMessage(1, request.getSectionLabel());
        return MessageModel.builder().message_details(new MessageDetails(createMessage, "S")).sectionBuilder(sectionBuilder).build();
    }
    @Override
    public MessageModel update(SectionBuilderRequest request) {

        String handle = createHandle(request);
        SectionBuilder existingSectionBuilder = sectionBuilderRepository.findByHandleAndSiteAndActive(handle, request.getSite(), 1);

        if (existingSectionBuilder == null) {
            throw new SectionBuilderException(2003, request.getSectionLabel());
        }

        SectionBuilder sectionBuilder = sectionBuilder(request);
        sectionBuilder.setHandle(handle);
        sectionBuilder.setCreatedBy(existingSectionBuilder.getCreatedBy());
        sectionBuilder.setCreatedDateTime(existingSectionBuilder.getCreatedDateTime());
        sectionBuilder.setModifiedBy(request.getUserId());
        sectionBuilder.setModifiedDateTime(LocalDateTime.now());

        sectionBuilderRepository.save(sectionBuilder);

        String updateMessage = getFormattedMessage(2, request.getSectionLabel());
        return MessageModel.builder().message_details(new MessageDetails(updateMessage, "S")).sectionBuilder(sectionBuilder).build();
    }
    @Override
    public MessageModel delete(SectionBuilderRequest request) {

        String handle = createHandle(request);
        SectionBuilder existingSectionBuilder = sectionBuilderRepository.findByHandleAndSiteAndActive(handle, request.getSite(), 1);

        if (existingSectionBuilder == null) {
            throw new SectionBuilderException(2003, request.getSectionLabel());
        }
        existingSectionBuilder.setActive(0);
        existingSectionBuilder.setModifiedDateTime(LocalDateTime.now());

        sectionBuilderRepository.save(existingSectionBuilder);

        String deleteMessage = getFormattedMessage(3, request.getSectionLabel());
        return MessageModel.builder().message_details(new MessageDetails(deleteMessage, "S")).build();
    }
    @Override
    public List<SectionBuilder> retrieveAll(String site) {

        List<SectionBuilder> existingLineClearanceList = sectionBuilderRepository.findBySiteAndActive(site, 1);
        return existingLineClearanceList;
    }

    @Override
    public SectionBuilder retrieve(SectionBuilderRequest request) {

        String handle = createHandle(request);
        SectionBuilder existingSectionBuilder = sectionBuilderRepository.findByHandleAndSiteAndActive(handle, request.getSite(), 1);
        if(existingSectionBuilder == null){
            throw new SectionBuilderException(2004);
        }
        return existingSectionBuilder;
    }

    @Override
    public List<SectionBuilder> retrieveTop50(String site) {
        List<SectionBuilder> existingSectionBuilderList = sectionBuilderRepository.findTop50BySiteAndActive(site, 1);
        return existingSectionBuilderList;
    }

    @Override
    public boolean isSectionBuilderExist(String site, String sectionLabel) {
        if(!StringUtils.hasText(sectionLabel)){
            throw new SectionBuilderException(2005);
        }
        return sectionBuilderRepository.existsBySiteAndActiveAndSectionLabel(site, 1, sectionLabel);
    }
    private SectionBuilder sectionBuilder(SectionBuilderRequest request) {
        SectionBuilder sectionBuilder = SectionBuilder.builder()
                .site(request.getSite())
                .sectionLabel(request.getSectionLabel())
                .instructions(request.getInstructions())
                .effectiveDateTime(request.getEffectiveDateTime())
                .componentIds(request.getComponentIds())
                .userId(request.getUserId())
                .active(1)
                .build();

        return sectionBuilder;
    }

    private String createHandle(SectionBuilderRequest sectionBuilderRequest){
        validateRequest(sectionBuilderRequest);
        String sectionLabelBO = "SectionBO:" + sectionBuilderRequest.getSite() + "," + sectionBuilderRequest.getSectionLabel();
        return sectionLabelBO;
    }

    public boolean validateRequest(SectionBuilderRequest request){
        if(!StringUtils.hasText(request.getSite())){
            throw new SectionBuilderException(2001);
        }
        if(!StringUtils.hasText(request.getSectionLabel())) {
            throw new SectionBuilderException(2005);
        }
        return true;
    }
}
