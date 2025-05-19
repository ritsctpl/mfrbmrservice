package com.rits.assyservice.controller;

import com.rits.assemblyservice.exception.AssemblyException;
import com.rits.assyservice.model.MessageModel;
import com.rits.assyservice.dto.AssyRequest;
import com.rits.assyservice.model.AssyData;
import com.rits.assyservice.service.AssyService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("app/v1/assy-service")
public class AssyController {
    private final AssyService assyService;


    public AssyController(AssyService assyService) {
        this.assyService = assyService;
    }

    @PostMapping("assemble")
    public boolean assemble(@RequestBody AssyRequest assy) {
        try{
            return assyService.assemble(assy);
        }catch (AssemblyException e){
            throw e;
        }catch (Exception e){
            throw e;
        }
    }
    @PostMapping("remove")
    public boolean remove(@RequestBody AssyRequest assy) throws Exception {
        try{
            return assyService.remove(assy.getSite(),assy.getPcuBO(),assy.getComponentList().get(0));
        }catch (AssemblyException e){
            throw e;
        }catch (Exception e){
            throw e;
        }
    }
    @PostMapping("getAssembly")
    public AssyData getAssembly(@RequestBody AssyRequest assy) throws Exception {
        try{
            return assyService.getTreeStructure(assy.getSite(),assy.getItemBO(),assy.getPcuBO());
        }catch (AssemblyException e){
            throw e;
        }catch (Exception e){
            throw e;
        }
    }
    @PostMapping("retrieve")
    public AssyData retrieveAssembly(@RequestBody AssyRequest assy) throws Exception {
        try{
            return assyService.retrieve(assy.getSite(),assy.getPcuBO());
        }catch (AssemblyException e){
            throw e;
        }catch (Exception e){
            throw e;
        }
    }
    @PostMapping("updateAncestry")
    public AssyData updateAncestry(@RequestBody AssyData assy) throws Exception {
        try{
            return assyService.setAncestry(assy);
        }catch (AssemblyException e){
            throw e;
        }catch (Exception e){
            throw e;
        }
    }
}