package com.rits.ncgroupservice.dto;

import com.rits.ncgroupservice.model.Operation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OperationResponseList {
    private List<Operation> operationList;
}
