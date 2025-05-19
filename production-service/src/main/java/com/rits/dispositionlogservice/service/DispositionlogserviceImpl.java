package com.rits.dispositionlogservice.service;

import com.rits.dispositionlogservice.dto.DispositionLogRequest;
import com.rits.dispositionlogservice.model.DispositionLog;
import com.rits.dispositionlogservice.model.MessageDetails;
import com.rits.dispositionlogservice.model.MessageModel;
import com.rits.dispositionlogservice.repository.DispositionlogserviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DispositionlogserviceImpl implements Dispositionlogservice {
    private final DispositionlogserviceRepository dispositionlogserviceRepository;
    private final WebClient.Builder webClientBuilder;
    private final MongoTemplate mongoTemplate;


    @Override
    public MessageModel createDispositionLog(DispositionLogRequest dispositionLogRequest) throws Exception {
        String active="1";
        if(dispositionLogRequest.getActive()!=null&&dispositionLogRequest.getActive().equalsIgnoreCase("0")){
            active="0";
        }
        DispositionLog dispositionLog= DispositionLog.builder()
                .handle(dispositionLogRequest.getPcuBO()+","+"FromRouterBo:"+dispositionLogRequest.getRouterBo()+","+"ToRouterBo:"+dispositionLogRequest.getDispositionRoutingBo()+","+"DispostionTime:"+LocalDateTime.now())
                .pcuBO(dispositionLogRequest.getPcuBO())
                .qty(dispositionLogRequest.getQty())
                .itemBo(dispositionLogRequest.getItemBo())
                .fromoperationBO(dispositionLogRequest.getOperationBO())
                .tooperationBO(dispositionLogRequest.getToOperationBo())
                .workCenterBo(dispositionLogRequest.getWorkCenterBo())
                .fromRoutingBo(dispositionLogRequest.getRouterBo())
                .toRoutingBo(dispositionLogRequest.getDispositionRoutingBo())
                .resourceBo(dispositionLogRequest.getResourceBo())
                .dateTime(LocalDateTime.now().toString())
                .site(dispositionLogRequest.getSite())
                .shopOrderBo(dispositionLogRequest.getShopOrderBo())
                .active(active).build();

        return MessageModel.builder().message_details(new MessageDetails(dispositionLogRequest.getPcuBO() + " created SuccessFully", "S")).response(dispositionlogserviceRepository.save(dispositionLog)).build();

    }
    @Override
    public DispositionLog getActiveRecord(DispositionLogRequest dispositionLogRequest){
        DispositionLog dispositionLog=new DispositionLog();
        dispositionLog=dispositionlogserviceRepository.findByPcuBOAndToRoutingBoAndActive(dispositionLogRequest.getPcuBO(),dispositionLogRequest.getToRoutingBo(),"1");
        return  dispositionLog;
    }


}