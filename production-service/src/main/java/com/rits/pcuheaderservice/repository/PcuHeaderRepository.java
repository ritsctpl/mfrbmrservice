package com.rits.pcuheaderservice.repository;

import com.rits.pcuheaderservice.dto.ShopOrder;
import com.rits.pcuheaderservice.model.PcuHeader;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface PcuHeaderRepository extends MongoRepository<PcuHeader,String> {
    List<PcuHeader> findBySiteAndActive(String site,int active);

    PcuHeader findBySiteAndPcuBOAndActiveEquals(String site, String pcuBO, int active);

    Boolean existsBySiteAndActiveAndPcuBOAndRouterListPcuRouterBO(String site, int active, String pcuBO, String pcuRouterBO);

    Boolean existsBySiteAndActiveAndPcuBOAndBomListPcuBomBO(String site,int active,String pcuBO,String pcuBomBO);

    PcuHeader findBySiteAndActiveAndPcuBO(String site, int active, String pcuBO);

    Boolean existsBySiteAndActiveAndPcuBO(String site, int active, String pcuBO);


    List<PcuHeader> findBySiteAndActiveAndShopOrderBO(String site, int i, String shopOrder);

    List<PcuHeader> findBySiteAndActiveAndItemBO(String site, int i, String itemBO);
}
