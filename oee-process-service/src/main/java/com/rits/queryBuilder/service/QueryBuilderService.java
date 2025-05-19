package com.rits.queryBuilder.service;

import com.rits.queryBuilder.dto.*;
import com.rits.queryBuilder.exception.QueryBuilderException;
import com.rits.queryBuilder.model.*;
import com.rits.queryBuilder.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QueryBuilderService {
    private final ManageDashboardRepository manageDashboardRepository;
    private final ManageColorSchemaRepository manageColorRepository;
    private final ManageFilterationRepository manageFilterRepository;
    private static final Logger logger = LoggerFactory.getLogger(QueryBuilderService.class);

    private DriverManagerDataSource driverManagerDataSource;
    private boolean isConnected = false;

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private QueryBuilderRepository repository;

    @Autowired
    private DataSourceRepository dataSourceRepository;

    public MessageModel createQueryBuilder(QueryBuilderDTO dto) {
        if (dto == null || dto.getTemplateName() == null || dto.getTemplateName().isEmpty()) {
            return MessageModel.builder()
                    .message_details(new MessageDetails("TemplateName cannot be null or empty", "E"))
                    .build();
        }

        Optional<QueryBuilder> existingQueryBuilder = repository.findByTemplateNameAndSiteAndActive(dto.getTemplateName(), dto.getSite(),1);
        if (existingQueryBuilder.isPresent()) {
            return MessageModel.builder()
                    .message_details(new MessageDetails("TemplateName already exists", "E"))
                    .build();
        }

        QueryBuilder queryBuilder = new QueryBuilder(
                null,
                dto.getSite(),
                dto.getTemplateName(),
                dto.getTemplateType(),
                dto.getValue(),
                dto.getStatus(),
                dto.getUser(),
                null,
                LocalDateTime.now(),
                null,
                1
        );

        try {
            QueryBuilder savedQueryBuilder = repository.save(queryBuilder);
            return MessageModel.builder()
                    .message_details(new MessageDetails("Query Builder Created Successfully", "S"))
                    .response(savedQueryBuilder)
                    .build();
        } catch (Exception e) {
            return MessageModel.builder()
                    .message_details(new MessageDetails("Error creating Query Builder: " + e.getMessage(), "E"))
                    .build();
        }
    }

    public MessageModel updateQueryBuilder(String templateName, QueryBuilderDTO dto) {
        if (templateName == null || templateName.isEmpty()) {
            return MessageModel.builder()
                    .message_details(new MessageDetails("TemplateName cannot be null or empty", "E"))
                    .build();
        }

        Optional<QueryBuilder> optionalQueryBuilder = repository.findByTemplateNameAndSiteAndActive(templateName, dto.getSite(),1);
        if (!optionalQueryBuilder.isPresent()) {
            return MessageModel.builder()
                    .message_details(new MessageDetails("Query Builder not found with templateName: " + templateName, "E"))
                    .build();
        }

        QueryBuilder queryBuilder = optionalQueryBuilder.get();

        if (dto == null) {
            return MessageModel.builder()
                    .message_details(new MessageDetails("QueryBuilderDTO cannot be null", "E"))
                    .build();
        }

        queryBuilder.setTemplateName(dto.getTemplateName());
        queryBuilder.setTemplateType(dto.getTemplateType());
        queryBuilder.setValue(dto.getValue());
        queryBuilder.setStatus(dto.getStatus());
        queryBuilder.setModifiedBy(dto.getUser());
        queryBuilder.setModifiedDatetime(LocalDateTime.now());

        try {
            QueryBuilder updatedQueryBuilder = repository.save(queryBuilder);
            return MessageModel.builder()
                    .message_details(new MessageDetails("Query Builder Updated Successfully", "S"))
                    .response(updatedQueryBuilder)
                    .build();
        } catch (Exception e) {
            return MessageModel.builder()
                    .message_details(new MessageDetails("Error updating Query Builder: " + e.getMessage(), "E"))
                    .build();
        }
    }

    public MessageModel deleteQueryBuilderByTemplateName(String templateName, String site) {
        if (templateName == null || templateName.isEmpty()) {
            return MessageModel.builder()
                    .message_details(new MessageDetails("TemplateName cannot be null or empty", "E"))
                    .build();
        }

        Optional<QueryBuilder> queryBuilderOpt = repository.findByTemplateNameAndSiteAndActive(templateName, site,1);
        if (queryBuilderOpt.isPresent()) {
            try {
                repository.delete(queryBuilderOpt.get());
                return MessageModel.builder()
                        .message_details(new MessageDetails("Query Builder Deleted Successfully", "S"))
                        .build();
            } catch (Exception e) {
                return MessageModel.builder()
                        .message_details(new MessageDetails("Error deleting Query Builder: " + e.getMessage(), "E"))
                        .build();
            }
        } else {
            return MessageModel.builder()
                    .message_details(new MessageDetails("Query Builder Not Found", "E"))
                    .build();
        }
    }

    public List<QueryBuilder> retrieveAllQueryBuilders(String site) {
        try {
            return repository.findBySite(site);
        } catch (Exception e) {
            return List.of();
        }
    }

    public List<Map<String, Object>> fetchDataByTemplateOrQuery(QueryBuilderDTO request) throws Exception {
        String site = request.getSite();
        String templateName = request.getTemplateName();
        String query = request.getValue();
        Map<String, Object> filters = request.getFilters();

        if (!getConnectionStatus())
            throw new QueryBuilderException(1112);

        JdbcTemplate jdbcTemplate = getJdbcTemplate();

        if (query == null || query.trim().isEmpty()) {
            Optional<QueryBuilder> queryBuilderOpt = repository.findByTemplateNameAndSiteAndActive(templateName, site, 1);
            if (queryBuilderOpt.isPresent()) {
                query = queryBuilderOpt.get().getValue();
            } else {
                throw new QueryBuilderException(1113, templateName);
            }
        }

        if (query == null || query.trim().isEmpty())
            throw new QueryBuilderException(1114);

        List<Object> queryParams = new ArrayList<>();
        Timestamp startTimestamp, endTimestamp;

        boolean isStartTimePresent = filters.containsKey("startTime");
        boolean isEndTimePresent = filters.containsKey("endTime");

        if (isStartTimePresent && filters.get("startTime") != null && !filters.get("startTime").toString().trim().isEmpty()) {
            startTimestamp = Timestamp.valueOf(filters.get("startTime").toString().replace("T", " "));
        } else {
            startTimestamp = new Timestamp(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
        }
        filters.put("startTime", startTimestamp);

        if (isEndTimePresent && filters.get("endTime") != null && !filters.get("endTime").toString().trim().isEmpty()) {
            endTimestamp = Timestamp.valueOf(filters.get("endTime").toString().replace("T", " "));
        } else {
            endTimestamp = new Timestamp(System.currentTimeMillis());
        }
        filters.put("endTime", endTimestamp);

        Pattern selectParamPattern = Pattern.compile("(?<!:):(\\w+)");
        Matcher selectMatcher = selectParamPattern.matcher(query);

        while (selectMatcher.find()) {
            String paramName = selectMatcher.group(1);
            Object value = filters.get(paramName);
            if (value instanceof String && ((String) value).trim().isEmpty()) {
                continue;
            }
            if (value instanceof List) {
                // Modified list handling
                List<?> listValue = (List<?>) value;
                String placeholders = String.join(",", Collections.nCopies(listValue.size(), "?"));
                // Remove extra parentheses, IN clause already provides them
                query = query.replaceFirst(":" + paramName, placeholders);
                // Add each value separately
                queryParams.addAll(listValue);
            } else {
                query = query.replaceFirst(":" + paramName, "?");
                queryParams.add(value);
            }
        }

        String baseQuery;
        String conditionsPart = "";
        String groupByPart = "";
        String havingPart = "";
        String orderByPart = "";

        String[] orderParts = query.split("(?i)\\bORDER\\s+BY\\b");
        if (orderParts.length > 1) {
            orderByPart = "ORDER BY " + orderParts[1].trim();
        }

        String[] havingParts = orderParts[0].split("(?i)\\bHAVING\\b");
        if (havingParts.length > 1) {
            havingPart = "HAVING " + havingParts[1].trim();
        }

        String[] groupParts = havingParts[0].split("(?i)\\bGROUP\\s+BY\\b");
        if (groupParts.length > 1) {
            groupByPart = "GROUP BY " + groupParts[1].trim();
        }

        String[] whereParts = groupParts[0].split("(?i)\\bWHERE\\b");
        baseQuery = whereParts[0].trim();
        if (whereParts.length > 1) {
            conditionsPart = whereParts[1].trim();
        }

        List<String> updatedConditions = new ArrayList<>();
        Pattern pattern = Pattern.compile(":(\\w+)");

        if (!conditionsPart.isEmpty()) {
            String[] conditions = conditionsPart.split("\\s+AND\\s+");
            for (String condition : conditions) {
                Matcher matcher = pattern.matcher(condition);
                boolean includeCondition = true;
                while (matcher.find()) {
                    String paramName = matcher.group(1);
                    Object value = filters.get(paramName);
                    if (value instanceof String && ((String) value).trim().isEmpty()) {
                        includeCondition = false;
                        break;
                    }
                }
                if (includeCondition) {
                    updatedConditions.add(condition);
                }
            }
        }

        if (updatedConditions.isEmpty()) {
            updatedConditions.add("1=1");
        }

        StringBuilder finalQuery = new StringBuilder(baseQuery);
        if (!updatedConditions.isEmpty()) {
            finalQuery.append(" WHERE ").append(String.join(" AND ", updatedConditions));
        }
        if (!groupByPart.isEmpty()) {
            finalQuery.append(" ").append(groupByPart);
        }
        if (!havingPart.isEmpty()) {
            finalQuery.append(" ").append(havingPart);
        }
        if (!orderByPart.isEmpty()) {
            finalQuery.append(" ").append(orderByPart);
        }

        Object[] params = queryParams.toArray();
        return jdbcTemplate.queryForList(finalQuery.toString(), params);
    }

    public MessageModel getTablesAndColumns(QueryBuilderDTO dto) throws Exception {
        if (!getConnectionStatus()) {
            return MessageModel.builder()
                    .message_details(new MessageDetails("Database connection is not active", "E"))
                    .build();
        }

        List<TableInfo> tablesList = new ArrayList<>();

        try (Connection connection = getJdbcTemplate().getDataSource().getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();

            // Fetching all tables
            try (ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"})) {
                while (tables.next()) {
                    String tableName = tables.getString("TABLE_NAME");
                    List<String> columnsList = new ArrayList<>();

                    try (ResultSet columns = metaData.getColumns(null, null, tableName, "%")) {
                        while (columns.next()) {
                            columnsList.add(columns.getString("COLUMN_NAME"));
                        }
                    }

                    tablesList.add(new TableInfo(tableName, columnsList));
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving tables and columns", e);
            return MessageModel.builder()
                    .message_details(new MessageDetails("Error retrieving tables and columns: " + e.getMessage(), "E"))
                    .build();
        }

        return MessageModel.builder().tableInfoList(tablesList).message_details(new MessageDetails("Success", "S")).build();
    }

    public MessageModel createDataSource(DataSourceDTO dto) throws Exception {
        if (dto == null || StringUtils.isBlank(dto.getDataSourceName()))
            return MessageModel.builder().message_details(new MessageDetails("Datasource name cannot be empty", "E")).build();

        if (StringUtils.isBlank(dto.getHost()))
            return MessageModel.builder().message_details(new MessageDetails("Host cannot be empty", "E")).build();

        if (StringUtils.isBlank(dto.getPort()))
            return MessageModel.builder().message_details(new MessageDetails("Port cannot be empty", "E")).build();

        if (StringUtils.isBlank(dto.getDataBase()))
            return MessageModel.builder().message_details(new MessageDetails("DataBase cannot be empty", "E")).build();

        if (StringUtils.isBlank(dto.getUsername()))
            return MessageModel.builder().message_details(new MessageDetails("Username cannot be empty", "E")).build();

        if (StringUtils.isBlank(dto.getPassword()))
            return MessageModel.builder().message_details(new MessageDetails("Password cannot be empty", "E")).build();

        String handle = createHandle(dto.getSite(), dto.getDataSourceName());
        String url = createURL(dto.getHost(), dto.getPort(), dto.getDataBase());

        Optional<DataSourceData> existingDataSource = dataSourceRepository.findBySiteAndHandleAndActive(dto.getSite(), handle,1);

        if (existingDataSource.isPresent())
            return MessageModel.builder().message_details(new MessageDetails("DataSource already exists", "E")).build();

        DataSourceData dataSource = new DataSourceData(null, handle, dto.getDataSourceName(), false, dto.getSite(), dto.getHost(), dto.getPort(),
                dto.getDataBase(), dto.getUsername(), dto.getPassword(), url, LocalDateTime.now(), null, dto.getUser(), null, 1);

        try {
            DataSourceData savedDataSource = dataSourceRepository.save(dataSource);

            return MessageModel.builder()
                    .message_details(new MessageDetails("Data Source Created Successfully", "S"))
                    .dsResponse(savedDataSource)
                    .build();
        } catch (Exception e) {
            return MessageModel.builder()
                    .message_details(new MessageDetails("Error creating Data Source: " + e.getMessage(), "E"))
                    .build();
        }
    }

    public MessageModel updateDataSource(DataSourceDTO dto) throws Exception {

        if (dto == null || dto.getDataSourceId() == null || dto.getDataSourceId().isEmpty()) {
            return MessageModel.builder()
                    .message_details(new MessageDetails("Id cannot be empty", "E"))
                    .build();
        }

        DataSourceData dataSource = dataSourceRepository.findBySiteAndDataSourceIdAndActive(dto.getSite(), dto.getDataSourceId(), 1);
        if(dataSource==null){
            return MessageModel.builder()
                    .message_details(new MessageDetails("No Data Source found to update", "E"))
                    .build();
        }

        dataSource.setHost(dto.getHost());
        dataSource.setDataSourceName(dto.getDataSourceName());
        dataSource.setPort(dto.getPort());
        dataSource.setDataBase(dto.getDataBase());
        dataSource.setUsername(dto.getUsername());
        dataSource.setPassword(dto.getPassword());
        dataSource.setUrl(createURL(dto.getHost(), dto.getPort(), dto.getDataBase()));

        dataSource.setModifiedBy(dto.getUser());
        dataSource.setModifiedDatetime(LocalDateTime.now());

        try {
            DataSourceData updatedDataSource = dataSourceRepository.save(dataSource);
            return MessageModel.builder()
                    .message_details(new MessageDetails("Data Source Updated Successfully", "S"))
                    .dsResponse(updatedDataSource)
                    .build();
        } catch (Exception e) {
            return MessageModel.builder()
                    .message_details(new MessageDetails("Error updating Data Source: " + e.getMessage(), "E"))
                    .build();
        }
    }

    public MessageModel deleteDataSourceById(DataSourceDTO dto) throws Exception {
        if (dto == null || dto.getDataSourceId() == null || dto.getDataSourceId().isEmpty()) {
            return MessageModel.builder()
                    .message_details(new MessageDetails("Id cannot be empty", "E"))
                    .build();
        }

        DataSourceData dataSource = dataSourceRepository.findBySiteAndDataSourceIdAndActive(dto.getSite(), dto.getDataSourceId(), 1);
        if(dataSource==null){
            return MessageModel.builder()
                    .message_details(new MessageDetails("No Datasource found to delete", "E"))
                    .build();
        }

        dataSource.setActive(0);
        dataSource.setModifiedBy(dto.getUser());
        dataSource.setModifiedDatetime(LocalDateTime.now());

        try {
            DataSourceData deletedDataSource = dataSourceRepository.save(dataSource);
            return MessageModel.builder()
                    .message_details(new MessageDetails("Data Source Deleted Successfully", "S"))
                    .dsResponse(deletedDataSource)
                    .build();
        } catch (Exception e) {
            return MessageModel.builder()
                    .message_details(new MessageDetails("Error deleting Data Source: " + e.getMessage(), "E"))
                    .build();
        }
    }

    private String createURL(String host, String port, String dataBase) {
        return "jdbc:postgresql://"+host+":"+port+"/"+dataBase;
    }

    private String createHandle(String site, String datasourceName) {
        return "DataSourceBO:" + site + "," + datasourceName;
    }

    public MessageModel retrieveAllDataSource(DataSourceDTO request) throws Exception {
        try {
            List<DataSourceData> dataSourceDataList = dataSourceRepository.findBySiteAndActive(request.getSite(), 1);
            return MessageModel.builder().dataSourceDataList(dataSourceDataList).build();
        } catch (Exception e) {
            return MessageModel.builder()
                    .message_details(new MessageDetails("Error while getting Datasource: " + e.getMessage(), "E"))
                    .build();
        }
    }

    public MessageModel retrieveDataSource(DataSourceDTO request) throws Exception {
        try {
            if(StringUtils.isBlank(request.getDataSourceId()))
                return MessageModel.builder()
                        .message_details(new MessageDetails("Datasource id is empty", "S"))
                        .build();

            DataSourceData dataSource = dataSourceRepository.findBySiteAndDataSourceIdAndActive(request.getSite(), request.getDataSourceId(), 1);
            if(dataSource==null){
                return MessageModel.builder()
                        .message_details(new MessageDetails("No data found with this datasource id: " + request.getDataSourceId(), "S"))
                        .dsResponse(dataSource)
                        .build();
            }
            return MessageModel.builder()
                    .message_details(new MessageDetails("Datasource by id returned Successfully", "S"))
                    .dsResponse(dataSource)
                    .build();
        } catch (Exception e) {
            return MessageModel.builder()
                    .message_details(new MessageDetails("Error while getting Datasource: " + e.getMessage(), "E"))
                    .build();
        }
    }

    public MessageModel configureDataSource(DataSourceDTO config) throws Exception {
        try {
            if (config == null || StringUtils.isBlank(config.getDataSourceId())) {
                return MessageModel.builder()
                        .message_details(new MessageDetails("Datasource is empty", "E"))
                        .build();
            }

            DataSourceData data = dataSourceRepository.findBySiteAndDataSourceIdAndActive(config.getSite(), config.getDataSourceId(), 1);
            if (data == null) {
                return MessageModel.builder()
                        .message_details(new MessageDetails("Datasource not found", "E"))
                        .build();
            }

            if (config.isStatus()) { // Connecting to DB
                if (!isConnected) {
                    deactivateOtherDataSources(config.getSite(), config.getDataSourceId());

                    driverManagerDataSource = new DriverManagerDataSource();
                    driverManagerDataSource.setDriverClassName("org.postgresql.Driver");
                    driverManagerDataSource.setUrl(data.getUrl());
                    driverManagerDataSource.setUsername(data.getUsername());
                    driverManagerDataSource.setPassword(data.getPassword());

                    try {
                        jdbcTemplate = new JdbcTemplate(driverManagerDataSource);
                        jdbcTemplate.execute("SELECT 1"); // Test connection
                        isConnected = true;
                        logger.info("Database connection established.");
                    } catch (Exception ex) {
                        logger.error("Failed to connect to the database: " + ex.getMessage(), ex);
                        return MessageModel.builder()
                                .message_details(new MessageDetails("Database connection failed: " + ex.getMessage(), "E"))
                                .build();
                    }

                } else {
                    logger.warn("Database is already connected.");
                }
            } else { // Disconnecting from DB
                if (isConnected) {
                    driverManagerDataSource = null;
                    jdbcTemplate = null;
                    isConnected = false;
                    logger.info("Database connection closed.");
                } else {
                    logger.warn("No active database connection to close.");
                }
            }

            data.setStatus(config.isStatus());
            dataSourceRepository.save(data);
            return MessageModel.builder()
                    .message_details(new MessageDetails(config.isStatus() ? "Connected" : "Disconnected", "S"))
                    .build();
        } catch (Exception e) {
            logger.error("Error in configuring data source: " + e.getMessage(), e);
            return MessageModel.builder()
                    .message_details(new MessageDetails("Internal Server Error", "E"))
                    .build();
        }
    }

    private void deactivateOtherDataSources(String site, String activeDataSourceId) throws Exception {
        List<DataSourceData> activeDataSources = dataSourceRepository.findBySiteAndActive(site, 1);
        for (DataSourceData ds : activeDataSources) {
            if (!ds.getDataSourceId().equals(activeDataSourceId)) {
                ds.setStatus(false);
                dataSourceRepository.save(ds);
            }
        }
    }

    public JdbcTemplate getJdbcTemplate() throws Exception {
        if (!isConnected) {
            throw new IllegalStateException("Database connection is not active.");
        }
        return jdbcTemplate;
    }
    public boolean getConnectionStatus() {
        return isConnected;
    }

    @PostConstruct
    @Transactional
    public void resetDataSourceStatus() {
        try {
            if (!getConnectionStatus()) {
                logger.warn("Database is not ready. Skipping reset.");
                return;
            }

            List<DataSourceData> activeConnections = dataSourceRepository.findByStatus(true);
            if (!activeConnections.isEmpty()) {
                activeConnections.forEach(ds -> ds.setStatus(false));
                dataSourceRepository.saveAll(activeConnections);
                logger.info("All active database connections have been reset to inactive.");
            }
        } catch (Exception e) {
            logger.error("Error resetting data source status: {}", e.getMessage(), e);
        }
    }


    public MessageModel createManagementDasgboard(ManageRequest manageRequest) throws Exception {
        if (manageDashboardRepository.existsByActiveAndSiteAndDashBoardName(1, manageRequest.getSite(), manageRequest.getDashBoardName())) {
            throw new QueryBuilderException(6, manageRequest.getDashBoardName());
        }

        ManageDashboard dashBoard = ManageDashboard.builder()
                .handle("DashBoardBO:"+ manageRequest.getSite() + "," + manageRequest.getDashBoardName())
                .site(manageRequest.getSite())
                .dashBoardName(manageRequest.getDashBoardName())

                .createdBy(manageRequest.getUser())
                .createdDateTime(LocalDateTime.now())
                .active(1)
                .build();

        //        String createMessage = getFormattedMessage(1, manageRequest.getDashBoardName());
        return MessageModel.builder().messageDetails(new MessageDetails("Created Successfully","S")).manageResponse(manageDashboardRepository.save(dashBoard)).build();
    }

    public MessageModel updateManagementDasgboard(ManageRequest request) throws Exception{

        // Update dashboard
        if (request.getDashBoardDataList() != null) {
            updateOrCreateDashboard(request);
        }

        // Update colorSchema
        if (request.getDashBoardDataList() != null) {
            request.getDashBoardDataList().forEach(dashBoardData -> {
                boolean hasColorScheme = dashBoardData.getData() != null
                        && dashBoardData.getData().stream()
                        .filter(Objects::nonNull)
                        .anyMatch(data -> data.getColorScheme() != null);

                if (hasColorScheme) {
                    updateOrCreateColorSchema(request, dashBoardData, dashBoardData.getCategory());
                }
            });
        }

        // Update filter
        if (request.getFilterDataList() != null) {
            updateOrCreateFilter(request);
        }
        ManagementDashboardRes manageResponse = buildManageResponse(request);
//        String updateMessage = getFormattedMessage(2, request.getDashBoardName());
        return MessageModel.builder().messageDetails(new MessageDetails("Updated Successfully","S")).managementUpdateRes(manageResponse).build();
    }


    private boolean checkIfDashboardExists(ManageRequest request) {
        return manageDashboardRepository.existsByActiveAndSiteAndDashBoardName(1, request.getSite(), request.getDashBoardName());
    }

    private boolean checkIfColorSchemaExists(ManageRequest request, String category) {
        String handle = "ColorSchemeBO:" + request.getSite() + "," + request.getDashBoardName() + "," + category;
        return manageColorRepository.existsByHandleAndSiteAndActive(handle, request.getSite(), 1);
    }

    private boolean checkIfFilterExists(ManageRequest request) {
        String handle = "FilterBO:" + request.getSite() + "," + request.getDashBoardName();
        return manageFilterRepository.existsByHandleAndSiteAndActive(handle, request.getSite(), 1);
    }

    private void updateOrCreateDashboard(ManageRequest request) {

        if (checkIfDashboardExists(request)) {
            // Update existing record
            String handle = "DashBoardBO:" + request.getSite() + "," + request.getDashBoardName();
            ManageDashboard db = manageDashboardRepository.findByHandleAndSiteAndActive(handle, request.getSite(), 1);
            List<DashBoardData> dashBoardDataList = manageDataRecord(request);
            db.setDashBoardName(request.getDashBoardName());
            db.setSite(request.getSite());
            db.setModifiedBy(request.getUser());
            db.setModifiedDateTime(LocalDateTime.now());
            db.setDashBoardDataList(dashBoardDataList);
            manageDashboardRepository.save(db);
        } else {
            // Create new record
//            manageDashboardRepository.save(buildDashBoard(request));
            throw new QueryBuilderException(7);
        }
    }

    private List<DashBoardData> manageDataRecord(ManageRequest request) {
        if (request == null || request.getDashBoardDataList() == null) {
            return Collections.emptyList();
        }
        ManageRequest request1 = new ManageRequest(request);
        return request1.getDashBoardDataList().stream()
                .map(dashboardData -> {
                    if (dashboardData.getData() != null) {
                        dashboardData.getData().forEach(data -> {
                            String dataName = data.getDataName();
                            if (dataName != null) {
                                String handleRef = request1.getDashBoardName() + "," + dashboardData.getCategory() + "," + dataName;
                                data.setHandleRef(handleRef);
                                data.setColorScheme(null);
                            }
                        });
                    }
                    return dashboardData;
                })
                .collect(Collectors.toList());
    }


    private void updateOrCreateColorSchema(ManageRequest request, DashBoardData dashboard, String category) {
        if (checkIfColorSchemaExists(request, category)) {
            // Update existing record
            String handle = "ColorSchemeBO:" + request.getSite() + "," + request.getDashBoardName() + "," + category;
            ManageColorSchema cs = manageColorRepository.findByHandleAndSiteAndActive(handle, request.getSite(), 1);
            cs.setSite(request.getSite());
            cs.setDashBoardName(request.getDashBoardName());
            cs.setModifiedBy(request.getUser());
            cs.setModifiedDateTime(LocalDateTime.now());
            cs.setColorSchemeList(updateColorSchema(request, dashboard));
            manageColorRepository.save(cs);
        } else {
            manageColorRepository.save(buildColorSchema(request, dashboard, category));
        }
    }

    private List<ColorSchemeItem> updateColorSchema(ManageRequest request, DashBoardData dashboardData) {
        if (request == null || request.getDashBoardDataList() == null) {
            return Collections.emptyList();
        }

        return dashboardData.getData().stream()
                .filter(data -> data.getColorScheme() != null)
                .map(data -> {
                    String dashboardName = request.getDashBoardName() != null ? request.getDashBoardName() : "UnknownDashboard";
                    String category = dashboardData.getCategory() != null ? dashboardData.getCategory() : "UnknownCategory";
                    String dataName = data.getDataName() != null ? data.getDataName() : "UnknownData";

                    String handleRef = dashboardName + "," + category + "," + dataName;
                    if (data.getColorSchemeItem() == null) {
                        data.setColorSchemeItem(new ColorSchemeItem());
                    }

                    data.getColorSchemeItem().setHandleRef(handleRef);
                    data.getColorSchemeItem().setColorScheme(data.getColorScheme());
                    return data.getColorSchemeItem();
                })

                .collect(Collectors.toList());
    }


    private ManageColorSchema buildColorSchema(ManageRequest request, DashBoardData dashboardData, String category){
        List<ColorSchemeItem> colorSchemeItemList = updateColorSchema(request, dashboardData);

        return ManageColorSchema.builder()
                .site(request.getSite())
                .handle("ColorSchemeBO:" + request.getSite() + "," + request.getDashBoardName() + "," + category)
                .dashBoardName(request.getDashBoardName())
                .colorSchemeList(colorSchemeItemList)
                .createdBy(request.getUser())
                .createdDateTime(LocalDateTime.now())
                .active(1)
                .build();
    }

    private void updateOrCreateFilter(ManageRequest request) {

        if (checkIfFilterExists(request)) {
            // Update existing record
            String handle = "FilterBO:" + request.getSite() + "," + request.getDashBoardName();
            ManageFilter f = manageFilterRepository.findByHandleAndSiteAndActive(handle, request.getSite(), 1);
            f.setDashBoardName(request.getDashBoardName());
            f.setSite(request.getSite());
            f.setModifiedBy(request.getUser());
            f.setModifiedDateTime(LocalDateTime.now());
            f.setFilterationData(request.getFilterDataList());
            manageFilterRepository.save(f);
        } else {
            manageFilterRepository.save(buildFilter(request));
        }
    }
    private ManageFilter buildFilter(ManageRequest request){

        return ManageFilter.builder()
                .site(request.getSite())
                .handle("FilterBO:" + request.getSite() + "," + request.getDashBoardName())
                .dashBoardName(request.getDashBoardName())
                .filterationData(request.getFilterDataList())
                .createdBy(request.getUser())
                .createdDateTime(LocalDateTime.now())
                .active(1)
                .build();
    }

    private ManagementDashboardRes buildManageResponse(ManageRequest request) {
        List<DashBoardData> dashBoardDataList = new ArrayList<>();

        if (request != null && request.getDashBoardDataList() != null && !request.getDashBoardDataList().isEmpty()) {
            for (DashBoardData dashboardData : request.getDashBoardDataList()) {
                List<Data> dataResponseList = new ArrayList<>();

                if (dashboardData.getData() != null) {
                    for (Data data : dashboardData.getData()) {
                        if (data != null) {
                            Data dataResponse = Data.builder()
                                    .dataName(data.getDataName())
                                    .type(data.getType())
                                    .query(data.getQuery())
                                    .endPoint(data.getEndPoint())
                                    .enabled(data.isEnabled())
                                    .seconds(data.getSeconds())
                                    .column(data.getColumn())
                                    .handleRef(buildHandleRef(request, dashboardData, data))
                                    .colorScheme(buildColorSchemeResponse(data))
                                    .build();
                            dataResponseList.add(dataResponse);
                        }
                    }
                }

                DashBoardData dashboardDataResponse = DashBoardData.builder()
                        .category(dashboardData.getCategory())
                        .enabled(dashboardData.isEnabled())
                        .data(dataResponseList)
                        .build();

                dashBoardDataList.add(dashboardDataResponse);
            }
        }

        List<FilterData> filterResponseList = buildFilterResponse(request);

        return ManagementDashboardRes.builder()
                .site(request.getSite())
                .dashBoardName(request.getDashBoardName())
                .dashBoardDataList(dashBoardDataList)
                .filterDataList(filterResponseList)
                .build();
    }

    private String buildHandleRef(ManageRequest request, DashBoardData dashboardData, Data data) {
        String dashBoardName = request != null && request.getDashBoardName() != null ? request.getDashBoardName() : "UnknownDashboard";
        String category = dashboardData != null && dashboardData.getCategory() != null ? dashboardData.getCategory() : "UnknownCategory";
        String dataName = data != null && data.getDataName() != null ? data.getDataName() : "UnknownData";
        return dashBoardName + "," + category + "," + dataName;
    }

    private ColorScheme buildColorSchemeResponse(Data data) {
        if (data.getColorSchemeItem() != null) {
            return ColorScheme.builder()
                    .lineColor(data.getColorScheme().getLineColor())
                    .itemColor(data.getColorScheme().getItemColor())
                    .build();
        }
        return null;
    }

    private List<FilterData> buildFilterResponse(ManageRequest request) {
        List<FilterData> filterResponseList = new ArrayList<>();
        if (request.getFilterDataList() != null) {
            for (FilterData filterData : request.getFilterDataList()) {
                filterResponseList.add(FilterData.builder()
                        .filterName(filterData.getFilterName())
                        .keyName(filterData.getKeyName())
                        .retriveFeild(filterData.getRetriveFeild())
                        .type(filterData.getType())
                        .status(filterData.isStatus())
                        .controller(filterData.getController())
                        .endpoint(filterData.getEndpoint())
                        .build());
            }
        }
        return filterResponseList;
    }

    public MessageModel deleteManagementDasgboard(ManageRequest manageRequest) throws Exception{
        // delete dashboard
        ManageDashboard manageDashboard = manageDashboardRepository.findByDashBoardNameAndSiteAndActive(manageRequest.getDashBoardName(), manageRequest.getSite(), 1);
        if (manageDashboard == null) {
            throw new QueryBuilderException(7);
        }
        manageDashboard.setActive(0);
        manageDashboard.setModifiedBy(manageRequest.getUser());
        manageDashboard.setModifiedDateTime(LocalDateTime.now());

        manageDashboardRepository.save(manageDashboard);

        // delete colorSchema
        List<ManageColorSchema> colorSchemas = manageColorRepository.findByDashBoardNameAndSiteAndActive(manageRequest.getDashBoardName(), manageRequest.getSite(), 1);
        if (colorSchemas == null) {
            throw new QueryBuilderException(8);
        }

        colorSchemas.forEach(schema -> {
            schema.setActive(0);
            schema.setModifiedBy(manageRequest.getUser());
            schema.setModifiedDateTime(LocalDateTime.now());
        });

        manageColorRepository.saveAll(colorSchemas);

        // delete filter
        ManageFilter manageFilter = manageFilterRepository.findByDashBoardNameAndSiteAndActive(manageRequest.getDashBoardName(), manageRequest.getSite(), 1);
        if (manageFilter == null) {
            throw new QueryBuilderException(9);
        }
        manageFilter.setActive(0);
        manageFilter.setModifiedBy(manageRequest.getUser());
        manageFilter.setModifiedDateTime(LocalDateTime.now());

        manageFilterRepository.save(manageFilter);

//        String deleteMessage = getFormattedMessage(3, manageRequest.getDashBoardName());
        return MessageModel.builder().messageDetails(new MessageDetails("Deleted Successfully","S")).build();
    }

    public MessageModel retrieveForColorScheme(ManageRequest manageRequest) throws Exception {
        ManageColorSchema manageColorSchema = null;
        List<ManageColorSchema> manageColorSchemaList = null;

        if(StringUtils.isBlank(manageRequest.getCategory())) {
            manageColorSchemaList = manageColorRepository.findByDashBoardNameAndSiteAndActive(manageRequest.getDashBoardName(), manageRequest.getSite(), 1);
        } else {
            String handle = "ColorSchemeBO:" + manageRequest.getSite() + "," + manageRequest.getDashBoardName() + "," + manageRequest.getCategory();
            manageColorSchema = manageColorRepository.findByHandleAndSiteAndActive(handle, manageRequest.getSite(), 1);
        }

        if (manageColorSchema == null && manageColorSchemaList == null)
            return MessageModel.builder().messageDetails(new MessageDetails("no record found for colorScheme","E")).build();

        return MessageModel.builder().colorSchemeResponse(manageColorSchema).colorSchemeResponseList(manageColorSchemaList).build();
    }

    public MessageModel retrieveForFilter(ManageRequest manageRequest) throws Exception {

        ManageFilter manageFilter = manageFilterRepository.findByDashBoardNameAndSiteAndActive(manageRequest.getDashBoardName(), manageRequest.getSite(),1);
        if (manageFilter == null)
            return MessageModel.builder().messageDetails(new MessageDetails("no record found for filter","E")).build();

        return MessageModel.builder().filterResponse(manageFilter).build();
    }

    public MessageModel retrieveManagementDasgboard(ManageRequest manageRequest) throws Exception {
        if (manageRequest == null || manageRequest.getSite() == null || manageRequest.getDashBoardName() == null) {
            throw new IllegalArgumentException("Invalid: DashBoardName must not be null.");
        }

        ManageDashboard dashboard = mongoTemplate.findOne(
                Query.query(Criteria.where("site").is(manageRequest.getSite())
                        .and("dashBoardName").is(manageRequest.getDashBoardName())
                        .and("active").is(1)),
                ManageDashboard.class
        );

        if (dashboard == null) {
            throw new QueryBuilderException(6, manageRequest.getDashBoardName());
        }

        List<ManageColorSchema> colorSchemas = mongoTemplate.find(
                Query.query(Criteria.where("dashBoardName").is(manageRequest.getDashBoardName())),
                ManageColorSchema.class
        );

        if (colorSchemas == null) {
            colorSchemas = new ArrayList<>();
        }

        Map<String, ManageColorSchema> colorSchemaMap = new HashMap<>();
        for (ManageColorSchema colorSchema : colorSchemas) {
            if (colorSchema != null && colorSchema.getDashBoardName() != null) {
                colorSchemaMap.put(colorSchema.getDashBoardName(), colorSchema);
            }
        }

        if (dashboard.getDashBoardDataList() != null) {
            assignColorSchemesToData(dashboard.getDashBoardDataList(), colorSchemas);
        }

        List<ManageFilter> filterData = mongoTemplate.find(
                Query.query(Criteria.where("dashBoardName").is(manageRequest.getDashBoardName())),
                ManageFilter.class
        );

        if (filterData == null) {
            filterData = new ArrayList<>();
        }

        List<FilterData> filterationDataList = new ArrayList<>();
        for (ManageFilter filter : filterData) {
            if (filter != null && filter.getFilterationData() != null) {
                filterationDataList.addAll(filter.getFilterationData());
            }
        }

        MessageModel messageModel = mapToMessageModel(dashboard, filterationDataList);

        return messageModel;
    }

    public MessageModel retrieveAllManagementDasgboard(ManageRequest manageRequest) throws Exception {
        List<AggregationOperation> pipeline = Arrays.asList(
                Aggregation.match(Criteria.where("site").is(manageRequest.getSite()).and("active").is(1)),
                Aggregation.lookup("r_manage_color_schema", "dashBoardName", "dashBoardName", "colorSchemaData"),
                Aggregation.lookup("r_manage_filter", "dashBoardName", "dashBoardName", "filterData"),
                Aggregation.project()
                        .and("site").as("site")
                        .and("dashBoardName").as("dashBoardName")
                        .and("dashBoardDataList").as("dashBoardDataList")
                        .and("filterData.filterationData").as("filterDataList")
                        .and("colorSchemaData.colorSchemeList").as("colorSchemeList")
        );

        Aggregation aggregation = Aggregation.newAggregation(pipeline);
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "r_manage_dashboard", Document.class);
        List<Document> documents = results.getMappedResults();

        if (documents == null || documents.isEmpty()) {
            throw new QueryBuilderException(7, manageRequest.getSite());
        }

        return MessageModel.builder().managementList(documents.stream()
                .map(doc -> mapToDashboardResponse(doc))
                .collect(Collectors.toList())).build();
    }

    private ManagementDashboardRes mapToDashboardResponse(Document doc) {
        ManagementDashboardRes response = new ManagementDashboardRes();
        response.setSite(doc.getString("site"));
        response.setDashBoardName(doc.getString("dashBoardName"));

        List<?> colorSchemeRawList = doc.getList("colorSchemeList", Object.class); // Use Object.class to avoid casting error
        List<Document> colorSchemeDocs = new ArrayList<>();

        if (colorSchemeRawList != null) {
            for (Object obj : colorSchemeRawList) {
                if (obj instanceof Document) {
                    colorSchemeDocs.add((Document) obj);
                } else if (obj instanceof Map) {
                    colorSchemeDocs.add(new Document((Map<String, Object>) obj)); // Convert Map to Document
                } else if (obj instanceof List) {
                    // Handle case where an object is a List (e.g., nested structures)
                    List<?> innerList = (List<?>) obj;
                    for (Object innerObj : innerList) {
                        if (innerObj instanceof Map) {
                            colorSchemeDocs.add(new Document((Map<String, Object>) innerObj));
                        } else {
                            System.out.println("Skipping unsupported inner type: " + innerObj.getClass().getName());
                        }
                    }
                }
            }
        }

        Map<String, ColorScheme> colorSchemeMap = new HashMap<>();
        if (colorSchemeDocs != null) {
            for (Document colorSchemeDoc : colorSchemeDocs) {
                String handleRef = colorSchemeDoc.getString("handleRef");
                colorSchemeMap.put(handleRef, mapToColorScheme(colorSchemeDoc));
            }
        }

        List<Document> dashBoardDataDocs = doc.getList("dashBoardDataList", Document.class);
        if (dashBoardDataDocs != null) {
            List<DashBoardData> dashBoardDataList = dashBoardDataDocs.stream()
                    .map(d -> mapToDashBoardData(d, colorSchemeMap))
                    .collect(Collectors.toList());
            response.setDashBoardDataList(dashBoardDataList);
        }

        List<Object> filterDataList = doc.getList("filterDataList", Object.class);
        List<Document> filterDataDocs = new ArrayList<>();
        if(filterDataList != null) {
            for (Object obj : filterDataList) {
                if (obj instanceof Document) {
                    filterDataDocs.add((Document) obj);
                } else if (obj instanceof Map) {
                    filterDataDocs.add(new Document((Map<String, Object>) obj));
                } else if (obj instanceof List) {
                    // Handle case where an object is a List (e.g., nested structures)
                    List<?> innerList = (List<?>) obj;
                    for (Object innerObj : innerList) {
                        if (innerObj instanceof Map) {
                            filterDataDocs.add(new Document((Map<String, Object>) innerObj));
                        } else {
                            System.out.println("Skipping unsupported inner type: " + innerObj.getClass().getName());
                        }
                    }
                }
            }
        }

        if (filterDataDocs != null) {
            List<FilterData> filterDataLists = filterDataDocs.stream()
                    .map(this::mapToFilterData)
                    .collect(Collectors.toList());
            response.setFilterDataList(filterDataLists);
        }

        return response;
    }

    private DashBoardData mapToDashBoardData(Document doc, Map<String, ColorScheme> colorSchemeMap) {
        DashBoardData dashBoardData = new DashBoardData();
        dashBoardData.setCategory(doc.getString("category"));
        dashBoardData.setEnabled(doc.getBoolean("enabled"));

        List<Document> dataDocs = doc.getList("data", Document.class);
        if (dataDocs != null) {
            List<Data> dataItems = dataDocs.stream()
                    .map(d -> mapToDashBoardDataItem(d, colorSchemeMap))
                    .collect(Collectors.toList());
            dashBoardData.setData(dataItems);
        }

        return dashBoardData;
    }

    private Data mapToDashBoardDataItem(Document doc, Map<String, ColorScheme> colorSchemeMap) {
        Data dataItem = new Data();
        dataItem.setDataName(doc.getString("dataName"));
        dataItem.setType(doc.getString("type"));
        dataItem.setQuery(doc.getString("query"));
        dataItem.setEndPoint(doc.getString("endPoint"));
        dataItem.setEnabled(doc.getBoolean("enabled"));
        dataItem.setSeconds(doc.getString("seconds"));
        dataItem.setColumn(doc.getString("column"));

        String handleRef = doc.getString("handleRef");
        if (colorSchemeMap.containsKey(handleRef)) {
            dataItem.setColorScheme(colorSchemeMap.get(handleRef));
        }

        return dataItem;
    }

    private ColorScheme mapToColorScheme(Document colorSchemeDoc) {
        ColorScheme colorScheme = new ColorScheme();

        Document colorSchemeData = colorSchemeDoc.get("colorScheme", Document.class);

        if (colorSchemeData != null) {
            List<String> lineColorList = colorSchemeData.getList("lineColor", String.class);
            colorScheme.setLineColor(lineColorList);

            List<Document> itemColorDocs = colorSchemeData.getList("itemColor", Document.class);
            List<ItemColor> itemColors = new ArrayList<>();

            if (itemColorDocs != null) {
                for (Document itemColorDoc : itemColorDocs) {
                    ItemColor itemColor = new ItemColor();
                    itemColor.setColor(itemColorDoc.getString("color"));
                    itemColor.setRange(itemColorDoc.getInteger("range"));
                    itemColors.add(itemColor);
                }
            }

            colorScheme.setItemColor(itemColors);
        }
        return colorScheme;
    }

    private FilterData mapToFilterData(Document doc) {
        return new FilterData(
                doc.getString("filterName"),
                doc.getString("type"),
                doc.getString("keyName"),
                doc.getString("retriveFeild"),
                doc.getBoolean("status"),
                doc.getString("controller"),
                doc.getString("endpoint")
        );
    }

    public void assignColorSchemesToData(List<DashBoardData> dashBoardDataList, List<ManageColorSchema> colorSchemas) {
        if (dashBoardDataList == null || dashBoardDataList.isEmpty() || colorSchemas == null || colorSchemas.isEmpty()) {
            return;
        }

        Map<String, ColorSchemeItem> handleRefToColorSchemeMap = new HashMap<>();

        for (ManageColorSchema colorSchema : colorSchemas) {
            if (colorSchema != null && colorSchema.getColorSchemeList() != null) {
                for (ColorSchemeItem colorSchemeItem : colorSchema.getColorSchemeList()) {
                    if (colorSchemeItem != null && colorSchemeItem.getHandleRef() != null) {
                        handleRefToColorSchemeMap.put(colorSchemeItem.getHandleRef(), colorSchemeItem);
                    }
                }
            }
        }

        for (DashBoardData dashBoardData : dashBoardDataList) {
            if (dashBoardData != null && dashBoardData.getData() != null) {
                for (Data dataItem : dashBoardData.getData()) {
                    if (dataItem != null && dataItem.getHandleRef() != null) {
                        ColorSchemeItem colorSchemeItem = handleRefToColorSchemeMap.get(dataItem.getHandleRef());
                        if (colorSchemeItem != null) {
                            dataItem.setColorScheme(colorSchemeItem.getColorScheme());
                        }
                    }
                }
            }
        }
    }


    public MessageModel mapToMessageModel(ManageDashboard dashboard, List<FilterData> filterDataList) {
        if (dashboard == null) {
            throw new IllegalArgumentException("Dashboard cannot be null");
        }

        List<DashBoardData> dashBoardDataModels = new ArrayList<>();
        if (dashboard.getDashBoardDataList() != null) {
            for (DashBoardData dashBoardData : dashboard.getDashBoardDataList()) {
                if (dashBoardData == null) continue;

                DashBoardData dashBoardDataModel = new DashBoardData();
                dashBoardDataModel.setCategory(dashBoardData.getCategory());
                dashBoardDataModel.setEnabled(dashBoardData.isEnabled());

                List<Data> dataModels = new ArrayList<>();
                if (dashBoardData.getData() != null) {
                    for (Data dataItem : dashBoardData.getData()) {
                        if (dataItem == null) continue;

                        Data dataModel = new Data();
                        dataModel.setHandleRef(dataItem.getHandleRef());
                        dataModel.setDataName(dataItem.getDataName());
                        dataModel.setType(dataItem.getType());
                        dataModel.setQuery(dataItem.getQuery());
                        dataModel.setEndPoint(dataItem.getEndPoint());
                        dataModel.setEnabled(dataItem.isEnabled());
                        dataModel.setSeconds(dataItem.getSeconds());
                        dataModel.setColumn(dataItem.getColumn());

                        if (dataItem.getColorScheme() != null) {
                            ColorScheme colorSchemeModel = new ColorScheme();
                            colorSchemeModel.setLineColor(dataItem.getColorScheme().getLineColor());
                            colorSchemeModel.setItemColor(dataItem.getColorScheme().getItemColor());
                            dataModel.setColorScheme(colorSchemeModel);
                        }

                        dataModels.add(dataModel);
                    }
                }

                dashBoardDataModel.setData(dataModels);
                dashBoardDataModels.add(dashBoardDataModel);
            }
        }

        return MessageModel.builder()
                .managementrResponse(ManagementDashboardResponse.builder()
                        .site(dashboard.getSite())
                        .dashBoardName(dashboard.getDashBoardName())
                        .dashBoardDataList(dashBoardDataModels)
                        .filterDataList(filterDataList != null ? filterDataList : new ArrayList<>())
                        .build())
                .build();
    }

}
