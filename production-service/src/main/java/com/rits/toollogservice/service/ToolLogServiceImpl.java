package com.rits.toollogservice.service;

import com.rits.toollogservice.dto.ToolLogRequest;
import com.rits.toollogservice.dto.ToolNumber;
import com.rits.toollogservice.dto.ToolNumberRequest;
import com.rits.toollogservice.exception.ToolLogException;
import com.rits.toollogservice.model.MessageDetails;
import com.rits.toollogservice.model.ToolLog;
import com.rits.toollogservice.model.ToolLogMessageModel;
import com.rits.toollogservice.repository.ToolLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ToolLogServiceImpl implements ToolLogService{
    private final ToolLogRepository toolLogRepository;
    private final WebClient.Builder webClientBuilder;
    private final MessageSource localMessageSource;

    @Value("${toolnumber-service.url}/retrieve")
    private String retrieveToolNumberUrl;

    @Value("${toolnumber-service.url}/updateCurrentCount")
    private String updateToolNumberCurrentCountUrl;


    public String getFormattedMessage(int code, Object... args) {
        return localMessageSource.getMessage(String.valueOf(code), args, Locale.getDefault());
    }

    @Override
    public ToolLogMessageModel logTool(ToolLogRequest toolLogRequest) throws Exception
    {
        ToolLog log = null;
        ToolNumber retrievedToolNumber = retrieveToolNumber(toolLogRequest.getSite(),toolLogRequest.getToolNumber());
        if(retrievedToolNumber !=null && retrievedToolNumber.getToolNumber() != null)
        {
            if(toolLogRequest.getLoggedQty()>retrievedToolNumber.getQtyAvailable())
            {
                throw new ToolLogException(5203);
            }
            Boolean isCalibration = validateCalibration(retrievedToolNumber);
            if(LocalDateTime.now().isAfter(retrievedToolNumber.getDurationExpiration()))
            {
                ToolLog retrieveToolLog = retrieveByToolNumberAndAttachment(toolLogRequest.getSite(),toolLogRequest.getToolNumber(),"pcu_"+toolLogRequest.getPcuBO());
                if(retrieveToolLog != null && retrieveToolLog.getToolNumberBO()!=null)
                {
                    throw new ToolLogException(5202,toolLogRequest.getToolNumber());
                }
                log = toolLogBuilder(toolLogRequest);
                log.setAttachment("pcu_"+toolLogRequest.getPcuBO());
                log.setSite(toolLogRequest.getSite());
            }else{
                ToolLog retrieveToolLog = retrieveByToolNumberAndAttachment(toolLogRequest.getSite(),toolLogRequest.getToolNumber(),"operation_"+toolLogRequest.getOperationBO()+"_"+"resource_"+toolLogRequest.getResourceBO());
                if(retrieveToolLog != null && retrieveToolLog.getToolNumberBO()!=null)
                {
                    throw new ToolLogException(5202,toolLogRequest.getToolNumber());
                }
                log = toolLogBuilder(toolLogRequest);
                log.setAttachment("operation_"+toolLogRequest.getOperationBO()+"_"+"resource_"+toolLogRequest.getResourceBO());
                log.setSite(toolLogRequest.getSite());
            }

        }
        String createdMessage = getFormattedMessage(16, toolLogRequest.getToolNumber());
        ToolLog savedRecord = toolLogRepository.save(log);
        Boolean isToolNumberUpdated = updateToolNumber(toolLogRequest.getSite(),toolLogRequest.getToolNumber(),toolLogRequest.getLoggedQty());
        return ToolLogMessageModel.builder().message_details(new MessageDetails(createdMessage,"S")).response(savedRecord).build();
    }


    public Boolean validateCalibration(ToolNumber toolNumber) throws Exception
    {
        Boolean isValidation = false;
        if(toolNumber.getCalibrationType().equalsIgnoreCase("count"))
        {
            if(toolNumber.getCurrentCalibrationCount()>0)
            {
                isValidation = true;
            }else{
                throw new ToolLogException(5200,toolNumber.getToolNumber());
            }
        }
        if(toolNumber.getCalibrationType().equalsIgnoreCase("time"))
        {
            LocalDateTime expirationDate = LocalDateTime.parse(toolNumber.getExpirationDate());
            if(LocalDateTime.now().isBefore(expirationDate) /*&& LocalDateTime.now().isEqual(expirationDate)*/)
            {
                isValidation = true;
            }else{
                throw new ToolLogException(5201,toolNumber.getToolNumber());
            }
        }
        if(toolNumber.getCalibrationType().equalsIgnoreCase("date"))
        {
            LocalDateTime expirationDate = LocalDateTime.parse(toolNumber.getExpirationDate());
            if(LocalDateTime.now().isBefore(expirationDate) && LocalDateTime.now().isAfter(LocalDateTime.parse(toolNumber.getStartCalibrationDate())/*&& LocalDateTime.now().isEqual(expirationDate)*/))
            {
                isValidation = true;
            }else{
                throw new ToolLogException(5201,toolNumber.getToolNumber());
            }
        }
        return isValidation;
    }

    public ToolLog toolLogBuilder(ToolLogRequest toolLogRequest)
    {
        ToolLog toolLog = ToolLog.builder()
                .toolGroupBO(toolLogRequest.getToolGroupBO())
                .toolNumberBO("ToolNumberBO:"+toolLogRequest.getSite()+","+toolLogRequest.getToolNumber())
                .loggedQty(toolLogRequest.getLoggedQty())
                .pcuBO(toolLogRequest.getPcuBO())
                .itemBO(toolLogRequest.getItemBO())
                .routerBO(toolLogRequest.getRouterBO())
                .operationBO(toolLogRequest.getOperationBO())
                .resourceBO(toolLogRequest.getResourceBO())
                .shopOrderBO(toolLogRequest.getShopOrderBO())
                .workCenterBO(toolLogRequest.getWorkCenterBO())
                .comments(toolLogRequest.getComments())
                .userId(toolLogRequest.getUserId())
                .active(1)
                .build();
        return toolLog;
    }
  public ToolNumber retrieveToolNumber(String site,String toolNumber)
  {
      ToolNumberRequest toolNumberRequest = ToolNumberRequest.builder().site(site).toolNumber(toolNumber).build();
      ToolNumber  retrieveToolNumber = webClientBuilder.build()
              .post()
              .uri(retrieveToolNumberUrl)
              .bodyValue(toolNumberRequest)
              .retrieve()
              .bodyToMono(ToolNumber.class)
              .block();
      return retrieveToolNumber;
  }

    public Boolean updateToolNumber(String site, String toolNumber, int qty)
    {
        ToolNumberRequest toolNumberRequest = ToolNumberRequest.builder().site(site).toolQty(qty).toolNumber(toolNumber).build();
        Boolean  retrieveToolNumber = webClientBuilder.build()
                .post()
                .uri(updateToolNumberCurrentCountUrl)
                .bodyValue(toolNumberRequest)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
        return retrieveToolNumber;
    }

  public ToolLog retrieveByToolNumberAndAttachment(String site, String toolNumber, String attachment)
  {
      ToolLog retrievedRecord = toolLogRepository.findByActiveAndSiteAndToolNumberBOAndAttachment(1,site,"ToolNumberBO:"+site+","+toolNumber,attachment);
      return retrievedRecord;
  }

  @Override
  public List<ToolLog> retrieveBySiteAndPcu(String site, String pcu) throws Exception
  {
      List<ToolLog> retrievedList = toolLogRepository.findByActiveAndSiteAndPcuBO(1,site,"PcuBO:"+site+","+pcu);
      return retrievedList;
  }
}
