package com.rits.ncservice.repository;


import com.rits.ncservice.dto.DispositionRoutingResponse;
import com.rits.ncservice.model.LogNC;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NCRepository extends MongoRepository<LogNC,String> {


}
