package com.rits.auditlogservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rits.auditlogservice.dto.AuditLogRequest;
import com.rits.auditlogservice.exception.AuditLogException;
import com.rits.auditlogservice.model.AuditLog;
import com.rits.auditlogservice.repository.AuditLogMongoRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {
    private final AuditLogMongoRepository activityLogMongoRepository;
    private final MongoTemplate mongoTemplate;
    private static final Logger logger = LoggerFactory.getLogger(AuditLogServiceImpl.class);
    @Value("${DOCKER_KAFKA_HOST:localhost}")
    private String dockerKafkaHost;

    @Value("${DOCKER_KAFKA_PORT:9092}")
    private String dockerKafkaPort;

    @Override
    public Boolean log(AuditLogRequest actvityLogRequest) throws Exception {
        int secondsSinceEpoch = (int) LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        AuditLog activityLog = AuditLog.builder()
                .site(actvityLogRequest.getSite())
                .change_type(actvityLogRequest.getChange_type())
                .action_code(actvityLogRequest.getAction_code())
                .action_detail(actvityLogRequest.getAction_detail())
                .action_detail_handle(actvityLogRequest.getAction_detail_handle())
                .activity(actvityLogRequest.getActivity())
                .date_time(actvityLogRequest.getDate_time())
                .crew(actvityLogRequest.getCrew())
                .userId(actvityLogRequest.getUserId())
                .pcu(actvityLogRequest.getPcu())
                .process_lot(actvityLogRequest.getProcess_lot())
                .operation(actvityLogRequest.getOperation())
                .operation_revision(actvityLogRequest.getOperation_revision())
                .item(actvityLogRequest.getItem())
                .item_revision(actvityLogRequest.getItem_revision())
                .router(actvityLogRequest.getRouter())
                .router_revision(actvityLogRequest.getRouter_revision())
                .stepId(actvityLogRequest.getStepId())
                .substepId(actvityLogRequest.getSubstepId())
                .resrce(actvityLogRequest.getResrce())
                .work_center(actvityLogRequest.getWork_center())
                .qty(actvityLogRequest.getQty())
                .rework(actvityLogRequest.getRework())
                .reporting_center_bo(actvityLogRequest.getReporting_center_bo())
                .shop_order_bo(actvityLogRequest.getShop_order_bo())
                .partition_date(actvityLogRequest.getPartition_date())
                .lcc_bo(actvityLogRequest.getLcc_bo())
                .action_span(actvityLogRequest.getAction_span())
                .prev_site(actvityLogRequest.getPrev_site())
                .txnId(actvityLogRequest.getTxnId())
                .created_date_time(actvityLogRequest.getCreated_date_time())
                .modified_date_time(actvityLogRequest.getModified_date_time())
                .category(actvityLogRequest.getCategory())
                .build();
        activityLogMongoRepository.save(activityLog);
        return true;
    }

    @Override
    public void producer(AuditLogRequest activityLog) throws Exception {
        Map<String, Object> producerProps = new HashMap<>();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, dockerKafkaHost + ":" + dockerKafkaPort);
        logger.info("Using Docker Kafka host IP: {}", dockerKafkaHost + ":" + dockerKafkaPort);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "com.rits.auditlogservice.dto.PojoSerializer"); // Use your custom serializer

        try {
            KafkaProducer<String, AuditLogRequest> producer = new KafkaProducer<>(producerProps);
            ProducerRecord<String, AuditLogRequest> record = new ProducerRecord<>("audit-log", activityLog);
            producer.send(record);
            producer.close();
        } catch (Exception e) {
            // Handle any exceptions that may occur
            e.printStackTrace(); // You can replace this with proper error handling
        }
    }

    @Service
    public class KafkaConsumerService {
        @KafkaListener(
                topics = "audit-log",
                groupId = "log-group",
                containerFactory = "kafkaListenerContainerFactory"
        )
        public void consumeMessage(String message) throws Exception {
            AuditLogRequest actvityLogRequest = convertJsonToActivityLogRequest(message);
            log(actvityLogRequest);
        }

    }

    public AuditLogRequest convertJsonToActivityLogRequest(String json) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(json, AuditLogRequest.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<AuditLog> getAuditlogByUser(AuditLogRequest actvityLogRequest) {
        List<AuditLog> auditLogs = new ArrayList<AuditLog>();
        auditLogs = activityLogMongoRepository.findAllBySiteAndUserId(actvityLogRequest.getSite(), actvityLogRequest.getUserId());

        return auditLogs;
    }

    @Override
    public List<AuditLog> getAuditlogByCatagory(AuditLogRequest actvityLogRequest) {
        List<AuditLog> auditLogs = new ArrayList<AuditLog>();
        auditLogs = activityLogMongoRepository.findAllBySiteAndCategory(actvityLogRequest.getSite(), actvityLogRequest.getCategory());

        return auditLogs;
    }

    @Override
    public List<AuditLog> getAuditlogByUserAndCatagory(AuditLogRequest actvityLogRequest) {
        List<AuditLog> auditLogs = new ArrayList<AuditLog>();
        if (!actvityLogRequest.getUserId().isEmpty() || !actvityLogRequest.getUserId().equalsIgnoreCase("null")) {
            auditLogs = activityLogMongoRepository.findAllByAndSiteAndUserIdAndCategory(actvityLogRequest.getSite(), actvityLogRequest.getUserId(), actvityLogRequest.getCategory());
        }
        return auditLogs;
    }

    @Override
    public List<AuditLog> getAuditLogs(AuditLogRequest auditLogRequest) {
        Query query = new Query();

        if (auditLogRequest.getUserId() != null && !auditLogRequest.getUserId().isEmpty()) {
            query.addCriteria(Criteria.where("userId").is(auditLogRequest.getUserId()));
        }

        if (auditLogRequest.getCategory() != null && !auditLogRequest.getCategory().isEmpty()) {
            query.addCriteria(Criteria.where("category").regex(".*" + auditLogRequest.getCategory() + ".*", "i"));
        }

        if (auditLogRequest.getSite() != null && !auditLogRequest.getSite().isEmpty()) {
            query.addCriteria(Criteria.where("site").is(auditLogRequest.getSite()));
        }

        if (auditLogRequest.getChange_type() != null && !auditLogRequest.getChange_type().equalsIgnoreCase("all")) {
            query.addCriteria(Criteria.where("change_type").regex(".*" + auditLogRequest.getChange_type() + ".*", "i"));
        }

        LocalDateTime startDate = null;
        LocalDateTime endDate = null;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        switch (auditLogRequest.getDateRange()) {
                case "24hours":
                    LocalDateTime now = LocalDateTime.now();
                    startDate = StringUtils.isBlank(auditLogRequest.getStart_date()) ? now.minusHours(24) : LocalDateTime.parse(auditLogRequest.getStart_date());
                    endDate = StringUtils.isBlank(auditLogRequest.getEnd_date()) ? now : LocalDateTime.parse(auditLogRequest.getEnd_date());
                    break;

                case "today":
                    startDate = LocalDateTime.now().toLocalDate().atStartOfDay();
                    endDate = LocalDateTime.now();
                    break;
                case "yesterday":
                    startDate = LocalDateTime.now().minusDays(1).with(LocalTime.MIN);
                    endDate = LocalDateTime.now().minusDays(1).with(LocalTime.MAX);
                    break;
                case "thisWeek":
                    startDate = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)).atStartOfDay();
                    endDate = LocalDateTime.now();
                    break;
                case "lastWeek":
                    startDate = LocalDate.now().minusWeeks(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)).atStartOfDay();
                    endDate = LocalDate.now().minusWeeks(1).with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY)).atTime(LocalTime.MAX);
                    break;
                case "thisMonth":
                    startDate = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay();
                    endDate = LocalDateTime.now();
                    break;
                case "lastMonth":
                    startDate = LocalDate.now().minusMonths(1).with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay();
                    endDate = LocalDate.now().minusMonths(1).with(TemporalAdjusters.lastDayOfMonth()).atTime(LocalTime.MAX);
                    break;
                case "thisYear":
                    startDate = LocalDate.now().with(TemporalAdjusters.firstDayOfYear()).atStartOfDay();
                    endDate = LocalDateTime.now();
                    break;
                case "lastYear":
                    startDate = LocalDate.now().minusYears(1).with(TemporalAdjusters.firstDayOfYear()).atStartOfDay();
                    endDate = LocalDate.now().minusYears(1).with(TemporalAdjusters.lastDayOfYear()).atTime(LocalTime.MAX);
                    break;
                case "custom":
                    if (StringUtils.isBlank(auditLogRequest.getStart_date()) || StringUtils.isBlank(auditLogRequest.getEnd_date())) {
                            throw new AuditLogException(707);
                    }
                     try {
                        startDate = LocalDateTime.parse(auditLogRequest.getStart_date());
                        endDate = LocalDateTime.parse(auditLogRequest.getEnd_date());
                     } catch (DateTimeParseException e) {
                            throw new AuditLogException(708);
                    }
                      break;
                 default:
                        break;
        }

        if (startDate != null && endDate != null) {
            String startFormatted = startDate.format(formatter);
            String endFormatted = endDate.format(formatter);

            query.addCriteria(Criteria.where("created_date_time")
                    .gte(startFormatted)
                    .lte(endFormatted));
        }
        List<AuditLog> auditLogs = mongoTemplate.find(query, AuditLog.class);
        return auditLogs;
    }

}