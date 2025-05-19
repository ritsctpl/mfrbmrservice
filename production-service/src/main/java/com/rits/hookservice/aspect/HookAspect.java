package com.rits.hookservice.aspect;

import com.rits.customhookservice.exception.CustomHookException;
import com.rits.hookservice.model.AttachmentPoint;
import com.rits.hookservice.repository.AttachmentPointRepository;
import com.rits.hookservice.service.CustomHook;
import com.rits.hookservice.util.ApplicationContextProvider;
import com.rits.processorderstateservice.exception.ProcessOrderStateException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Aspect
@Component
public class HookAspect {

   @Autowired
   CustomHook customHook;

    @Autowired
    private AttachmentPointRepository attachmentPointRepository;

    @Around("@annotation(com.rits.hookservice.annotation.Hookable)")
    public Object aroundHookable(ProceedingJoinPoint joinPoint) throws Throwable {
        // -------------------------------------------------------------
        // 0) Gather info about the target method
        // -------------------------------------------------------------
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String targetClass = joinPoint.getTarget().getClass().getName();
        String targetMethod = method.getName();
//        customHook.execute(joinPoint);

        // Find all active attachments for this target method
        List<AttachmentPoint> attachments = attachmentPointRepository
                .findByTargetClassAndTargetMethodAndActiveTrue(targetClass, targetMethod);

        // Initial arguments (which may be modified by EXTENSIONs)
        Object[] args = joinPoint.getArgs();

        // -------------------------------------------------------------
        // 1) Process BEFORE attachments
        // -------------------------------------------------------------
        for (AttachmentPoint attachment : attachments) {
            if ("BEFORE".equalsIgnoreCase(attachment.getHookPoint())) {
                Object hookBean = ApplicationContextProvider.getApplicationContext()
                        .getBean(attachment.getHookClass());
                // The hook method must match the target method's parameter types
                Method hookMethod = hookBean.getClass().getMethod(attachment.getHookMethod(), method.getParameterTypes());
                String hookType = attachment.getHookType() != null ? attachment.getHookType() : "HOOK";
                String execMode = attachment.getExecutionMode() != null ? attachment.getExecutionMode() : "SYNC";

                // -----------------------------------------------------
                // 1A) EXTENSION BEFORE
                // -----------------------------------------------------
                if ("EXTENSION".equalsIgnoreCase(hookType)) {
                    try {
                        if ("ASYNC".equalsIgnoreCase(execMode)) {
                            // We must capture args in a final variable for the lambda
                            final Object[] finalArgs = args;
                            // Execute asynchronously, but we .get() so we can block for the result
                            Object modifiedArgs = CompletableFuture.supplyAsync(() -> {
                                try {
                                    return hookMethod.invoke(hookBean, (Object[]) finalArgs);
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }).get();  // block until done

                            if (modifiedArgs instanceof Object[]) {
                                args = (Object[]) modifiedArgs;
                            }
                        } else {
                            // Synchronous call
                            Object modifiedArgs = hookMethod.invoke(hookBean, args);
                            if (modifiedArgs instanceof Object[]) {
                                args = (Object[]) modifiedArgs;
                            }
                        }
                    } catch (Exception ex) {
                        // If an extension fails, stop execution
                        throw ex;
                    }
                }
                // -----------------------------------------------------
                // 1B) HOOK BEFORE
                // -----------------------------------------------------
                else if ("HOOK".equalsIgnoreCase(hookType)) {
                    if ("ASYNC".equalsIgnoreCase(execMode)) {
                        final Object[] finalArgs = args;
                        CompletableFuture.runAsync(() -> {
                            try {
                                hookMethod.invoke(hookBean, (Object[]) finalArgs);
                            } catch (Exception e) {
                                // Async error is logged, does not stop main flow
                                e.printStackTrace();
                            }
                        });
                    } else {
                        // Synchronous HOOK
                        try {
                            hookMethod.invoke(hookBean, args);

                        }  catch (InvocationTargetException ex) {
                            Throwable cause = ex.getCause();
                            if (cause instanceof ProcessOrderStateException) {
                                throw (ProcessOrderStateException) cause;
                            } else if (cause instanceof CustomHookException) {
                                throw (CustomHookException) cause;
                            } else {
                                throw new Exception(cause);
                            }
                        }catch (Exception ex) {
                            // If a sync hook fails, stop execution
                            throw ex;
                        }
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // 2) Proceed with main method (with possibly modified args)
        // -------------------------------------------------------------
        Object result = joinPoint.proceed(args);

        // -------------------------------------------------------------
        // 3) Process AFTER attachments
        // -------------------------------------------------------------
        for (AttachmentPoint attachment : attachments) {
            if ("AFTER".equalsIgnoreCase(attachment.getHookPoint())) {
                Object hookBean = ApplicationContextProvider.getApplicationContext()
                        .getBean(attachment.getHookClass());
                // The hook method must accept a single parameter matching the return type
                Method hookMethod = hookBean.getClass().getMethod(attachment.getHookMethod(), method.getReturnType());
                String hookType = attachment.getHookType() != null ? attachment.getHookType() : "HOOK";
                String execMode = attachment.getExecutionMode() != null ? attachment.getExecutionMode() : "SYNC";

                // -----------------------------------------------------
                // 3A) EXTENSION AFTER
                // -----------------------------------------------------
                if ("EXTENSION".equalsIgnoreCase(hookType)) {
                    try {
                        if ("ASYNC".equalsIgnoreCase(execMode)) {
                            // capture the current result
                            final Object finalResult = result;
                            // run asynchronously but wait for the modified result
                            Object modifiedResult = CompletableFuture.supplyAsync(() -> {
                                try {
                                    return hookMethod.invoke(hookBean, finalResult);
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }).get();

                            result = modifiedResult;
                        } else {
                            // Synchronous extension
                            Object modifiedResult = hookMethod.invoke(hookBean, result);
                            result = modifiedResult;
                        }
                    } catch (Exception ex) {
                        // If an extension fails, stop execution
                        throw ex;
                    }
                }
                // -----------------------------------------------------
                // 3B) HOOK AFTER
                // -----------------------------------------------------
                else if ("HOOK".equalsIgnoreCase(hookType)) {
                    if ("ASYNC".equalsIgnoreCase(execMode)) {
                        final Object finalResult = result;
                        CompletableFuture.runAsync(() -> {
                            try {
                                hookMethod.invoke(hookBean, finalResult);
                            } catch (Exception e) {
                                // Async error is logged, does not stop main flow
                                e.printStackTrace();
                            }
                        });
                    } else {
                        // Synchronous HOOK
                        try {
                            hookMethod.invoke(hookBean, result);
                        } catch (Exception ex) {
                            throw ex;
                        }
                    }
                }
            }
        }

        // Return the final (possibly modified) result
        return result;
    }
}
