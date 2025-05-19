package com.rits.processorderrelease_old.controller;

//import com.rits.processorderrelease.exception.ProcessOrderReleaseException;
//import com.rits.processorderrelease.service.ProcessOrderReleaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
        import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@Controller("processOrderReleaseControllerOld")
@RequiredArgsConstructor
public class ProcessOrderReleaseControllerNotUsed {
    //private final ProcessOrderReleaseService processOrderReleaseService;
    private final ApplicationContext context;
    @PostMapping("/shutdown")
    public void shutdown() {
        System.out.println("Shutting down...");
        SpringApplication.exit(context, () -> 1);
    }
//    @PostMapping("release")
//    public SOReleaseMessageModel release(@RequestBody ReleaseRequestList releaseRequest)
//    {
//        try {
//            return processOrderReleaseService.multiRelease(releaseRequest.getReleaseRequest());
//        } catch (ProcessOrderReleaseException e) {
//            throw e;
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
}

