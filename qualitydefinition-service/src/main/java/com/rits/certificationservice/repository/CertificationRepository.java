package com.rits.certificationservice.repository;

import com.rits.certificationservice.dto.CertificationResponse;
import com.rits.certificationservice.model.Certification;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CertificationRepository extends MongoRepository<Certification, String> {
    boolean existsByActiveAndSiteAndCertification(int active, String site, String certificate);

    public Certification findByActiveAndSiteAndCertification(int active, String site, String certificate);

    public List<CertificationResponse> findByActiveAndSiteAndCertificationContainingIgnoreCase(int active, String site, String certificate);

    public List<CertificationResponse> findByActiveAndSiteOrderByCreatedDateTimeDesc(int active, String site);

    public List<CertificationResponse> findByActiveAndSiteAndStatus(int active, String site, String status);

    public List<CertificationResponse> findByActiveAndSite(int active, String site);
}
