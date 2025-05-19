package com.rits.bomservice.service;

import com.rits.bomservice.Exception.BomException;
import com.rits.bomservice.dto.*;
import com.rits.bomservice.model.*;
import com.rits.bomservice.repository.BomRepository;
import com.rits.containermaintenanceservice.dto.AuditLogRequest;
import com.rits.itemservice.dto.IsExistRequest;
import com.rits.itemservice.dto.ItemRequest;
import com.rits.itemservice.exception.ItemException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class BomServiceImpl implements BomService {
    private final BomRepository bomRepository;
    private final WebClient.Builder webClientBuilder;
    private final MessageSource localMessageSource;

    @Value("${item-service.url}/isExist")
    private String isItemExistUrl;
    @Value("${datatype-service.url}/isExist")
    private String isDataTypeExistUrl;
    @Value("${operation-service.url}/isExist")
    private String isOperationExistUrl;
    @Value("${extension-service.url}/addExtension")
    private String extensionUrl;

    public String getFormattedMessage(int code, Object... args) {
        return localMessageSource.getMessage(String.valueOf(code), args, Locale.getDefault());
    }

    @Override
    public BomResponseList getBomListByCreationDate(String site) throws Exception {
        List<BomResponse> existing = bomRepository.findTop50ByActiveAndSiteOrderByCreatedDateTimeDesc(1, site);
        //db.item.find({}).sort({createdDateTime: -1}).limit(50)
        return BomResponseList.builder().bomList(existing).build();

    }

    @Override
    public BomResponseList getBomList(String bom, String site) throws Exception {
        List<BomResponse> bomResponses;
        if (bom != null && !bom.isEmpty()) {
            bomResponses = bomRepository.findByActiveAndSiteAndBomContainingIgnoreCase(1, site, bom);

            if (bomResponses.isEmpty()) {
                throw new BomException(202, bom, "currentVersion");
            }
            return BomResponseList.builder().bomList(bomResponses).build();
        } else {
            return getBomListByCreationDate(site);
        }
    }

    @Override
    public BomMessageModel createBom(BomRequest bomRequest) throws Exception {


        if (bomRepository.existsByActiveAndSiteAndBomAndRevision(1, bomRequest.getSite(), bomRequest.getBom(), bomRequest.getRevision())) {
            return updateBom(bomRequest);
        } else {
            try {
                validateBomRequest(bomRequest);
            } catch (BomException bomException) {
                throw bomException;
            } catch (Exception e) {
                throw e;
            }
            // Update existing BOM versions if the current version is true
            updateExistingBomVersions(bomRequest);

            Bom bom = createBomEntity(bomRequest);
            String createdMessage = getFormattedMessage(20, bomRequest.getBom(), bomRequest.getRevision());


            return BomMessageModel.builder()
                    .message_details(new MessageDetails(createdMessage, "S"))
                    .response(bomRepository.save(bom))
                    .build();
        }
    }

    private void validateBomRequest(BomRequest bomRequest) throws Exception {
        if (bomRequest.getUserId() == null || bomRequest.getUserId().isEmpty()) {
            throw new BomException(107, bomRequest.getBom());
        }
        if (bomRequest.getRevision() == null || bomRequest.getRevision().isEmpty()) {
            throw new BomException(108, bomRequest.getBom());
        }
        validateAssyOperations(bomRequest);
        validateComponents(bomRequest);
        setDefaultDescriptionIfEmpty(bomRequest);
        setValidFromToNullIfEmpty(bomRequest);
        validateDataType(bomRequest);
    }
    private void validateDataType(BomRequest bomRequest) throws Exception {
        if (bomRequest.getBomComponentList() != null) {
            for (BomComponent bomComponent : bomRequest.getBomComponentList()) {
                if (bomComponent.getAssemblyDataTypeBo() != null && !bomComponent.getAssemblyDataTypeBo().isEmpty()) {
                    validateAssemblyDataType(bomComponent.getAssemblyDataTypeBo(), bomRequest.getSite());
                }
            }
        }

    }

    private void validateAssemblyDataType(String dataType, String site) {
        if (dataType != null && !dataType.isEmpty()) {
            IsExistRequest isExistRequest = IsExistRequest.builder().site(site).category("Assembly").dataType(dataType).build();
            Boolean isDatatypeExist = isDataTypeExist(isExistRequest);
            if (!isDatatypeExist) {
                throw new BomException(111, dataType);
            }
        }
    }
    private Boolean isDataTypeExist(IsExistRequest isExistRequest) {
        return webClientBuilder.build()
                .post()
                .uri(isDataTypeExistUrl)
                .bodyValue(isExistRequest)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
    }

    private void validateAssyOperations(BomRequest bomRequest) {
        if (bomRequest.getBomComponentList() != null) {
            for (BomComponent bomComponent : bomRequest.getBomComponentList()) {
                if (bomComponent.getAssyOperation() != null && !bomComponent.getAssyOperation().isEmpty()) {
                    validateOperationExistence(bomComponent.getAssyOperation(), bomRequest.getSite());
                }
            }
        }
    }

    private void validateOperationExistence(String operation, String site) {
        IsExist isOperationExist = IsExist.builder().operation(operation).site(site).build();
        Boolean operationExist = webClientBuilder.build()
                .post()
                .uri(isOperationExistUrl)
                .bodyValue(isOperationExist)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
        if (!operationExist) {
            throw new BomException(1000, operation);
        }
    }

    private void validateComponents(BomRequest bomRequest) {
        if (bomRequest.getBomComponentList() != null) {
            for (BomComponent bomComponent : bomRequest.getBomComponentList()) {
                validateComponentExistence(bomComponent.getComponent(), bomComponent.getComponentVersion(), bomRequest.getSite());

                for (AlternateComponent alternateComponent : bomComponent.getAlternateComponentList()) {
                    if (alternateComponent != null && alternateComponent.getAlternateComponent() != null && !alternateComponent.getAlternateComponent().isEmpty()) {
                        validateComponentExistence(alternateComponent.getAlternateComponent(), alternateComponent.getAlternateComponentVersion(), bomRequest.getSite());
                    }
                }
            }
        }
    }

    private void validateComponentExistence(String item, String revision, String site) {
        IsExist isExist = IsExist.builder().site(site).item(item).revision(revision).build();
        Boolean componentExist = webClientBuilder.build()
                .post()
                .uri(isItemExistUrl)
                .bodyValue(isExist)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
        if (!componentExist) {
            throw new BomException(203, item, revision);
        }
    }

    private void setDefaultDescriptionIfEmpty(BomRequest bomRequest) {
        if (bomRequest.getDescription() == null || bomRequest.getDescription().isEmpty()) {
            bomRequest.setDescription(bomRequest.getBom());
        }
    }

    private void setValidFromToNullIfEmpty(BomRequest bomRequest) {
        if (bomRequest.getValidFrom()!= null && bomRequest.getValidFrom().equals("")) {
            bomRequest.setValidFrom(null);
        }
        if (bomRequest.getValidTo()!= null && bomRequest.getValidTo().equals("")) {
            bomRequest.setValidTo(null);
        }
    }

    private void updateExistingBomVersions(BomRequest bomRequest) {
        if (bomRequest.isCurrentVersion()) {
            List<Bom> existing = bomRepository.findByActiveAndSiteAndBom(1, bomRequest.getSite(), bomRequest.getBom());

            existing.stream()
                    .filter(Bom::isCurrentVersion)
                    .map(existingItem -> {
                        existingItem.setCurrentVersion(false);
                        existingItem.setModifiedBy(bomRequest.getUserId());
                        existingItem.setModifiedDateTime(LocalDateTime.now());
                        return existingItem;
                    }).forEach(bomRepository::save);
        }
    }

    private Bom createBomEntity(BomRequest bomRequest) {
        return Bom.builder()
                .site(bomRequest.getSite())
                .bom(bomRequest.getBom())
                .bomType(bomRequest.getBomType())
                .revision(bomRequest.getRevision())
                .description(bomRequest.getDescription())
                .status(bomRequest.getStatus())
                .currentVersion(bomRequest.isCurrentVersion())
                .validFrom(bomRequest.getValidFrom())
                .bomTemplate(bomRequest.isBomTemplate())
                .isUsed(bomRequest.isUsed())
                .active(1)
                .createdBy(bomRequest.getUserId())
                .validTo(bomRequest.getValidTo())
                .createdDateTime(LocalDateTime.now())
                .designCost(bomRequest.getDesignCost())
                .bomComponentList(bomRequest.getBomComponentList())
                .bomCustomDataList(bomRequest.getBomCustomDataList())
                .handle("BomBo:" + bomRequest.getSite() + "," + bomRequest.getBom() + "," + bomRequest.getRevision())
                .build();
    }

    @Override
    public BomMessageModel deleteBom(String bom, String revision, String site, String userId) throws Exception {
        if (userId == null || userId.isEmpty()) {
            throw new BomException(107, bom);
        }
        if (bomRepository.existsByActiveAndSiteAndBomAndRevision(1, site, bom, revision)) {
            if (!isBomUsed(bom, revision, site)) {
                Bom existingBom = bomRepository.findByActiveAndSiteAndBomAndRevision(1, site, bom, revision);
                existingBom.setActive(0);
                existingBom.setModifiedBy(userId);
                existingBom.setModifiedDateTime(LocalDateTime.now());
                bomRepository.save(existingBom);
                String deletedMessage = getFormattedMessage(22, bom, revision);
                return BomMessageModel.builder().message_details(new MessageDetails(deletedMessage, "S")).build();

            } else {
                throw new BomException(205);
            }

        } else {
            throw new BomException(202, bom, revision);
        }
    }

    @Override
    public boolean isBomUsed(String bom, String revision, String site) throws Exception {
        if (bomRepository.existsByActiveAndSiteAndBomAndRevision(1, site, bom, revision)) {
            Bom bomType = bomRepository.findByActiveAndSiteAndBomAndRevision(1, site, bom, revision);
            return bomType.isUsed();
        } else {
            throw new BomException(202, bom, revision);
        }
    }

    @Override
    public BomMessageModel updateBom(BomRequest bomRequest) throws Exception {
        if (bomRepository.existsByActiveAndSiteAndBomAndRevision(1, bomRequest.getSite(), bomRequest.getBom(), bomRequest.getRevision())) {
            Bom existingBom = bomRepository.findByActiveAndSiteAndBomAndRevision(1, bomRequest.getSite(), bomRequest.getBom(), bomRequest.getRevision());
            if (isBomUsed(bomRequest.getBom(), bomRequest.getRevision(), bomRequest.getSite())) {
                throw new BomException(205, bomRequest.getBom(), bomRequest.getRevision());
            } else {
                try {
                    validateBomRequest(bomRequest);
                } catch (BomException bomException) {
                    throw bomException;
                } catch (Exception e) {
                    throw e;
                }
                // Update existing BOM versions if the current version is true
                updateExistingBomVersions(bomRequest);
                Bom updatedBom = createUpdatedBomEntity(existingBom, bomRequest);
                String updatedMessage = getFormattedMessage(21, bomRequest.getBom(), bomRequest.getRevision());

                return BomMessageModel.builder()
                        .message_details(new MessageDetails(updatedMessage, "S"))
                        .response(bomRepository.save(updatedBom))
                        .build();
            }
        } else {
            throw new BomException(202, bomRequest.getBom(), bomRequest.getRevision());
        }
    }

    private Bom createUpdatedBomEntity(Bom existingBom, BomRequest bomRequest) {
        return Bom.builder()
                .site(existingBom.getSite())
                .bom(existingBom.getBom())
                .bomType(bomRequest.getBomType())
                .revision(existingBom.getRevision())
                .description(bomRequest.getDescription())
                .status(bomRequest.getStatus())
                .currentVersion(bomRequest.isCurrentVersion())
                .validFrom(bomRequest.getValidFrom())
                .validTo(bomRequest.getValidTo())
                .bomTemplate(bomRequest.isBomTemplate())
                .isUsed(bomRequest.isUsed())
                .active(1)
                .createdDateTime(existingBom.getCreatedDateTime())
                .modifiedDateTime(LocalDateTime.now())
                .designCost(bomRequest.getDesignCost())
                .bomComponentList(bomRequest.getBomComponentList())
                .bomCustomDataList(bomRequest.getBomCustomDataList())
                .createdBy(existingBom.getCreatedBy())
                .modifiedBy(bomRequest.getUserId())
                .handle(existingBom.getHandle())
                .build();
    }


    @Override
    public BomComponentList getComponentListByOperation(String bom, String revision, String site, String operation) throws Exception {
        if (bomRepository.existsByActiveAndSiteAndBomAndRevision(1, site, bom, revision)) {
            Bom bomType = bomRepository.findByActiveAndSiteAndBomAndRevision(1, site, bom, revision);
            List<BomComponent> bomComponentLists = new ArrayList<>();
            for (BomComponent bomComponent : bomType.getBomComponentList()) {
                if (operation.equalsIgnoreCase(bomComponent.getAssyOperation())) {
                    bomComponentLists.add(bomComponent);
                }
            }
            return BomComponentList.builder().bomComponentList(bomComponentLists).build();
        } else {
            throw new BomException(202, bom, revision);
        }
    }

    @Override
    public BomResponseList componentUsage(String component, String version, String site) throws Exception {
        if (bomRepository.existsByActiveAndSiteAndBomComponentList_ComponentAndBomComponentList_ComponentVersion(1, site, component, version)) {
            List<BomResponse> bomList = bomRepository.findByActiveAndSiteAndBomComponentList_ComponentAndBomComponentList_ComponentVersion(1, site, component, version);
            //db.R_Bom.find({"bomComponentList.component": component, "bomComponentList.version": version})
            if (!bomList.isEmpty()) {
                return BomResponseList.builder().bomList(bomList).build();
            }
        }
        throw new BomException(203, component, version);

    }

    @Override
    public Boolean isBomExist(String bom, String revision, String site) throws Exception {

        if (revision != null && !revision.isEmpty()) {
            return bomRepository.existsByActiveAndSiteAndBomAndRevision(1, site, bom, revision);


        } else {
            return bomRepository.existsByActiveAndSiteAndBomAndCurrentVersion(1, site, bom, true);
        }
    }

    @Override
    public Bom retrieveBom(String bom, String revision, String site) throws Exception {
        Bom existingBom;
        if (revision != null && !revision.isEmpty()) {
            existingBom = bomRepository.findByActiveAndSiteAndBomAndRevision(1, site, bom, revision);
            if (existingBom == null) {
                throw new BomException(202, bom, revision);
            }
        } else {
            existingBom = bomRepository.findByActiveAndSiteAndBomAndCurrentVersion(1, site, bom, true);
            if (existingBom == null) {
                throw new BomException(202, bom, revision);
            }
        }
        return existingBom;
    }

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
            throw new BomException(800);
        }
        return extensionResponse;
    }

    @Override
    public BomResponseList retrieveByBomTypeAndSite(String site, String bomType) throws Exception {
        List<BomResponse> existing = bomRepository.findBySiteAndActiveAndBomType(site, 1, bomType);
        BomResponseList bomResponseList = new BomResponseList();
        if (existing != null && !existing.isEmpty()) {
            bomResponseList.setBomList(existing);
        }
        return bomResponseList;
    }

    @Override
    public AuditLogRequest createAuditLog(BomRequest bomRequest) {
        return AuditLogRequest.builder()
                .site(bomRequest.getSite())
                .action_code("BOM-CREATED " + bomRequest.getDescription())
                .action_detail("Bom Created " + bomRequest.getDescription())
                .action_detail_handle("ActionDetailBO:" + bomRequest.getSite() + "," + "BOM-CREATED" + "," + bomRequest.getUserId() + ":" + "com.rits.bomservice.controller")
                .activity("From Service")
                .date_time(String.valueOf(LocalDateTime.now()))
                .userId(bomRequest.getUserId())
                .txnId("BOM-CREATED" + LocalDateTime.now() + bomRequest.getUserId())
                .created_date_time(String.valueOf(LocalDateTime.now()))
                .category("Create")
                .topic("audit-log")
                .build();
    }

    @Override
    public AuditLogRequest deleteAuditLog(BomRequest bomRequest) {
        return AuditLogRequest.builder()
                .site(bomRequest.getSite())
                .action_code("BOM-DELETED")
                .action_detail("Bom Deleted " + bomRequest.getDescription())
                .action_detail_handle("ActionDetailBO:" + bomRequest.getSite() + "," + "BOM-DELETED" + "," + bomRequest.getUserId() + ":" + "com.rits.bomservice.controller")
                .activity("From Service")
                .date_time(String.valueOf(LocalDateTime.now()))
                .userId(bomRequest.getUserId())
                .txnId("BOM-DELETED" + LocalDateTime.now() + bomRequest.getUserId())
                .created_date_time(String.valueOf(LocalDateTime.now()))
                .category("Delete")
                .build();
    }

    @Override
    public AuditLogRequest updateAuditLog(BomRequest bomRequest) {
        return AuditLogRequest.builder()
                .site(bomRequest.getSite())
                .action_code("BOM-UPDATED " + bomRequest.getDescription())
                .action_detail("Bom Updated")
                .action_detail_handle("ActionDetailBO:" + bomRequest.getSite() + "," + "BOM-UPDATED" + "," + bomRequest.getUserId() + ":" + "com.rits.bomservice.controller")
                .activity("From Service")
                .date_time(String.valueOf(LocalDateTime.now()))
                .userId(bomRequest.getUserId())
                .txnId("BOM-UPDATED" + LocalDateTime.now() + bomRequest.getUserId())
                .created_date_time(String.valueOf(LocalDateTime.now()))
                .category("Update")
                .build();
    }

}
