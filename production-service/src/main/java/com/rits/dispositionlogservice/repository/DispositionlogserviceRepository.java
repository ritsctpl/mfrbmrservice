package com.rits.dispositionlogservice.repository;

import com.rits.dispositionlogservice.model.DispositionLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface DispositionlogserviceRepository extends MongoRepository<DispositionLog,String> {

    DispositionLog findByPcuBOAndToRoutingBoAndActive(String pcuBO, String toRoutingBo, String active);
}
