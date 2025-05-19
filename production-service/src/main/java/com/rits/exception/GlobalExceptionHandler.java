package com.rits.exception;

import com.rits.activityhookservice.exception.ActivityHookException;
import com.rits.assemblyservice.exception.AssemblyException;
import com.rits.batchnocomplete.exception.BatchNoCompleteException;
import com.rits.batchnodoneservice.exception.BatchNoDoneException;
import com.rits.batchnoheader.exception.BatchNoHeaderException;
import com.rits.batchnohold.exception.BatchNoHoldException;
import com.rits.batchnoinqueue.exception.BatchNoInQueueException;
import com.rits.batchnoinwork.exception.BatchNoInWorkException;
import com.rits.batchnophaseprogressservice.exception.BatchNoPhaseProgressException;
import com.rits.batchnorecipeheaderservice.exception.BatchNoRecipeHeaderException;
import com.rits.batchnoscrap.exception.BatchNoScrapException;
import com.rits.batchnoyieldreportingservice.exception.BatchNoYieldReportingException;
import com.rits.bomheaderservice.exception.BomHeaderException;
import com.rits.changeproductionservice.exception.ChangeProductionException;
import com.rits.checkhook.exception.CheckHookException;
import com.rits.customdataformatservice.exception.CustomDataFormatException;
import com.rits.customhookservice.exception.CustomHookException;
import com.rits.dccollect.exception.DcCollectException;
import com.rits.inventoryservice.exception.InventoryException;
import com.rits.lineclearancelogservice.exception.LineClearanceLogException;
import com.rits.lineclearanceservice.exception.LineClearanceException;
import com.rits.listmaintenceservice.exception.ListMaintenanceException;
import com.rits.logbuyoffservice.exception.LogBuyOffException;
import com.rits.nextnumbergeneratorservice.exception.NextNumberGeneratorException;
import com.rits.pcucompleteservice.exception.PcuCompleteException;
import com.rits.pcudoneservice.exception.PcuDoneException;
import com.rits.pcuheaderservice.exception.PcuHeaderException;
import com.rits.pcuinqueueservice.exception.PcuInQueueException;
import com.rits.pcurouterheaderservice.exception.PcuRouterHeaderException;
import com.rits.pcustepstatus.exception.PcuStepStatusException;
import com.rits.processorderservice.exception.ProcessOrderException;
import com.rits.processorderstateservice.exception.ProcessOrderStateException;
import com.rits.productionlogservice.exception.ProductionLogException;
import com.rits.qualityacceptanceservice.exception.QualityAcceptanceException;
import com.rits.schedulerconfigservice.exception.SchedulerException;
import com.rits.shoporderrelease.exception.ShopOrderReleaseException;
import com.rits.shoporderservice.exception.ShopOrderException;
import com.rits.startservice.exception.StartException;
import com.rits.stepstatusservice.exception.BatchStepStatusException;
import com.rits.toollogservice.exception.ToolLogException;
import com.rits.worklistservice.exception.WorkListException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.Locale;

@ControllerAdvice
public class GlobalExceptionHandler {

    @Autowired
    private MessageSource messageSource;

    @ExceptionHandler(NextNumberGeneratorException.class)
    public ResponseEntity<ErrorDetails> handleNextNumberGeneratorException(NextNumberGeneratorException ex, WebRequest request) {
        String errorMessage = messageSource.getMessage(String.valueOf(ex.getCode()), ex.getArgs(), Locale.getDefault());
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), errorMessage, request.getDescription(false), String.valueOf(ex.getCode()));
        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
    }

    @ExceptionHandler(StartException.class)
    public ResponseEntity<ErrorDetails> handleStartException(StartException ex, WebRequest request) {
        String errorMessage = messageSource.getMessage(String.valueOf(ex.getCode()), ex.getArgs(), Locale.getDefault());
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), errorMessage, request.getDescription(false), String.valueOf(ex.getCode()));
        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
    }

    @ExceptionHandler(PcuCompleteException.class)
    public ResponseEntity<ErrorDetails> handlePcuCompleteException(PcuCompleteException ex, WebRequest request) {
        String errorMessage = messageSource.getMessage(String.valueOf(ex.getCode()), ex.getArgs(), Locale.getDefault());
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), errorMessage, request.getDescription(false), String.valueOf(ex.getCode()));
        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
    }
    @ExceptionHandler(PcuStepStatusException.class)
    public ResponseEntity<ErrorDetails> handlePcuStepStatusException(PcuStepStatusException ex, WebRequest request) {
        String errorMessage = messageSource.getMessage(String.valueOf(ex.getCode()), ex.getArgs(), Locale.getDefault());
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), errorMessage, request.getDescription(false), String.valueOf(ex.getCode()));
        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
    }

