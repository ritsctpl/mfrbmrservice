package com.rits.quality.controller;

import com.rits.common.dto.OeeFilterRequest;
import com.rits.quality.dto.*;
import com.rits.quality.model.ProductionQuality;
import com.rits.quality.service.QualityService;
import com.rits.quality.exception.QualityException;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/app/v1/quality-service")
public class QualityController {

    @Autowired
    private QualityService qualityService;

    @PostMapping("/overallQuality")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<OverallQualityResponse> getOverallQuality(@RequestBody OeeFilterRequest request) {

        if(StringUtils.isEmpty(request.getSite()))
            throw new QualityException(1001);

        try{
            return ResponseEntity.ok(qualityService.getOverallQuality(request));
        }catch (QualityException qualityException){
            throw qualityException;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/qualityByTime")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<QualityByTimeResponse> getQualityByTime(@RequestBody OeeFilterRequest request) {

        if(StringUtils.isEmpty(request.getSite()))
            throw new QualityException(1001);

        try{
            return ResponseEntity.ok(qualityService.getQualityByTime(request));
        }catch (QualityException qualityException){
            throw qualityException;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/qualityByShift")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<QualityByShiftResponse> getQualityByShift(@RequestBody OeeFilterRequest request) {
        if(StringUtils.isEmpty(request.getSite()))
            throw new QualityException(1001);

        try{
            return ResponseEntity.ok(qualityService.getQualityByShift(request));
        }catch (QualityException qualityException){
            throw qualityException;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/getQualityByWorkcenter")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<ProductionQualityDTO>> getQualityByWorkcenter(@RequestBody OeeFilterRequest request) {

        if(StringUtils.isEmpty(request.getSite()))
            throw new QualityException(1001);

        try{
            return ResponseEntity.ok(qualityService.getQualityByWorkcenter(request));
        }catch (QualityException qualityException){
            throw qualityException;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

//    @PostMapping("/getQualityByItem")
//    @ResponseStatus(HttpStatus.OK)
//    public ResponseEntity<List<ProductionQualityDTO>> getQualityByItem(@RequestBody OeeFilterRequest request) {
//        if(StringUtils.isEmpty(request.getSite()))
//            throw new QualityException(1001);
//        try{
//            return ResponseEntity.ok(qualityService.getQualityByItem(request));
//        }catch (QualityException qualityException){
//            throw qualityException;
//        }catch (Exception e){
//            throw new RuntimeException(e);
//        }
//    }

    @PostMapping("/getQualityByDateRange")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<ProductionQualityDTO>> getQualityByDateRange(@RequestBody OeeFilterRequest request) {
        if(StringUtils.isEmpty(request.getSite()))
            throw new QualityException(1001);
        try{
            return ResponseEntity.ok(qualityService.getQualityByDateRange(request));
        }catch (QualityException qualityException){
            throw qualityException;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/getQualityByDateTime")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<ProductionQuality>> getQualityByDateTime(@RequestBody OeeFilterRequest request) {
        if(StringUtils.isEmpty(request.getSite()))
            throw new QualityException(1001);
        try{
            return ResponseEntity.ok(qualityService.getQualityByDateTime(request));
        }catch (QualityException qualityException){
            throw qualityException;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/getQualityByCombination")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<ProductionQuality>> getQualityByCombination(@RequestBody OeeFilterRequest request) {

        if(StringUtils.isEmpty(request.getSite()))
            throw new QualityException(1001);

        try{
            return ResponseEntity.ok(qualityService.getQualityByCombination(request));
        }catch (QualityException qualityException){
            throw qualityException;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

//    @PostMapping("/getQualityByResource")
//    @ResponseStatus(HttpStatus.OK)
//    public ResponseEntity<List<ProductionQualityDTO>> getQualityByResource(@RequestBody OeeFilterRequest request) {
//        if(StringUtils.isEmpty(request.getSite()) || request.getResourceId() == null || request.getResourceId().isEmpty())
//            throw new QualityException(1015);
//
//        try{
//            return ResponseEntity.ok(qualityService.getQualityByResource(request));
//        }catch (QualityException qualityException){
//            throw qualityException;
//        }catch (Exception e){
//            throw new RuntimeException(e);
//        }
//    }

    @PostMapping("/qualityByMachine")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<QualityByMachineResponse> getQualityByMachine(@RequestBody OeeFilterRequest request) {

        if(StringUtils.isEmpty(request.getSite()))
            throw new QualityException(1001);

        try{
            return ResponseEntity.ok(qualityService.getQualityByMachine(request));
        }catch (QualityException qualityException){
            throw qualityException;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/qualityByProduct")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<QualityByProductResponse> getQualityByProduct(@RequestBody OeeFilterRequest request) {

        if(StringUtils.isEmpty(request.getSite()))
            throw new QualityException(1001);

        try{
            return ResponseEntity.ok(qualityService.getQualityByProduct(request));
        }catch (QualityException qualityException){
            throw qualityException;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/defectsByReason")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<DefectsByReasonResponse> getDefectsByReason(@RequestBody OeeFilterRequest request) {

        if(StringUtils.isEmpty(request.getSite()))
            throw new QualityException(1001);

        try{
            return ResponseEntity.ok(qualityService.getDefectsByReason(request));
        }catch (QualityException qualityException){
            throw qualityException;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/qualityLossByProductionLine")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<QualityLossByProductionLineResponse> getQualityLossByProductionLine(@RequestBody OeeFilterRequest request) {

        if(StringUtils.isEmpty(request.getSite()))
            throw new QualityException(1001);

        try{
            return ResponseEntity.ok(qualityService.getQualityLossByProductionLine(request));
        }catch (QualityException qualityException){
            throw qualityException;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

//    @PostMapping("/qualityByOperator")
//    public QualityByOperatorResponse getQualityByOperator(@RequestBody OeeFilterRequest request) {
//        return qualityService.getQualityByOperator(request);
//    }

    @PostMapping("/defectDistributionByProduct")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<DefectByProductResponse> getDefectByProduct(@RequestBody OeeFilterRequest request) {

        if(StringUtils.isEmpty(request.getSite()))
            throw new QualityException(1001);

        try{
            return ResponseEntity.ok(qualityService.getDefectByProduct(request));
        }catch (QualityException qualityException){
            throw qualityException;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/defectTrendByTime")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<DefectByTimeResponse> getDefectByTime(@RequestBody OeeFilterRequest request) {

        if(StringUtils.isEmpty(request.getSite()))
            throw new QualityException(1001);

        try{
            return ResponseEntity.ok(qualityService.getDefectByTime(request));
        }catch (QualityException qualityException){
            throw qualityException;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/getGoodVsBadQtyForResource")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GoodVsBadQtyForResourceResponse> getGoodVsBadQtyForResource(@RequestBody OeeFilterRequest request) {

        if(StringUtils.isEmpty(request.getSite()))
            throw new QualityException(1001);

        try{
            return ResponseEntity.ok(qualityService.getGoodVsBadQtyForResource(request));
        }catch (QualityException qualityException){
            throw qualityException;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    /*@PostMapping("/getScrapAndReworkTrend")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ScrapAndReworkTrendResponse> getScrapAndReworkTrend(@RequestBody OeeFilterRequest request) {

        if(StringUtils.isEmpty(request.getSite()))
            throw new QualityException(1001);

        try{
            return ResponseEntity.ok(qualityService.getScrapAndReworkTrend(request));
        }catch (QualityException qualityException){
            throw qualityException;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }*/

    @PostMapping("/calculateQuality")
    public Boolean calculateQuality(@RequestBody OeeFilterRequest qualityRequest) {

        if(StringUtils.isEmpty(qualityRequest.getSite()))
            throw new QualityException(1003);

        try{
            return qualityService.calculateQuality(qualityRequest);
        }catch (QualityException qualityException){
            throw qualityException;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
