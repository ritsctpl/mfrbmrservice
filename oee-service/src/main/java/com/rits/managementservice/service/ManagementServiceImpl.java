package com.rits.managementservice.service;

import com.rits.managementservice.dto.*;
import com.rits.managementservice.exception.ManageException;
import com.rits.managementservice.model.*;
import com.rits.managementservice.repository.ManageColorSchemaRepository;
import com.rits.managementservice.repository.ManageDashboardRepository;
import com.rits.managementservice.repository.ManageFilterationRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ManagementServiceImpl implements ManagementService {
    private final ManageDashboardRepository manageDashboardRepository;
    private final ManageColorSchemaRepository manageColorRepository;
    private final ManageFilterationRepository manageFilterRepository;
    private final MessageSource localMessageSource;

    public String getFormattedMessage(int code, Object... args) {
        return localMessageSource.getMessage(String.valueOf(code), args, Locale.getDefault());
    }

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public MessageModel create(ManageRequest manageRequest) throws Exception {
        if (manageDashboardRepository.existsByActiveAndSiteAndDashBoardName(1, manageRequest.getSite(), manageRequest.getDashBoardName())) {
            throw new ManageException(6, manageRequest.getDashBoardName());
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

    @Override
    public MessageModel update(ManageRequest request) throws Exception{

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
            throw new ManageException(7);
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

    @Override
    public MessageModel delete(ManageRequest manageRequest) throws Exception{
        // delete dashboard
        ManageDashboard manageDashboard = manageDashboardRepository.findByDashBoardNameAndSiteAndActive(manageRequest.getDashBoardName(), manageRequest.getSite(), 1);
        if (manageDashboard == null) {
            throw new ManageException(7);
        }
        manageDashboard.setActive(0);
        manageDashboard.setModifiedBy(manageRequest.getUser());
        manageDashboard.setModifiedDateTime(LocalDateTime.now());

        manageDashboardRepository.save(manageDashboard);

        // delete colorSchema
        List<ManageColorSchema> colorSchemas = manageColorRepository.findByDashBoardNameAndSiteAndActive(manageRequest.getDashBoardName(), manageRequest.getSite(), 1);
        if (colorSchemas == null) {
            throw new ManageException(8);
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
            throw new ManageException(9);
        }
        manageFilter.setActive(0);
        manageFilter.setModifiedBy(manageRequest.getUser());
        manageFilter.setModifiedDateTime(LocalDateTime.now());

        manageFilterRepository.save(manageFilter);

//        String deleteMessage = getFormattedMessage(3, manageRequest.getDashBoardName());
        return MessageModel.builder().messageDetails(new MessageDetails("Deleted Successfully","S")).build();
    }

    @Override
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

    @Override
    public MessageModel retrieveForFilter(ManageRequest manageRequest) throws Exception {

        ManageFilter manageFilter = manageFilterRepository.findByDashBoardNameAndSiteAndActive(manageRequest.getDashBoardName(), manageRequest.getSite(),1);
        if (manageFilter == null)
            return MessageModel.builder().messageDetails(new MessageDetails("no record found for filter","E")).build();

        return MessageModel.builder().filterResponse(manageFilter).build();
    }

    @Override
    public MessageModel retrieve(ManageRequest manageRequest) throws Exception {
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
            throw new ManageException(6, manageRequest.getDashBoardName());
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

    @Override
    public MessageModel retrieveAll(ManageRequest manageRequest) throws Exception {
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
            throw new ManageException(7, manageRequest.getSite());
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
                .response(ManagementDashboardResponse.builder()
                        .site(dashboard.getSite())
                        .dashBoardName(dashboard.getDashBoardName())
                        .dashBoardDataList(dashBoardDataModels)
                        .filterDataList(filterDataList != null ? filterDataList : new ArrayList<>())
                        .build())
                .build();
    }

}