//    @ExceptionHandler(SignOffException.class)
//    public ResponseEntity<ErrorDetails> handleSignOffException(SignOffException ex, WebRequest request) {
//        String errorMessage = messageSource.getMessage(String.valueOf(ex.getCode()), ex.getArgs(), Locale.getDefault());
//        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), errorMessage, request.getDescription(false), String.valueOf(ex.getCode()));
//        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
//    }

    @ExceptionHandler(PcuDoneException.class)
    public ResponseEntity<ErrorDetails> handlePcuDoneException(PcuDoneException ex, WebRequest request) {
        String errorMessage = messageSource.getMessage(String.valueOf(ex.getCode()), ex.getArgs(), Locale.getDefault());
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), errorMessage, request.getDescription(false), String.valueOf(ex.getCode()));
        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
    }

    @ExceptionHandler(AssemblyException.class)
    public ResponseEntity<ErrorDetails> handleAssemblyException(AssemblyException ex, WebRequest request) {
        String errorMessage = messageSource.getMessage(String.valueOf(ex.getCode()), ex.getArgs(), Locale.getDefault());
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), errorMessage, request.getDescription(false), String.valueOf(ex.getCode()));
        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
    }

    @ExceptionHandler(DcCollectException.class)
    public ResponseEntity<ErrorDetails> handleDcCollectException(DcCollectException ex, WebRequest request) {
        String errorMessage = messageSource.getMessage(String.valueOf(ex.getCode()), ex.getArgs(), Locale.getDefault());
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), errorMessage, request.getDescription(false), String.valueOf(ex.getCode()));
        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
    }

    @ExceptionHandler(BomHeaderException.class)
    public ResponseEntity<ErrorDetails> handleBomHeaderException(BomHeaderException ex, WebRequest request) {
        String errorMessage = messageSource.getMessage(String.valueOf(ex.getCode()), ex.getArgs(), Locale.getDefault());
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), errorMessage, request.getDescription(false), String.valueOf(ex.getCode()));
        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
    }

    @ExceptionHandler(ListMaintenanceException.class)
    public ResponseEntity<ErrorDetails> handleListMaintenanceException(ListMaintenanceException ex, WebRequest request) {
        String errorMessage = messageSource.getMessage(String.valueOf(ex.getCode()), ex.getArgs(), Locale.getDefault());
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), errorMessage, request.getDescription(false), String.valueOf(ex.getCode()));
        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
    }

    @ExceptionHandler(PcuHeaderException.class)
    public ResponseEntity<ErrorDetails> handlePcuHeaderException(PcuHeaderException ex, WebRequest request) {
        String errorMessage = messageSource.getMessage(String.valueOf(ex.getCode()), ex.getArgs(), Locale.getDefault());
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), errorMessage, request.getDescription(false), String.valueOf(ex.getCode()));
        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
    }

    @ExceptionHandler(PcuInQueueException.class)
    public ResponseEntity<ErrorDetails> handlePcuInQueueException(PcuInQueueException ex, WebRequest request) {
        String errorMessage = messageSource.getMessage(String.valueOf(ex.getCode()), ex.getArgs(), Locale.getDefault());
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), errorMessage, request.getDescription(false), String.valueOf(ex.getCode()));
        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
    }

    @ExceptionHandler(PcuRouterHeaderException.class)
    public ResponseEntity<ErrorDetails> handlePcuRouterHeaderException(PcuRouterHeaderException ex, WebRequest request) {
        String errorMessage = messageSource.getMessage(String.valueOf(ex.getCode()), ex.getArgs(), Locale.getDefault());
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), errorMessage, request.getDescription(false), String.valueOf(ex.getCode()));
        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
    }
    @ExceptionHandler(WorkListException.class)
    public ResponseEntity<ErrorDetails> handleWorkListException(WorkListException ex, WebRequest request) {
        String errorMessage = messageSource.getMessage(String.valueOf(ex.getCode()), ex.getArgs(), Locale.getDefault());
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), errorMessage, request.getDescription(false), String.valueOf(ex.getCode()));
        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
    }

    @ExceptionHandler(ShopOrderException.class)
    public ResponseEntity<ErrorDetails> handleShopOrderException(ShopOrderException ex, WebRequest request) {
        String errorMessage = messageSource.getMessage(String.valueOf(ex.getCode()), ex.getArgs(), Locale.getDefault());
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), errorMessage, request.getDescription(false), String.valueOf(ex.getCode()));
        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
    }
    @ExceptionHandler(ProcessOrderException.class)
    public ResponseEntity<ErrorDetails> handleProcessOrderException(ProcessOrderException ex, WebRequest request) {
        String errorMessage = messageSource.getMessage(String.valueOf(ex.getCode()), ex.getArgs(), Locale.getDefault());
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), errorMessage, request.getDescription(false), String.valueOf(ex.getCode()));
        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
    }

    @ExceptionHandler(ShopOrderReleaseException.class)
    public ResponseEntity<ErrorDetails> handleShopOrderReleaseException(ShopOrderReleaseException ex, WebRequest request) {
        String errorMessage = messageSource.getMessage(String.valueOf(ex.getCode()), ex.getArgs(), Locale.getDefault());
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), errorMessage, request.getDescription(false), String.valueOf(ex.getCode()));
        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
    }

    @ExceptionHandler(CustomDataFormatException.class)
    public ResponseEntity<ErrorDetails> handleCustomDataFormatException(CustomDataFormatException ex, WebRequest request) {
        String errorMessage = messageSource.getMessage(String.valueOf(ex.getCode()), ex.getArgs(), Locale.getDefault());
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), errorMessage, request.getDescription(false), String.valueOf(ex.getCode()));
        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
    }


    @ExceptionHandler(ChangeProductionException.class)
    public ResponseEntity<ErrorDetails> handleChangeProductionException(ChangeProductionException ex, WebRequest request) {
        String errorMessage = messageSource.getMessage(String.valueOf(ex.getCode()), ex.getArgs(), Locale.getDefault());
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), errorMessage, request.getDescription(false), String.valueOf(ex.getCode()));
        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
    }

    @ExceptionHandler(ToolLogException.class)
    public ResponseEntity<ErrorDetails> handleToolLogException(ToolLogException ex, WebRequest request) {
        String errorMessage = messageSource.getMessage(String.valueOf(ex.getCode()), ex.getArgs(), Locale.getDefault());
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), errorMessage, request.getDescription(false), String.valueOf(ex.getCode()));
        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
    }
    @ExceptionHandler(LogBuyOffException.class)
    public ResponseEntity<ErrorDetails> handleLogBuyOffException(LogBuyOffException ex, WebRequest request) {
        String errorMessage = messageSource.getMessage(String.valueOf(ex.getCode()), ex.getArgs(), Locale.getDefault());
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), errorMessage, request.getDescription(false), String.valueOf(ex.getCode()));
        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
    }
    @ExceptionHandler(CheckHookException.class)
    public ResponseEntity<ErrorDetails> handleCheckHookException(CheckHookException ex, WebRequest request) {
        String errorMessage = messageSource.getMessage(String.valueOf(ex.getCode()), ex.getArgs(), Locale.getDefault());
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), errorMessage, request.getDescription(false), String.valueOf(ex.getCode()));
        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleAllExceptions(Exception ex, WebRequest request) {
        String customMessage = ex.getMessage();
        if (customMessage != null && customMessage.startsWith("java.lang.IllegalArgumentException:")) {
            customMessage = customMessage.substring("java.lang.IllegalArgumentException:".length()).trim();
        }
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), customMessage, request.getDescription(false), "internal_server_error");
        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
    }

    @ExceptionHandler(SchedulerException.class)
    public ResponseEntity<ErrorDetails> handleSchedulerException(SchedulerException ex, WebRequest request) {
        String errorMessage = messageSource.getMessage(String.valueOf(ex.getCode()), ex.getArgs(), Locale.getDefault());
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), errorMessage, request.getDescription(false), String.valueOf(ex.getCode()));
        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
    }

    @ExceptionHandler(BatchNoRecipeHeaderException.class)
    public ResponseEntity<ErrorDetails> handleBatchNoRecipeHeaderException(BatchNoRecipeHeaderException ex, WebRequest request) {
        String errorMessage = messageSource.getMessage(String.valueOf(ex.getCode()), ex.getArgs(), Locale.getDefault());
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), errorMessage, request.getDescription(false), String.valueOf(ex.getCode()));
        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
    }

    @ExceptionHandler(BatchNoHeaderException.class)
    public ResponseEntity<ErrorDetails> handleBatchNoHeaderException(BatchNoHeaderException ex, WebRequest request) {
        String errorMessage = messageSource.getMessage(String.valueOf(ex.getCode()), ex.getArgs(), Locale.getDefault());
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), errorMessage, request.getDescription(false), String.valueOf(ex.getCode()));
        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
    }

    @ExceptionHandler(BatchNoInQueueException.class)
    public ResponseEntity<ErrorDetails> handleBatchNoInQueueException(BatchNoInQueueException ex, WebRequest request) {
        String errorMessage = messageSource.getMessage(String.valueOf(ex.getCode()), ex.getArgs(), Locale.getDefault());
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), errorMessage, request.getDescription(false), String.valueOf(ex.getCode()));
        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
    }
    @ExceptionHandler(BatchNoInWorkException.class)
    public ResponseEntity<ErrorDetails> handleBatchNoInWorkException(BatchNoInWorkException ex, WebRequest request) {
        String errorMessage = messageSource.getMessage(String.valueOf(ex.getCode()), ex.getArgs(), Locale.getDefault());
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), errorMessage, request.getDescription(false), String.valueOf(ex.getCode()));
        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
    }


    @ExceptionHandler(BatchNoCompleteException.class)
    public ResponseEntity<ErrorDetails> handleBatchNoCompleteException(BatchNoCompleteException ex, WebRequest request) {
        String errorMessage = messageSource.getMessage(String.valueOf(ex.getCode()), ex.getArgs(), Locale.getDefault());
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), errorMessage, request.getDescription(false), String.valueOf(ex.getCode()));
        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        String customMessage = ex.getMessage();
        if (customMessage != null && customMessage.startsWith("java.lang.IllegalArgumentException:")) {
            customMessage = customMessage.substring("java.lang.IllegalArgumentException:".length()).trim();
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(customMessage);
    }

    @ExceptionHandler(ProductionLogException.class)
    public ResponseEntity<ErrorDetails> handleProductionLogException(ProductionLogException ex, WebRequest request) {
        String errorMessage = messageSource.getMessage(String.valueOf(ex.getCode()), ex.getArgs(), Locale.getDefault());
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), errorMessage, request.getDescription(false), String.valueOf(ex.getCode()));
        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
    }

    @ExceptionHandler(BatchNoDoneException.class)
    public ResponseEntity<ErrorDetails> handleBatchNoDoneException(BatchNoDoneException ex, WebRequest request) {
        String errorMessage = messageSource.getMessage(String.valueOf(ex.getCode()), ex.getArgs(), Locale.getDefault());
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), errorMessage, request.getDescription(false), String.valueOf(ex.getCode()));
        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
    }

    @ExceptionHandler(BatchNoPhaseProgressException.class)
    public ResponseEntity<ErrorDetails> handleBatchNoPhaseProgressException(BatchNoPhaseProgressException ex, WebRequest request) {
        String errorMessage = messageSource.getMessage(String.valueOf(ex.getCode()), ex.getArgs(), Locale.getDefault());
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), errorMessage, request.getDescription(false), String.valueOf(ex.getCode()));
        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
    }

    @ExceptionHandler(BatchNoYieldReportingException.class)
    public ResponseEntity<ErrorDetails> handleBatchNoYieldReportingException(BatchNoYieldReportingException ex, WebRequest request) {
        String errorMessage = messageSource.getMessage(String.valueOf(ex.getCode()), ex.getArgs(), Locale.getDefault());
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), errorMessage, request.getDescription(false), String.valueOf(ex.getCode()));
        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
    }

    @ExceptionHandler(ProcessOrderStateException.class)
    public ResponseEntity<ErrorDetails> handleProcessOrderStateException(ProcessOrderStateException ex, WebRequest request) {
        String errorMessage = messageSource.getMessage(String.valueOf(ex.getCode()), ex.getArgs(), Locale.getDefault());
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), errorMessage, request.getDescription(false), String.valueOf(ex.getCode()));
        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
    }

    @ExceptionHandler(BatchNoScrapException.class)
    public ResponseEntity<ErrorDetails> handleBatchNoScrapException(BatchNoScrapException ex, WebRequest request) {
        String errorMessage = messageSource.getMessage(String.valueOf(ex.getCode()), ex.getArgs(), Locale.getDefault());
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), errorMessage, request.getDescription(false), String.valueOf(ex.getCode()));
        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
    }

    @ExceptionHandler(LineClearanceException.class)
    public ResponseEntity<ErrorDetails> handleLineClearanceException(LineClearanceException ex, WebRequest request) {
        String errorMessage = messageSource.getMessage(String.valueOf(ex.getCode()), ex.getArgs(), Locale.getDefault());
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), errorMessage, request.getDescription(false), String.valueOf(ex.getCode()));
        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
    }

    @ExceptionHandler(LineClearanceLogException.class)
    public ResponseEntity<ErrorDetails> handleLineClearanceLogException(LineClearanceLogException ex, WebRequest request) {
        String errorMessage = messageSource.getMessage(String.valueOf(ex.getCode()), ex.getArgs(), Locale.getDefault());
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), errorMessage, request.getDescription(false), String.valueOf(ex.getCode()));
        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
    }

    @ExceptionHandler(QualityAcceptanceException.class)
    public ResponseEntity<ErrorDetails> handleQualityAcceptanceException(QualityAcceptanceException ex, WebRequest request) {
        String errorMessage = messageSource.getMessage(String.valueOf(ex.getCode()), ex.getArgs(), Locale.getDefault());
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), errorMessage, request.getDescription(false), String.valueOf(ex.getCode()));
        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
    }

    @ExceptionHandler(InventoryException.class)
    public ResponseEntity<ErrorDetails> handleToolNumberException(InventoryException ex, WebRequest request) {
        String errorMessage = messageSource.getMessage(String.valueOf(ex.getCode()), ex.getArgs(), Locale.getDefault());
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), errorMessage, request.getDescription(false), String.valueOf(ex.getCode()));
        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
    }

    @ExceptionHandler(BatchNoHoldException.class)
    public ResponseEntity<ErrorDetails> handleBatchNoHoldException(BatchNoHoldException ex, WebRequest request) {
        String errorMessage = messageSource.getMessage(String.valueOf(ex.getCode()), ex.getArgs(), Locale.getDefault());
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), errorMessage, request.getDescription(false), String.valueOf(ex.getCode()));
        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
    }

    @ExceptionHandler(BatchStepStatusException.class)
    public ResponseEntity<ErrorDetails> handleBatchStepStatusException(BatchStepStatusException ex, WebRequest request) {
        String errorMessage = messageSource.getMessage(String.valueOf(ex.getCode()), ex.getArgs(), Locale.getDefault());
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), errorMessage, request.getDescription(false), String.valueOf(ex.getCode()));
        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
    }

    @ExceptionHandler(ActivityHookException.class)
    public ResponseEntity<ErrorDetails> handleActivityHookException(ActivityHookException ex, WebRequest request) {
        String errorMessage = messageSource.getMessage(String.valueOf(ex.getCode()), ex.getArgs(), Locale.getDefault());
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), errorMessage, request.getDescription(false), String.valueOf(ex.getCode()));
        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
    }

    @ExceptionHandler(CustomHookException.class)
    public ResponseEntity<ErrorDetails> handleCustomHookException(CustomHookException ex, WebRequest request) {
        String errorMessage = messageSource.getMessage(String.valueOf(ex.getCode()), ex.getArgs(), Locale.getDefault());
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), errorMessage, request.getDescription(false), String.valueOf(ex.getCode()));
        return ResponseEntity.status(HttpStatus.OK).body(errorDetails);
    }
}
