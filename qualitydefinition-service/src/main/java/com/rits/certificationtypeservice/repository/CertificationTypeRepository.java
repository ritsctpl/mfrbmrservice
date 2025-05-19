package com.rits.certificationtypeservice.repository;

import com.rits.certificationtypeservice.dto.CertificationTypeResponse;
import com.rits.certificationtypeservice.model.CertificationType;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CertificationTypeRepository extends MongoRepository<CertificationType, String> {
    public List<CertificationTypeResponse> findByActiveAndSiteAndCertificationTypeContainingIgnoreCase(int active, String site, String certificationType);

    public List<CertificationTypeResponse> findByActiveAndSiteOrderByCreatedDateTimeDesc(int active, String site);

    public CertificationType findByActiveAndSiteAndCertificationType(int active, String site, String certificationType);

    public boolean existsByActiveAndSiteAndCertificationType(int active, String site, String certificationType);
}
