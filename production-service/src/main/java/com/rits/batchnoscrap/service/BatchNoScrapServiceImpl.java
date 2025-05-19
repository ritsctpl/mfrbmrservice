package com.rits.batchnoscrap.service;

import com.rits.Utility.ProcessOrderUtility;
import com.rits.batchnocomplete.model.BatchNoComplete;
import com.rits.batchnocomplete.repository.BatchNoCompleteRepository;
import com.rits.batchnoheader.model.BatchNoHeader;
import com.rits.batchnoheader.repository.BatchNoHeaderRepository;
import com.rits.batchnoinqueue.dto.BatchNoInQueueRequest;
import com.rits.batchnoinqueue.model.BatchNoInQueue;
import com.rits.batchnoinqueue.repository.BatchNoInQueueRepository;
import com.rits.batchnoinqueue.service.BatchNoInQueueService;
import com.rits.batchnoinwork.model.BatchNoInWork;
import com.rits.batchnoinwork.repository.BatchNoInWorkRepository;
import com.rits.batchnophaseprogressservice.dto.BatchNoPhaseProgressRequest;
import com.rits.batchnophaseprogressservice.dto.PhaseProgress;
import com.rits.batchnophaseprogressservice.model.BatchNoPhaseProgress;
import com.rits.batchnophaseprogressservice.repository.BatchNoPhaseProgressRepository;
import com.rits.batchnophaseprogressservice.service.BatchNoPhaseProgressServiceImpl;
import com.rits.batchnoscrap.dto.BatchNoScrapQtyResponse;
import com.rits.batchnoscrap.dto.BatchNoScrapRequest;
import com.rits.batchnoscrap.exception.BatchNoScrapException;
import com.rits.batchnoscrap.model.BatchNoScrap;
import com.rits.batchnoscrap.model.BatchNoScrapMessageModel;
import com.rits.batchnoscrap.repository.BatchNoScrapRepository;
import com.rits.batchnoyieldreportingservice.dto.BatchNoYieldReportingRequest;
import com.rits.batchnoyieldreportingservice.model.BatchNoYieldReporting;
import com.rits.batchnoyieldreportingservice.repository.BatchNoYieldReportingRepository;
import com.rits.batchnoyieldreportingservice.service.BatchNoYieldReportingServiceImpl;
import com.rits.processorderstateservice.dto.ProcessOrderCompleteRequest;
import com.rits.productionlogservice.dto.ProductionLogRequest;
import com.rits.scrapservice.model.MessageDetails;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BatchNoScrapServiceImpl implements BatchNoScrapService {
    private final BatchNoScrapRepository batchNoScrapRepository;
    private final BatchNoInQueueRepository batchNoInQueueRepository;
    private final BatchNoInWorkRepository batchNoInWorkRepository;
    private final BatchNoCompleteRepository batchNoCompleteRepository;
    private final MessageSource localMessageSource;
    private final ApplicationEventPublisher eventPublisher;
    private final BatchNoInQueueService batchNoInQueueService;
    private final BatchNoYieldReportingServiceImpl batchNoYieldReportingService;
    private final BatchNoPhaseProgressServiceImpl batchNoPhaseProgressService;
    private final BatchNoPhaseProgressRepository batchNoPhaseProgressRepository;
    private final BatchNoYieldReportingRepository batchNoYieldReportingRepository;
    private final BatchNoHeaderRepository batchNoHeaderRepository;

    public String getFormattedMessage(int code, Object... args) {
        return localMessageSource.getMessage(String.valueOf(code), args, Locale.getDefault());
    }

    @Override
    public BatchNoScrapMessageModel scrap(BatchNoScrapRequest batchNoScrapRequest) throws Exception {

        BatchNoInWork batchNoInWork = batchNoInWorkRepository.findByActiveAndSiteAndBatchNoAndPhaseIdAndOperationAndResourceAndUserAndOrderNumber(1,batchNoScrapRequest.getSite(),
                batchNoScrapRequest.getBatchNo(), batchNoScrapRequest.getPhaseId(), batchNoScrapRequest.getOperation(), batchNoScrapRequest.getResource(), batchNoScrapRequest.getUser(), batchNoScrapRequest.getOrderNumber());

        BigDecimal scrapQuantity = getProperQty(batchNoScrapRequest.getScrapQuantity());
        BatchNoInQueue batchNoInQueue = null;

        if(batchNoInWork == null) {
            throw new BatchNoScrapException(130, batchNoScrapRequest.getBatchNo());
//            batchNoInQueue = batchNoInQueueRepository.findBySiteAndBatchNoAndPhaseIdAndOperationAndOrderNumberAndActive(batchNoScrapRequest.getSite(),
//                    batchNoScrapRequest.getBatchNo(), batchNoScrapRequest.getPhaseId(), batchNoScrapRequest.getOperation(), batchNoScrapRequest.getOrderNumber(), 1);
//
//            if(batchNoInQueue == null)
//                throw new BatchNoScrapException(349);
//
//            if (batchNoInQueue.getQtyInQueue().compareTo(BigDecimal.ZERO) == 0 || scrapQuantity.compareTo(BigDecimal.ZERO) == 0) {
//                throw new BatchNoScrapException(9008);
//            }
//
//            if (scrapQuantity.compareTo(batchNoInQueue.getQtyInQueue()) > 0) {
//                throw new BatchNoScrapException(9006, batchNoScrapRequest.getBatchNo());
//            }
//
//            batchNoInQueue.setQtyInQueue(batchNoInQueue.getQtyInQueue().subtract(scrapQuantity));
//            if (batchNoInQueue.getQtyInQueue().compareTo(BigDecimal.ZERO) == 0) {
//                batchNoInQueue.setActive(0);
//            }
        } else {
            if (batchNoInWork.getQtyToComplete().compareTo(BigDecimal.ZERO) == 0 || scrapQuantity.compareTo(BigDecimal.ZERO) == 0) {
                throw new BatchNoScrapException(9008);
            }

            if (scrapQuantity.compareTo(batchNoInWork.getQtyToComplete()) > 0) {
                throw new BatchNoScrapException(9006, batchNoScrapRequest.getBatchNo());
            }

            batchNoInWork.setQtyToComplete(batchNoInWork.getQtyToComplete().subtract(scrapQuantity));
            if (batchNoInWork.getQtyToComplete().compareTo(BigDecimal.ZERO) == 0) {
                batchNoInWork.setActive(0);
            }

            // call phaseprogress
//            scrapUpdatePhaseProgress(batchNoScrapRequest, batchNoInWork, scrapQuantity);
        }

        BatchNoScrap scrap = batchNoScrapRepository.findBySiteAndOrderNumberAndBatchNoAndPhaseIdAndOperationAndResourceAndActive(batchNoScrapRequest.getSite(), batchNoScrapRequest.getOrderNumber(),
                        batchNoScrapRequest.getBatchNo(), batchNoScrapRequest.getPhaseId(), batchNoScrapRequest.getOperation(), batchNoScrapRequest.getResource(), 1
                )
                .map(existingScrap -> {
                    BigDecimal existingScrapQty = getProperQty(existingScrap.getScrapQuantity());
                    existingScrap.setScrapQuantity(existingScrapQty.add(scrapQuantity));
                    existingScrap.setModifiedDateTime(LocalDateTime.now());
                    existingScrap.setModifiedBy(batchNoScrapRequest.getUser());
                    return existingScrap;
                })
                .orElseGet(() -> BatchNoScrap.builder()
                        .site(batchNoScrapRequest.getSite())
                        .scrapBO("ScrapBO:" + batchNoScrapRequest.getSite() + "," + batchNoScrapRequest.getBatchNo() + "," + batchNoScrapRequest.getOrderNumber())
                        .batchNo(StringUtils.defaultIfEmpty(batchNoScrapRequest.getBatchNo(), null))
                        .status(StringUtils.defaultIfEmpty(batchNoScrapRequest.getStatus(), null))
                        .phaseId(StringUtils.defaultIfEmpty(batchNoScrapRequest.getPhaseId(), null))
                        .operation(StringUtils.defaultIfEmpty(batchNoScrapRequest.getOperation(), null))
                        .resource(StringUtils.defaultIfEmpty(batchNoScrapRequest.getResource(), null))
                        .orderNumber(StringUtils.defaultIfEmpty(batchNoScrapRequest.getOrderNumber(), null))
                        .material(StringUtils.defaultIfEmpty(batchNoScrapRequest.getMaterial(), null))
                        .materialVersion(StringUtils.defaultIfEmpty(batchNoScrapRequest.getMaterialVersion(), null))
                        .recipe(StringUtils.defaultIfEmpty(batchNoScrapRequest.getRecipe(), null))
                        .recipeVersion(StringUtils.defaultIfEmpty(batchNoScrapRequest.getRecipeVersion(), null))
                        .scrapQuantity(scrapQuantity)
                        .user(batchNoScrapRequest.getUser())
                        .reasonCode(batchNoScrapRequest.getReasonCode())
                        .comment(batchNoScrapRequest.getComment())
                        .createdDateTime(LocalDateTime.now())
                        .createdBy(batchNoScrapRequest.getUser())
                        .batchNoHeaderHandle("BatchNoBO:" + batchNoScrapRequest.getSite() + "," + batchNoScrapRequest.getBatchNo())
                        .batchNoRecipeHeaderHandle("BatchNoRecipeHeaderBO:" + batchNoScrapRequest.getSite() + ",RecipeBO:" + batchNoScrapRequest.getSite() + "," + batchNoScrapRequest.getRecipe() + "," + batchNoScrapRequest.getRecipeVersion()
                                + ",BatchNoBO:" + batchNoScrapRequest.getSite() + "," + batchNoScrapRequest.getBatchNo())
                        .active(1)
                        .build());

        if (scrap.getScrapQuantity().compareTo(BigDecimal.ZERO) == 0) {
            scrap.setActive(0);
        }
        batchNoScrapRepository.save(scrap);

        setYieldRecord(batchNoScrapRequest, "scrap");
        setPhaseProgress(batchNoScrapRequest, "scrap");
//        setCompleteRecord(batchNoScrapRequest, "scrap");

//        if(batchNoInWork == null) {
//            batchNoInQueue.setModifiedDateTime(LocalDateTime.now());
//            batchNoInQueue.setModifiedBy(batchNoScrapRequest.getUser());
//            batchNoInQueueRepository.save(batchNoInQueue);
//        } else {
            batchNoInWork.setModifiedDateTime(LocalDateTime.now());
            batchNoInWork.setModifiedBy(batchNoScrapRequest.getUser());
            batchNoInWorkRepository.save(batchNoInWork);
//        }

        logProductionLogForComplete(batchNoScrapRequest);
        logProductionLogForScrap(batchNoScrapRequest);

        return BatchNoScrapMessageModel.builder()
                .message_details(new MessageDetails("Batch " + batchNoScrapRequest.getBatchNo() + " scrapped successfully.", "S"))
                .build();
    }

    public void scrapUpdatePhaseProgress(BatchNoScrapRequest batchNoScrapRequest, BatchNoInWork batchNoInWork, BigDecimal scrapQuantity)  throws Exception {
        BatchNoPhaseProgress existingBatchNoPhaseRecord = getPhaseProgress(batchNoScrapRequest, batchNoInWork.getRecipe(), batchNoInWork.getRecipeVersion());

        if (existingBatchNoPhaseRecord != null && existingBatchNoPhaseRecord.getPhaseProgress() != null) {
            PhaseProgress existingPhaseProgress = existingBatchNoPhaseRecord.getPhaseProgress().stream()
                    .filter(pp -> batchNoScrapRequest.getPhaseId().equals(pp.getPhase()) &&
                            batchNoScrapRequest.getOperation().equals(pp.getOperation()))
                    .findFirst()
                    .orElse(null);

            if (existingPhaseProgress != null) {
                BigDecimal currentQuantity = existingPhaseProgress.getStartQuantityBaseUom();
                if(currentQuantity != null) {
                    BigDecimal updatedQuantity = currentQuantity
                            .subtract(scrapQuantity);
                    existingPhaseProgress.setStartQuantityBaseUom(updatedQuantity);
                    existingPhaseProgress.setStartQuantityMeasuredUom(updatedQuantity);

                    ProcessOrderUtility.updateBatchNoPhaseProgess(existingBatchNoPhaseRecord);
                }
            }
        }
    }

    public void setYieldRecord(BatchNoScrapRequest batchNoScrapRequest, String flag) throws Exception {
        BatchNoHeader batchNoHeader = Optional.ofNullable(getHeaderRecord(batchNoScrapRequest))
                .orElseThrow(() -> new BatchNoScrapException(343));

        BatchNoYieldReportingRequest request = buildYieldRequest(batchNoScrapRequest, batchNoHeader);
        String handle = batchNoYieldReportingService.createHandle(request);

        Optional<BatchNoYieldReporting> existingYieldOpt = Optional.ofNullable(
                batchNoYieldReportingRepository.findByHandleAndSiteAndActive(handle, request.getSite(), 1));

        if (existingYieldOpt.isEmpty()) {
            batchNoYieldReportingService.create(request);
        } else {
            BatchNoYieldReporting existingYield = existingYieldOpt.get();
            switch (flag.toLowerCase()) {
                case "scrap":
                    scrapQtyUpdate(existingYield, request);
                    break;
                case "unscrap":
                    unScrapQtyUpdate(existingYield, request);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid input from: " + flag);
            }
            batchNoYieldReportingService.update(request);
        }
    }

    public void scrapQtyUpdate(BatchNoYieldReporting existingYield, BatchNoYieldReportingRequest request) {
        if (existingYield == null || request == null) {
            throw new BatchNoScrapException(3470);
        }

        request.setScrapQuantityBaseUom(safeAdd(existingYield.getScrapQuantityBaseUom(), request.getScrapQuantityBaseUom()));
        request.setScrapQuantityMeasuredUom(safeAdd(existingYield.getScrapQuantityMeasuredUom(), request.getScrapQuantityMeasuredUom()));
        request.setActualYieldBaseUom(safeAdd(existingYield.getActualYieldBaseUom(), request.getActualYieldBaseUom()));
        request.setActualYieldMeasuredUom(safeAdd(existingYield.getActualYieldMeasuredUom(), request.getActualYieldMeasuredUom()));
        request.setYieldVarianceBaseUom(safeAdd(existingYield.getYieldVarianceBaseUom(), request.getYieldVarianceBaseUom()));
        request.setYieldVarianceMeasuredUom(safeAdd(existingYield.getYieldVarianceMeasuredUom(), request.getYieldVarianceMeasuredUom()));
        request.setTheoreticalYieldBaseUom(safeAdd(existingYield.getTheoreticalYieldBaseUom(), request.getTheoreticalYieldBaseUom()));
        request.setTheoreticalYieldMeasuredUom(safeAdd(existingYield.getTheoreticalYieldMeasuredUom(), request.getTheoreticalYieldMeasuredUom()));
    }

    private BigDecimal safeAdd(BigDecimal value1, BigDecimal value2) {
        return (value1 != null ? value1 : BigDecimal.ZERO).add(value2 != null ? value2 : BigDecimal.ZERO);
    }

    public void unScrapQtyUpdate(BatchNoYieldReporting existingYield, BatchNoYieldReportingRequest request) {//
        if (existingYield == null || request == null) {
            throw new BatchNoScrapException(3470);
        }

        request.setScrapQuantityBaseUom(safeSubtract(existingYield.getScrapQuantityBaseUom(), request.getScrapQuantityBaseUom()));
        request.setScrapQuantityMeasuredUom(safeSubtract(existingYield.getScrapQuantityMeasuredUom(), request.getScrapQuantityMeasuredUom()));
        request.setActualYieldBaseUom(safeSubtract(existingYield.getActualYieldBaseUom(), request.getActualYieldBaseUom()));
        request.setActualYieldMeasuredUom(safeSubtract(existingYield.getActualYieldMeasuredUom(), request.getActualYieldMeasuredUom()));
        request.setYieldVarianceBaseUom(safeSubtract(existingYield.getYieldVarianceBaseUom(), request.getYieldVarianceBaseUom()));
        request.setYieldVarianceMeasuredUom(safeSubtract(existingYield.getYieldVarianceMeasuredUom(), request.getYieldVarianceMeasuredUom()));
        request.setTheoreticalYieldBaseUom(safeSubtract(existingYield.getTheoreticalYieldBaseUom(), request.getTheoreticalYieldBaseUom()));
        request.setTheoreticalYieldMeasuredUom(safeSubtract(existingYield.getTheoreticalYieldMeasuredUom(), request.getTheoreticalYieldMeasuredUom()));
    }

    private BigDecimal safeSubtract(BigDecimal value1, BigDecimal value2) {
        return (value1 != null ? value1 : BigDecimal.ZERO).subtract(value2 != null ? value2 : BigDecimal.ZERO);
    }

    public BatchNoHeader getHeaderRecord(BatchNoScrapRequest batchNoScrapRequest) {
        return batchNoHeaderRepository.findBySiteAndBatchNoAndOrderNoAndMaterialAndMaterialVersionAndActive(
                batchNoScrapRequest.getSite(), batchNoScrapRequest.getBatchNo(), batchNoScrapRequest.getOrderNumber(), batchNoScrapRequest.getMaterial(), batchNoScrapRequest.getMaterialVersion(), 1);
    }
    public BatchNoInWork getInWorkRecord(BatchNoScrapRequest batchNoScrapRequest) {
        return batchNoInWorkRepository.findByActiveAndSiteAndBatchNoAndPhaseIdAndOperationAndResourceAndUserAndOrderNumber(1,
                batchNoScrapRequest.getSite(), batchNoScrapRequest.getBatchNo(), batchNoScrapRequest.getPhaseId(), batchNoScrapRequest.getOperation(), batchNoScrapRequest.getResource(),
                batchNoScrapRequest.getUser(), batchNoScrapRequest.getOrderNumber());
    }

    public BatchNoYieldReportingRequest buildYieldRequest(BatchNoScrapRequest batchNoScrapRequest, BatchNoHeader batchNoHeader) {
        if (batchNoScrapRequest == null || batchNoHeader == null) {
            throw new BatchNoScrapException(348);
        }

        BigDecimal scrapQty = getProperQty(batchNoScrapRequest.getScrapQuantity());

        return BatchNoYieldReportingRequest.builder()
                .site(getProperString(batchNoScrapRequest.getSite()))
                .batchNo(getProperString(batchNoScrapRequest.getBatchNo()))
                .phaseId(getProperString(batchNoScrapRequest.getPhaseId()))
                .operation(getProperString(batchNoScrapRequest.getOperation()))
                .resource(getProperString(batchNoScrapRequest.getResource()))
                .recipe(getProperString(batchNoHeader.getRecipeName()))
                .recipeVersion(getProperString(batchNoHeader.getRecipeVersion()))
                .baseUom(getProperString(batchNoHeader.getBaseUom()))
                .measuredUom(getProperString(batchNoHeader.getMeasuredUom()))
                .reportTimestamp(batchNoHeader.getCreatedDateTime() != null ? batchNoHeader.getCreatedDateTime() : LocalDateTime.now())
                .user(getProperString(batchNoScrapRequest.getUser()))
                .theoreticalYieldBaseUom(batchNoScrapRequest.getTheoreticalYield())// COMPLETE WITHOUT SCRAP
                .theoreticalYieldMeasuredUom(batchNoScrapRequest.getTheoreticalYield())
                .actualYieldBaseUom(batchNoScrapRequest.getActualYield())// COMPLETE AFTER SCRAP
                .actualYieldMeasuredUom(batchNoScrapRequest.getActualYield())
                .yieldVarianceBaseUom(scrapQty)
                .yieldVarianceMeasuredUom(scrapQty)
                .scrapQuantityBaseUom(scrapQty)
                .scrapQuantityMeasuredUom(scrapQty)
                .orderNumber(getProperString(batchNoScrapRequest.getOrderNumber()))
                .active(1)
                .build();
    }

    private String getProperString(String value) {
        return value != null ? value : "";
    }

    private BigDecimal getProperQty(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    public void setPhaseProgress(BatchNoScrapRequest batchNoScrapRequest, String flag) throws Exception {
        if (batchNoScrapRequest == null || flag == null) {
            throw new BatchNoScrapException(348);
        }

        BatchNoHeader batchNoHeader = getHeaderRecord(batchNoScrapRequest);
        if (batchNoHeader == null) {
            throw new BatchNoScrapException(343);
        }

        BatchNoInWork batchNoInWork = getInWorkRecord(batchNoScrapRequest);

        BatchNoPhaseProgress existingPhaseProgress = getPhaseProgress(batchNoScrapRequest, batchNoHeader.getRecipeName(), batchNoHeader.getRecipeVersion());
        BatchNoPhaseProgressRequest phaseRequest = buildPhaseProgress(batchNoScrapRequest, batchNoHeader, batchNoInWork);

        if (phaseRequest == null || phaseRequest.getPhaseProgress() == null || phaseRequest.getPhaseProgress().isEmpty()) {
            throw new BatchNoScrapException(344);
        }

        if (existingPhaseProgress == null || existingPhaseProgress.getPhaseProgress() == null) {
            batchNoPhaseProgressService.create(phaseRequest);
            return;
        }

        PhaseProgress matchingPhase = existingPhaseProgress != null && existingPhaseProgress.getPhaseProgress() != null
                ? existingPhaseProgress.getPhaseProgress()
                .stream()
                .filter(phase -> phase != null && batchNoScrapRequest.getPhaseId().equals(phase.getPhase()))
                .findFirst()
                .orElse(null)
                : null;

        if (matchingPhase != null) {
            BigDecimal existingScrapQty = getProperQty(matchingPhase.getScrapQuantity());
            BigDecimal providedScrapQty = getProperQty(phaseRequest.getPhaseProgress().get(0).getScrapQuantity());

            // Update scrap quantity based on the flag
            BigDecimal updatedScrapQty = flag.equalsIgnoreCase("scrap")
                    ? existingScrapQty.add(providedScrapQty)
                    : flag.equalsIgnoreCase("unScrap")
                    ? existingScrapQty.subtract(providedScrapQty)
                    : existingScrapQty;

            BigDecimal completeQty = (matchingPhase.getCompleteQuantityBaseUom() == null ? BigDecimal.ZERO : matchingPhase.getCompleteQuantityBaseUom())
                    .add(batchNoScrapRequest.getActualYield() == null ? BigDecimal.ZERO : batchNoScrapRequest.getActualYield());

            phaseRequest.getPhaseProgress().get(0).setScrapQuantity(updatedScrapQty);
            phaseRequest.getPhaseProgress().get(0).setScrapQuantityBaseUom(updatedScrapQty);
            phaseRequest.getPhaseProgress().get(0).setScrapQuantityMeasuredUom(updatedScrapQty);
            phaseRequest.getPhaseProgress().get(0).setCompleteQuantityBaseUom(completeQty);
            phaseRequest.getPhaseProgress().get(0).setCompleteQuantityMeasuredUom(completeQty);
        }

        batchNoPhaseProgressService.update(phaseRequest);
    }


    public void setCompleteRecord(BatchNoScrapRequest batchNoScrapRequest, String flag) throws Exception {
        if (batchNoScrapRequest == null || flag == null) {
            throw new BatchNoScrapException(348);
        }

        BatchNoHeader batchNoHeader = getHeaderRecord(batchNoScrapRequest);
        if (batchNoHeader == null) {
            throw new BatchNoScrapException(343);
        }

        BatchNoInWork batchNoInWork = getInWorkRecord(batchNoScrapRequest);

        boolean isProcessResource = ProcessOrderUtility.getResourceDetails(batchNoScrapRequest.getSite(), batchNoScrapRequest.getResource());

        if(flag.equalsIgnoreCase("scrap"))
            createUpdateScrapForComplete(batchNoScrapRequest, batchNoHeader, isProcessResource, batchNoInWork);
        else if(flag.equalsIgnoreCase("unScrap"))
            updateScrapForComplete(batchNoScrapRequest, batchNoHeader, isProcessResource, batchNoInWork);
    }

    private void createUpdateScrapForComplete(BatchNoScrapRequest batchNoScrapRequest, BatchNoHeader batchNoHeader, boolean isProcessResource, BatchNoInWork batchNoInWork) throws Exception {

        BatchNoComplete existingBatchComplete = ProcessOrderUtility.getBatchNoCompleteDetails(batchNoScrapRequest.getSite(), batchNoHeader.getHandle(), batchNoScrapRequest.getPhaseId(), batchNoScrapRequest.getOperation(),
                batchNoScrapRequest.getResource(), isProcessResource ? null : batchNoScrapRequest.getUser(), batchNoScrapRequest.getOrderNumber());

        if (existingBatchComplete != null) {

            existingBatchComplete.setScrapQuantityBaseUom((existingBatchComplete.getScrapQuantityBaseUom() != null ? existingBatchComplete.getScrapQuantityBaseUom() : BigDecimal.ZERO)
                    .add(batchNoScrapRequest.getScrapQuantity() != null ? batchNoScrapRequest.getScrapQuantity() : BigDecimal.ZERO));

            existingBatchComplete.setScrapQuantityMeasuredUom((existingBatchComplete.getScrapQuantityMeasuredUom() != null ? existingBatchComplete.getScrapQuantityMeasuredUom() : BigDecimal.ZERO)
                    .add(batchNoScrapRequest.getScrapQuantity() != null ? batchNoScrapRequest.getScrapQuantity() : BigDecimal.ZERO));

            ProcessOrderUtility.updateBatchNoComplete(existingBatchComplete);

        } else {
            ProcessOrderUtility.createBatchNoComplete(buildBatchComplete(batchNoScrapRequest), batchNoInWork, isProcessResource,BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO);
        }
    }

    private void updateScrapForComplete(BatchNoScrapRequest batchNoScrapRequest, BatchNoHeader batchNoHeader, boolean isProcessResource, BatchNoInWork batchNoInWork) throws Exception {

        BatchNoComplete existingBatchComplete = ProcessOrderUtility.getBatchNoCompleteDetails(batchNoScrapRequest.getSite(), batchNoHeader.getHandle(), batchNoScrapRequest.getPhaseId(), batchNoScrapRequest.getOperation(),
                batchNoScrapRequest.getResource(), isProcessResource ? null : batchNoScrapRequest.getUser(), batchNoScrapRequest.getOrderNumber());

        if (existingBatchComplete != null) {

            if(existingBatchComplete.getScrapQuantityBaseUom() != null)
                existingBatchComplete.setScrapQuantityBaseUom((existingBatchComplete.getScrapQuantityBaseUom())
                    .subtract(batchNoScrapRequest.getScrapQuantity() != null ? batchNoScrapRequest.getScrapQuantity() : BigDecimal.ZERO));

            if(existingBatchComplete.getScrapQuantityMeasuredUom() != null)
                existingBatchComplete.setScrapQuantityMeasuredUom((existingBatchComplete.getScrapQuantityMeasuredUom())
                    .subtract(batchNoScrapRequest.getScrapQuantity() != null ? batchNoScrapRequest.getScrapQuantity() : BigDecimal.ZERO));

            ProcessOrderUtility.updateBatchNoComplete(existingBatchComplete);

        } else {
            throw new BatchNoScrapException(126);
        }
    }

    public ProcessOrderCompleteRequest.BatchDetails buildBatchComplete(BatchNoScrapRequest batchNoScrapRequest) {
        ProcessOrderCompleteRequest.BatchDetails completeReq = ProcessOrderCompleteRequest.BatchDetails.builder()
                .site(batchNoScrapRequest.getSite())
                .batchNumber(batchNoScrapRequest.getBatchNo())
                .material(batchNoScrapRequest.getMaterial())
                .materialVersion(batchNoScrapRequest.getMaterialVersion())
                .phase(batchNoScrapRequest.getPhaseId())
                .operation(batchNoScrapRequest.getOperation())
                .resource(batchNoScrapRequest.getResource())
                .user(batchNoScrapRequest.getUser())
                .scrapQuantity(batchNoScrapRequest.getScrapQuantity())
                .orderNumber(batchNoScrapRequest.getOrderNumber())
                .build();
        return  completeReq;
    }

    public BatchNoPhaseProgress getPhaseProgress(BatchNoScrapRequest batchNoScrapRequest, String recipe, String recipeVersion) throws Exception {
        return batchNoPhaseProgressRepository.findBySiteAndBatchNoAndMaterialAndOrderNumberAndMaterialVersionAndActiveAndRecipeAndRecipeVersion(batchNoScrapRequest.getSite(),
                batchNoScrapRequest.getBatchNo(), batchNoScrapRequest.getMaterial(), batchNoScrapRequest.getOrderNumber(), batchNoScrapRequest.getMaterialVersion(),
                1, recipe, recipeVersion);
    }

    public BatchNoPhaseProgressRequest buildPhaseProgress(BatchNoScrapRequest batchNoScrapRequest, BatchNoHeader batchNoHeader, BatchNoInWork batchNoInWork) {
        if (batchNoScrapRequest == null || batchNoHeader == null) {
            throw new BatchNoScrapException(348);
        }

        BigDecimal qtyToComplete = getProperQty(batchNoInWork == null ? BigDecimal.ZERO : batchNoInWork.getQtyToComplete());
        BigDecimal scrapQty = getProperQty(batchNoScrapRequest.getScrapQuantity());

        PhaseProgress newPhaseProgress = PhaseProgress.builder()
                .phase(getProperString(batchNoScrapRequest.getPhaseId()))
                .operation(getProperString(batchNoScrapRequest.getOperation()))
                .completeQuantityBaseUom(batchNoScrapRequest.getActualYield())
                .completeQuantityMeasuredUom(batchNoScrapRequest.getActualYield())
                .scrapQuantity(scrapQty)
                .scrapQuantityBaseUom(scrapQty)
                .scrapQuantityMeasuredUom(scrapQty)
                .baseUom(getProperString(batchNoHeader.getBaseUom()))
                .measuredUom(getProperString(batchNoHeader.getMeasuredUom()))
                .startTimestamp(getSafeTimestamp(batchNoHeader.getCreatedDateTime()))
                .endTimestamp(LocalDateTime.now())
                .status("Completed")
                .build();

        return BatchNoPhaseProgressRequest.builder()
                .batchNo(getProperString(batchNoScrapRequest.getBatchNo()))
                .material(getProperString(batchNoScrapRequest.getMaterial()))
                .materialVersion(getProperString(batchNoScrapRequest.getMaterialVersion()))
                .orderNumber(getProperString(batchNoScrapRequest.getOrderNumber()))
                .site(getProperString(batchNoScrapRequest.getSite()))
                .recipe(getProperString(batchNoHeader.getRecipeName()))
                .recipeVersion(getProperString(batchNoHeader.getRecipeVersion()))
                .phaseProgress(List.of(newPhaseProgress))
                .dateTime(LocalDateTime.now())
                .user(getProperString(batchNoScrapRequest.getUser()))
                .build();
    }

    private LocalDateTime getSafeTimestamp(LocalDateTime value) {
        return value != null ? value : LocalDateTime.now();
    }

    @Override
    public BatchNoScrapMessageModel unScrap(BatchNoScrapRequest batchNoScrapRequest) throws Exception {

        validateUnScrapRequest(batchNoScrapRequest);

        BatchNoScrap existingBatchNoScrap = getBatchScrapRecord(batchNoScrapRequest);
        validateExistingScrap(existingBatchNoScrap, batchNoScrapRequest);

        existingBatchNoScrap.setScrapQuantity(existingBatchNoScrap.getScrapQuantity().subtract(batchNoScrapRequest.getScrapQuantity()));

        boolean updated = updateOrCreateQueue(batchNoScrapRequest);

        existingBatchNoScrap.setModifiedBy(batchNoScrapRequest.getUser());
        existingBatchNoScrap.setModifiedDateTime(LocalDateTime.now());
        batchNoScrapRepository.save(existingBatchNoScrap);

        setYieldRecord(batchNoScrapRequest, "unScrap");
        setPhaseProgress(batchNoScrapRequest, "unScrap");
//        setCompleteRecord(batchNoScrapRequest, "unScrap");

        if (!logProductionLogForUnScrap(batchNoScrapRequest)) {
            throw new BatchNoScrapException(7024);
        }

        return BatchNoScrapMessageModel.builder()
                .message_details(new MessageDetails("Batch " + batchNoScrapRequest.getBatchNo() + " unscrapped successfully.", "S"))
                .build();
    }

    private void validateUnScrapRequest(BatchNoScrapRequest batchNoScrapRequest) throws BatchNoScrapException {
        if (batchNoScrapRequest.getScrapQuantity() == null || batchNoScrapRequest.getScrapQuantity().compareTo(BigDecimal.ZERO) == 0) {
            throw new BatchNoScrapException(9008);
        }
    }

    private void validateExistingScrap(BatchNoScrap existingBatchNoScrap, BatchNoScrapRequest batchNoScrapRequest) throws BatchNoScrapException {
        if (existingBatchNoScrap == null) {
            throw new BatchNoScrapException(9008);
        }
        if (batchNoScrapRequest.getScrapQuantity().compareTo(existingBatchNoScrap.getScrapQuantity()) > 0) {
            throw new BatchNoScrapException(9007, batchNoScrapRequest.getBatchNo());
        }
    }

    private boolean updateOrCreateQueue(BatchNoScrapRequest batchNoScrapRequest) throws Exception {
        BatchNoInQueue batchInQueue = batchNoInQueueRepository.findBySiteAndBatchNoAndPhaseIdAndOperationAndOrderNumberAndActive(
                batchNoScrapRequest.getSite(), batchNoScrapRequest.getBatchNo(), batchNoScrapRequest.getPhaseId(),
                batchNoScrapRequest.getOperation(), batchNoScrapRequest.getOrderNumber(), 1);

        if (batchInQueue != null) {
            batchInQueue.setQtyInQueue(getProperQty(batchInQueue.getQtyInQueue()).add(batchNoScrapRequest.getScrapQuantity()));
            batchInQueue.setModifiedDateTime(LocalDateTime.now());
            batchInQueue.setModifiedBy(batchNoScrapRequest.getUser());
            batchNoInQueueRepository.save(batchInQueue);
            return true;
        } else {
            BatchNoInWork batchNoInWork = batchNoInWorkRepository.findBySiteAndBatchNoAndPhaseIdAndOperationAndResourceAndUserAndActiveAndOrderNumber(
                    batchNoScrapRequest.getSite(), batchNoScrapRequest.getBatchNo(), batchNoScrapRequest.getPhaseId(),
                    batchNoScrapRequest.getOperation(), batchNoScrapRequest.getResource(), batchNoScrapRequest.getUser(), 1,
                    batchNoScrapRequest.getOrderNumber());

            if (batchNoInWork == null) {
                throw new BatchNoScrapException(346);
            }

            BatchNoInQueueRequest batchNoInQueueRequest = buildInQueueRequest(batchNoInWork);
            batchNoInQueueRequest.setQtyInQueue(batchNoScrapRequest.getScrapQuantity());
            batchNoInQueueRequest.setQualityApproval(true);
            batchNoInQueueService.createBatchNoInQueue(batchNoInQueueRequest);
            return false;
        }
    }

    public BatchNoInQueueRequest buildInQueueRequest(BatchNoInWork batchNoInWork) {
        return BatchNoInQueueRequest.builder()
                .site(batchNoInWork.getSite())
                .dateTime(batchNoInWork.getDateTime())
                .batchNo(batchNoInWork.getBatchNo())
                .batchNoHeaderBO(batchNoInWork.getBatchNoHeaderBO())
                .material(batchNoInWork.getMaterial())
                .materialVersion(batchNoInWork.getMaterialVersion())
                .recipe(batchNoInWork.getRecipe())
                .recipeVersion(batchNoInWork.getRecipeVersion())
                .batchNoRecipeHeaderBO(batchNoInWork.getBatchNoRecipeHeaderBO())
                .phaseId(batchNoInWork.getPhaseId())
                .operation(batchNoInWork.getOperation())
                .quantityBaseUom(getProperQty(batchNoInWork.getQuantityBaseUom()))
                .quantityMeasuredUom(getProperQty(batchNoInWork.getQuantityMeasuredUom()))
                .baseUom(batchNoInWork.getBaseUom())
                .measuredUom(batchNoInWork.getMeasuredUom())
                .queuedTimestamp(batchNoInWork.getQueuedTimestamp())
                .resource(batchNoInWork.getResource())
                .workcenter(batchNoInWork.getWorkcenter())
                .user(batchNoInWork.getUser())
//                .qtyToComplete(getProperQty(batchNoInWork.getQtyToComplete()))
                .qtyInQueue(getProperQty(batchNoInWork.getQtyInQueue()))
                .orderNumber(batchNoInWork.getOrderNumber())
                .active(1)
                .build();
    }

    @Override
    public BatchNoScrapMessageModel delete(BatchNoScrapRequest batchNoScrapRequest) throws Exception {
        StringBuilder deletedRecords = new StringBuilder();

        List<List<?>> batchRecords = Collections.singletonList(Arrays.asList(
                batchNoInQueueRepository.findBySiteAndBatchNoAndPhaseIdAndOperationAndOrderNumberAndActive(
                        batchNoScrapRequest.getSite(), batchNoScrapRequest.getBatchNo(), batchNoScrapRequest.getPhaseId(),
                        batchNoScrapRequest.getOperation(), batchNoScrapRequest.getOrderNumber(), 1),
                batchNoInWorkRepository.findByBatchNoAndPhaseIdAndOperationAndResourceAndActiveAndSite(
                        batchNoScrapRequest.getBatchNo(), batchNoScrapRequest.getPhaseId(),
                        batchNoScrapRequest.getOperation(), batchNoScrapRequest.getResource(), 1, batchNoScrapRequest.getSite()),
                batchNoCompleteRepository.findBySiteAndBatchNoAndPhaseIdAndOperationAndResourceAndActive(
                        batchNoScrapRequest.getSite(), batchNoScrapRequest.getBatchNo(), batchNoScrapRequest.getPhaseId(),
                        batchNoScrapRequest.getOperation(), batchNoScrapRequest.getResource(), 1)
        ));

        for (List<?> batchRecordList : batchRecords) {
            if (!batchRecordList.isEmpty()) {
                for (Object record : batchRecordList) {

                    ProductionLogRequest productionLogRequest = ProductionLogRequest.builder()
                            .site(batchNoScrapRequest.getSite())
                            .eventType("BATCH-DELETE")
                            .userId(batchNoScrapRequest.getUser())
                            .batchNo(batchNoScrapRequest.getBatchNo())
                            .orderNumber(batchNoScrapRequest.getOrderNumber())
                            .operation(batchNoScrapRequest.getOperation())
                            .phaseId(batchNoScrapRequest.getPhaseId())
                            .workcenterId(batchNoScrapRequest.getWorkcenter())
                            .resourceId(batchNoScrapRequest.getResource())
                            .material(batchNoScrapRequest.getMaterial())
                            .materialVersion(batchNoScrapRequest.getMaterialVersion())
                            .qty(Optional.ofNullable(batchNoScrapRequest.getScrapQuantity())
                                    .map(scrapQuantity -> scrapQuantity.setScale(0, RoundingMode.HALF_UP).intValue())
                                    .orElse(0))
                            .topic("production-log")
                            .status("Active")
                            .createdDatetime(LocalDateTime.now())
                            .eventData(batchNoScrapRequest.getBatchNo() + " Deleted successfully")
                            .build();

                    // Call the utility method to process the log
                    boolean productionLog = ProcessOrderUtility.productionLog(productionLogRequest);

                    if (!productionLog) {
                        throw new BatchNoScrapException(7024);
                    }

                    if (record instanceof BatchNoInQueue) {
                        deletedRecords.append(((BatchNoInQueue) record).getBatchNo())
                                .append(" (").append(record.getClass().getSimpleName()).append("), ");
                    } else if (record instanceof BatchNoInWork) {
                        deletedRecords.append(((BatchNoInWork) record).getBatchNo())
                                .append(" (").append(record.getClass().getSimpleName()).append("), ");
                    } else if (record instanceof BatchNoComplete) {
                        deletedRecords.append(((BatchNoComplete) record).getBatchNo())
                                .append(" (").append(record.getClass().getSimpleName()).append("), ");
                    }

                    if (record instanceof BatchNoInQueue) {
                        batchNoInQueueRepository.delete((BatchNoInQueue) record);
                    } else if (record instanceof BatchNoInWork) {
                        batchNoInWorkRepository.delete((BatchNoInWork) record);
                    } else if (record instanceof BatchNoComplete) {
                        batchNoCompleteRepository.delete((BatchNoComplete) record);
                    }
                }
            }
        }

        if (deletedRecords.length() > 0) {
            deletedRecords.delete(deletedRecords.length() - 2, deletedRecords.length());
            BatchNoScrap existingBatchNoScrap = batchNoScrapRepository.findByActiveAndSiteAndBatchNoAndOperationAndResourceAndPhaseIdAndOrderNumber(1 , batchNoScrapRequest.getSite(), batchNoScrapRequest.getBatchNo(),
                    batchNoScrapRequest.getOperation(), batchNoScrapRequest.getResource(), batchNoScrapRequest.getPhaseId(), batchNoScrapRequest.getOrderNumber());

            if (existingBatchNoScrap == null) {
                throw new BatchNoScrapException(9005, batchNoScrapRequest.getBatchNo());
            }

            batchNoScrapRepository.delete(existingBatchNoScrap);
            return BatchNoScrapMessageModel.builder()
                    .message_details(new MessageDetails("Batch "+batchNoScrapRequest.getBatchNo() + " deleted successfully.", "S"))
                    .build();
        } else {
            return BatchNoScrapMessageModel.builder()
                    .message_details(new MessageDetails("No records found to delete.", "F"))
                    .build();
        }
    }

    @Override
    public List<BatchNoScrap> retrieveAllScrap(String site) throws Exception {
        List<BatchNoScrap> scrapList = batchNoScrapRepository.findByActiveAndSite(1, site);
        return scrapList;
    }

    @Override
    public BatchNoScrapMessageModel retrieve(BatchNoScrapRequest batchNoScrapRequest) throws Exception {
        if(StringUtils.isBlank(batchNoScrapRequest.getSite()) || StringUtils.isBlank(batchNoScrapRequest.getBatchNo()))
            throw new BatchNoScrapException(3510);

        BatchNoScrap existingBatchNoScrap = getBatchScrapRecord(batchNoScrapRequest);

        if(existingBatchNoScrap == null)
            return BatchNoScrapMessageModel.builder()
                    .batchNoScrapResponse(null)
                    .build();

        return BatchNoScrapMessageModel.builder()
                .batchNoScrapResponse(existingBatchNoScrap)
                .build();
    }

    public BatchNoScrap getBatchScrapRecord(BatchNoScrapRequest batchNoScrapRequest) {
        return batchNoScrapRepository.findByActiveAndSiteAndBatchNoAndOperationAndResourceAndPhaseIdAndOrderNumber(1 , batchNoScrapRequest.getSite(),
                batchNoScrapRequest.getBatchNo(), batchNoScrapRequest.getOperation(), batchNoScrapRequest.getResource(), batchNoScrapRequest.getPhaseId(), batchNoScrapRequest.getOrderNumber());
    }

    public BatchNoScrapMessageModel retrieveByBatchNo(BatchNoScrapRequest batchNoScrapRequest) throws Exception {
        if(StringUtils.isBlank(batchNoScrapRequest.getSite()) || StringUtils.isBlank(batchNoScrapRequest.getBatchNo()))
            throw new BatchNoScrapException(3510);

        List<BatchNoScrap> scrapList = batchNoScrapRepository.findByActiveAndSiteAndBatchNo(1, batchNoScrapRequest.getSite(), batchNoScrapRequest.getBatchNo());

        if(scrapList == null)
            return BatchNoScrapMessageModel.builder()
                    .batchNoScrapResponse(null)
                    .build();

        return BatchNoScrapMessageModel.builder()
                .batchNoScrapList(scrapList)
                .build();
    }


    private void scrapFromRelatedCollections(BatchNoScrapRequest batchNoScrapRequest, StringBuilder scrappedBatchNos) throws Exception {
        // Scrap from batchInQueue
//        List<BatchNoInQueue> batchNoInQueues = batchNoInQueueRepository.findBySiteAndBatchNoAndPhaseIdAndOperationAndResourceAndActive(
//                batchNoScrapRequest.getSite(), batchNoScrapRequest.getBatchNo(), batchNoScrapRequest.getPhaseId(),
//                batchNoScrapRequest.getOperation(), batchNoScrapRequest.getResource(), 1);
//
//        if (!batchNoInQueues.isEmpty()) {
//            scrappedBatchNos.append("BatchNoInQueue: ");
//            for (BatchNoInQueue batchInQueue : batchNoInQueues) {
//                batchInQueue.setActive(0);
//                batchInQueue.setModifiedDateTime(LocalDateTime.now());
//                batchInQueue.setModifiedBy(batchNoScrapRequest.getUser());
//                batchNoInQueueRepository.save(batchInQueue);
//                scrappedBatchNos.append(batchInQueue.getBatchNo()).append(", ");
//            }
//        }
        // Scrap from batchInWork
        List<BatchNoInWork> batchNoInWorks = batchNoInWorkRepository.findBySiteAndBatchNoAndPhaseIdAndOperationAndResourceAndActive(
                batchNoScrapRequest.getSite(), batchNoScrapRequest.getBatchNo(), batchNoScrapRequest.getPhaseId(),
                batchNoScrapRequest.getOperation(), batchNoScrapRequest.getResource(), 1);

        if (!batchNoInWorks.isEmpty()) {
            scrappedBatchNos.append("BatchNoInWork: ");
            for (BatchNoInWork batchNoInWork : batchNoInWorks) {
                if (batchNoInWork.getQtyToComplete().compareTo(BigDecimal.ZERO) == 0) throw new BatchNoScrapException(9008);
                if (batchNoScrapRequest.getScrapQuantity() == null || batchNoScrapRequest.getScrapQuantity().compareTo(BigDecimal.ZERO) == 0) {
                    batchNoScrapRequest.setScrapQuantity(batchNoInWork.getQtyToComplete());
                    batchNoInWork.setQtyToComplete(BigDecimal.ZERO);
                }else if (batchNoScrapRequest.getScrapQuantity().compareTo(batchNoInWork.getQtyToComplete()) > 0){
                    throw new BatchNoScrapException(9006, batchNoScrapRequest.getBatchNo());
                }
                else {
                    batchNoInWork.setQtyToComplete(batchNoInWork.getQtyToComplete().subtract(batchNoScrapRequest.getScrapQuantity()));
                }
//              batchNoInWork.setActive(0);
                batchNoInWork.setModifiedDateTime(LocalDateTime.now());
                batchNoInWork.setModifiedBy(batchNoScrapRequest.getUser());
                batchNoInWorkRepository.save(batchNoInWork);
                scrappedBatchNos.append(batchNoInWork.getBatchNo()).append(", ");
            }
        }

        // Scrap from batchComplete
//        List<BatchNoComplete> batchNoCompletes = batchNoCompleteRepository.findBySiteAndBatchNoAndPhaseIdAndOperationAndResourceAndActive(
//                batchNoScrapRequest.getSite(), batchNoScrapRequest.getBatchNo(), batchNoScrapRequest.getPhaseId(),
//                batchNoScrapRequest.getOperation(), batchNoScrapRequest.getResource(), 1);
//
//        if (!batchNoCompletes.isEmpty()) {
//            scrappedBatchNos.append("BatchNoComplete: ");
//            for (BatchNoComplete batchNoComplete : batchNoCompletes) {
//                batchNoComplete.setActive(0);
//                batchNoComplete.setModifiedDateTime(LocalDateTime.now());
//                batchNoComplete.setModifiedBy(batchNoScrapRequest.getUser());
//                batchNoCompleteRepository.save(batchNoComplete);
//                scrappedBatchNos.append(batchNoComplete.getBatchNo()).append(", ");
//            }
//        }

        if (scrappedBatchNos.length() > 0 && scrappedBatchNos.charAt(scrappedBatchNos.length() - 2) == ',') {
            scrappedBatchNos.delete(scrappedBatchNos.length() - 2, scrappedBatchNos.length());
        }
    }

    private boolean logProductionLogForComplete(BatchNoScrapRequest batchNoScrapRequest) throws Exception {
        ProductionLogRequest productionLogRequest = ProductionLogRequest.builder()
                .site(batchNoScrapRequest.getSite())
                .eventType("completeSfcBatch")
                .userId(batchNoScrapRequest.getUser())
                .batchNo(batchNoScrapRequest.getOrderNumber() + "_" + batchNoScrapRequest.getBatchNo())
                .orderNumber(batchNoScrapRequest.getOrderNumber())
                .operation(batchNoScrapRequest.getOperation())
                .phaseId(batchNoScrapRequest.getPhaseId())
                .workcenterId(batchNoScrapRequest.getWorkcenter())
                .resourceId(batchNoScrapRequest.getResource())
                .material(batchNoScrapRequest.getMaterial())
                .materialVersion(batchNoScrapRequest.getMaterialVersion())
                .shopOrderBO(batchNoScrapRequest.getOrderNumber())
                .qty(batchNoScrapRequest.getScrapQuantity().intValue())
                .reasonCode(batchNoScrapRequest.getReasonCode())
                .topic("production-log")
                .status("Active")
                .createdDatetime(LocalDateTime.now())
                .eventData(batchNoScrapRequest.getBatchNo() + " Completed successfully")
                .build();

        return ProcessOrderUtility.productionLog(productionLogRequest);
    }

    private boolean logProductionLogForScrap(BatchNoScrapRequest batchNoScrapRequest) throws Exception {
        ProductionLogRequest productionLogRequest = ProductionLogRequest.builder()
                .site(batchNoScrapRequest.getSite())
                .eventType("ScrapSFC")
                .userId(batchNoScrapRequest.getUser())
                .batchNo(batchNoScrapRequest.getOrderNumber() + "_" + batchNoScrapRequest.getBatchNo())
                .orderNumber(batchNoScrapRequest.getOrderNumber())
                .operation(batchNoScrapRequest.getOperation())
                .phaseId(batchNoScrapRequest.getPhaseId())
                .workcenterId(batchNoScrapRequest.getWorkcenter())
                .resourceId(batchNoScrapRequest.getResource())
                .material(batchNoScrapRequest.getMaterial())
                .materialVersion(batchNoScrapRequest.getMaterialVersion())
                .shopOrderBO(batchNoScrapRequest.getOrderNumber())
                .qty(batchNoScrapRequest.getScrapQuantity().intValue())
                .reasonCode(batchNoScrapRequest.getReasonCode())
                .topic("production-log")
                .status("Active")
                .createdDatetime(LocalDateTime.now())
                .eventData(batchNoScrapRequest.getBatchNo() + " Scrapped successfully")
                .build();

        return ProcessOrderUtility.productionLog(productionLogRequest);
    }

    private boolean logProductionLogForUnScrap(BatchNoScrapRequest batchNoScrapRequest) throws Exception {
        ProductionLogRequest productionLogRequest = ProductionLogRequest.builder()
                .site(batchNoScrapRequest.getSite())
                .eventType("BATCH-UNSCRAP")
                .userId(batchNoScrapRequest.getUser())
                .batchNo(batchNoScrapRequest.getBatchNo())
                .orderNumber(batchNoScrapRequest.getOrderNumber())
                .operation(batchNoScrapRequest.getOperation())
                .phaseId(batchNoScrapRequest.getPhaseId())
                .workcenterId(batchNoScrapRequest.getWorkcenter())
                .resourceId(batchNoScrapRequest.getResource())
                .material(batchNoScrapRequest.getMaterial())
                .materialVersion(batchNoScrapRequest.getMaterialVersion())
                .qty(Optional.ofNullable(batchNoScrapRequest.getScrapQuantity())
                        .map(scrapQuantity -> scrapQuantity.setScale(0, RoundingMode.HALF_UP).intValue())
                        .orElse(0))
                .topic("production-log")
                .status("Active")
                .createdDatetime(LocalDateTime.now())
                .eventData(batchNoScrapRequest.getBatchNo() + " Unscrapped successfully")
                .build();

        return ProcessOrderUtility.productionLog(productionLogRequest);
    }

    @Override
    public BatchNoScrapQtyResponse getBatchNoScrapByPhaseAndOperation(BatchNoScrapRequest request) {
        BatchNoScrapQtyResponse batchNoScrap = null;

        // If both batchNo and orderNumber are provided and are not blank
        if ((request.getBatchNo() != null && !request.getBatchNo().isEmpty()) &&
                (request.getOrderNumber() != null && !request.getOrderNumber().isEmpty())) {
            batchNoScrap = batchNoScrapRepository.findBySiteAndBatchNoAndOrderNumberAndPhaseIdAndOperation(
                    request.getSite(), request.getBatchNo(), request.getOrderNumber(), request.getPhaseId(), request.getOperation());
        }
        // If only batchNo is provided and is not blank
        else if (request.getBatchNo() != null && !request.getBatchNo().isEmpty()) {
            batchNoScrap = batchNoScrapRepository.findBySiteAndBatchNoAndPhaseIdAndOperation(
                    request.getSite(), request.getBatchNo(), request.getPhaseId(), request.getOperation());
        }
        // If only orderNumber is provided and is not blank
        else if (request.getOrderNumber() != null && !request.getOrderNumber().isEmpty()) {
            batchNoScrap = batchNoScrapRepository.findBySiteAndOrderNumberAndPhaseIdAndOperation(
                    request.getSite(), request.getOrderNumber(), request.getPhaseId(), request.getOperation());
        }
        return batchNoScrap;
    }

    public BigDecimal getTotalScrapQty(String site, String orderNo, String batchNo) {
        List<BatchNoScrap> batchNoScrapList = batchNoScrapRepository.findBySiteAndOrderNumberAndBatchNoAndActive(site, orderNo, batchNo, 1);

        if (batchNoScrapList == null || batchNoScrapList.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalScrapQty =  batchNoScrapList.stream()
                .map(BatchNoScrap::getScrapQuantity)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return totalScrapQty;
    }

}
