package com.rits.assemblyservice.repository;

import com.rits.assemblyservice.model.AssemblyGenealogy;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AssemblyGenealogyRepository extends MongoRepository<AssemblyGenealogy,String> {
}
