
package com.rits.batchnorecipeheaderservice.model;

import com.rits.batchnorecipeheaderservice.dto.*;
import lombok.*;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class MessageModel {
    private BatchNoRecipeHeader response;
    private List<BatchNoRecipeHeader> recipeHeaderList;
    private MessageDetails messageDetails;
    private List<Phase> phaseList;
    private List<Operation> operationList;
    private List<Phase> phases;
    private List<Operation> ops;
    private Operation op;
    private List<AlternateIngredient> alternateIngredients;
    private List<Waste> waste;
    private List<ByProduct> byProducts;
    private Ingredients processIngredient;
    private String opInstruction;
    private Map<String, Object> resultBody;
    private Map<String, String> phaseOps;
    private Ingredients ingredient;
    private PhaseIngredient phaseIngredient;
    private List<PhaseIngredient> phaseIngredientList;
    private String ingredientId;
    private String isVerified;
    private List<QualityControlParameter> qcParameters;
    private List<DataCollection> dataCollection;
    private List<List<DataCollection>> dataCollections;
    private boolean finalValue;
    private DataCollectionList masterDcList;
    private List<BatchRecipeInstruction> masterWorkIns;
    private List<BatchRecipeResponse> batchNos;
}