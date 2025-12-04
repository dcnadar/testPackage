package com.radyfy.common.service;
// package com.radyfy.common.service;

// import com.radyfy.common.commons.Constants;
// import com.radyfy.common.commons.UpdateAction;
// import com.radyfy.common.exception.AuthException;
// import com.radyfy.common.model.AccountTag;
// import com.radyfy.common.model.BaseEntityModel;
// import com.radyfy.common.model.commons.ExportForm;
// import com.radyfy.common.model.commons.FileUpload;
// import com.radyfy.common.model.dao.DaoQuery;
// import com.radyfy.common.model.dynamic.Matrix;
// import com.radyfy.common.model.dynamic.Option;
// import com.radyfy.common.model.dynamic.SearchUrl;
// import com.radyfy.common.model.dynamic.Searchable;
// import com.radyfy.common.model.dynamic.form.*;
// import com.radyfy.common.model.dynamic.table.*;
// import com.radyfy.common.model.enums.grid.GridType;
// import com.radyfy.common.request.table.TableRequest;
// import com.radyfy.common.response.CheckboxGroup;
// import com.radyfy.common.response.TableResult;
// import com.radyfy.common.utils.MapUtils;
// import com.radyfy.common.utils.Utils;
// import org.apache.poi.ss.usermodel.*;
// import org.apache.poi.xssf.usermodel.XSSFWorkbook;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.core.io.InputStreamResource;
// import org.springframework.core.io.Resource;
// import org.springframework.data.mongodb.core.query.Criteria;
// import org.springframework.data.mongodb.core.query.CriteriaDefinition;
// import org.springframework.data.mongodb.core.query.Update;
// import org.springframework.http.HttpHeaders;
// import org.springframework.http.MediaType;
// import org.springframework.http.ResponseEntity;
// import org.springframework.stereotype.Component;

// import java.io.*;
// import java.lang.reflect.Field;
// import java.lang.reflect.InvocationTargetException;
// import java.lang.reflect.Method;
// import java.lang.reflect.ParameterizedType;
// import java.util.*;
// import java.util.concurrent.atomic.AtomicInteger;

// @FunctionalInterface
// interface FormFieldConsumer {
//     void run(Field field, FormItem formItem, String prefix, Integer contextGroup);
// }

// @FunctionalInterface
// interface SearchFieldConsumer {
//     void run(Field field, Searchable searchable, String prefix);
// }

// @FunctionalInterface
// interface ColumnConsumer {
//     void run(Field field, Column column, String prefix);
// }

// class BasicFormItem {
//     String key;
//     Field field;
//     FormItem formItem;
// }

// @Component
// public class DynamicService {

//     private static final Logger logger = LoggerFactory.getLogger(DynamicService.class);
//     private final AccountOrmDao accountOrmDao;

//     @Autowired
//     public DynamicService(AccountOrmDao accountOrmDao) {
//         this.accountOrmDao = accountOrmDao;
//     }

//     public <T extends BaseEntityModel> TableResult<T> table(TableRequest tableRequest, Class<T> klass) {
//         Matrix matrix = buildColumns(klass, tableRequest);
//         tableRequest.setFields(matrix.getFields());
//         if (Utils.isNotEmpty(tableRequest.getQ())) {
//             List<CriteriaDefinition> orCriteria = new ArrayList<>();
//             forEachSearchable(klass, "",
//                     ((field, searchable, prefix) -> orCriteria.add(buildOrCriteria(tableRequest.getQ(), prefix + field.getName()))));
//             if (!orCriteria.isEmpty()) {
//                 if (tableRequest.getAdditionalCriterias() == null) {
//                     tableRequest.setAdditionalCriterias(new ArrayList<>());
//                 }
//                 tableRequest.getAdditionalCriterias()
//                         .add(new Criteria().orOperator(orCriteria.toArray(new Criteria[0])));
//             }
//         }
//         TableResult<T> result = accountOrmDao.table(tableRequest, klass);
//         result.setColumns(matrix.getColumns());
//         resolveSuperCollectionColumns(result, klass);
//         return result;
//     }

//     private <T> Criteria buildOrCriteria(String query, String key) {

//         List<CriteriaDefinition> orCriteria = new ArrayList<>();
//         for (String q : query.split(" ")) {
//             String regex = ".*" + q + ".*";
//             orCriteria.add(Criteria.where(key)
//                     .regex(regex, "si"));
//         }
//         return new Criteria().orOperator(orCriteria.toArray(new Criteria[0]));
//     }

//     public <T> FormGroup[] form(Class<T> klass) {
//         return buildFormFields(klass).getFormGroups();
//     }

//     public <T extends BaseEntityModel> FormGroup[] form(String id, Class<T> klass) {
//         T v = null;
//         boolean updateForm = Utils.isNotEmpty(id);
//         List<String> fields = new ArrayList<>();
//         forEachClassFormField(klass, (field, formItem, prefix, contextGroup) -> fields.add(prefix + field.getName()));
//         if (updateForm) {
//             v = accountOrmDao.getById(id, klass, DaoQuery.builder().fields(fields).build());
//         }
//         return buildFormFields(klass, v).getFormGroups();
//     }

//     // public <T extends BaseEntityModel> List<Option> formValues(String id, Class<T> klass) {
//     //     FormGroup[] groups = form(id, klass);
//     //     List<Option> values = new ArrayList<>();
//     //     for (FormGroup group : groups) {
//     //         if (Utils.isNotEmpty(group.getFields())) {
//     //             group.getFields().forEach(f -> {
//     //                 if (!f.getId().equals("id")) {
//     //                     if (f.getType() == FormItem.Type.list && f.getKeyValue() != null) {
//     //                         values.add(new Option(f.getId(), f.getKeyValue()));
//     //                     } else {
//     //                         values.add(new Option(f.getId(), f.getValue()));
//     //                     }
//     //                 }
//     //             });
//     //         }
//     //     }
//     //     return values;
//     // }

//     public <T extends BaseEntityModel> FormGroup[] form(T data, Class<T> klass) {
//         return buildFormFields(klass, data).getFormGroups();
//     }

//     public <T extends BaseEntityModel> CheckboxGroup[] exportFields(Class<T> klass) {
//         return buildClassExportFields(klass);
//     }

//     public <T extends BaseEntityModel> TableResult<T> importFromFile(
//             FileUpload fileUpload,
//             Class<T> klass,
//             Boolean isCreate,
//             UpdateAction<T> updateAction
//     ) {
//         TableResult<T> tableResult = new TableResult<>();
//         tableResult.setGridType(GridType.popupTable);
//         tableResult.setColumns(new ArrayList<>());
//         tableResult.setData(new ArrayList<>());
//         try {

//             int index = fileUpload.getFile().indexOf("downloadFile/");
//             String fileName = "/opt/school-api/uploads" + fileUpload.getFile().substring(index + 12);
//             File file = new File(fileName);   //creating a new file instance
//             FileInputStream fis = new FileInputStream(file);   //obtaining bytes from the file
//             Workbook workbook = new XSSFWorkbook(fis);

//             Sheet sheet = workbook.getSheet("Sheet1");
//             Iterator<Row> rows = sheet.iterator();
//             Map<String, List<Object>> uniqueValues = new HashMap<>();
//             List<String> uniqueKeys = new ArrayList<>();
//             Map<String, Integer> fieldIndex = new HashMap<>();

//             AtomicInteger rowNumber = new AtomicInteger(0);
//             boolean hasError = false;
//             while (rows.hasNext()) {
//                 Row currentRow = rows.next();

//                 // skip header
//                 if (rowNumber.get() == 0) {
//                     Iterator<Cell> cellsInRow = currentRow.iterator();
//                     int cellIdx = 0;
//                     while (cellsInRow.hasNext()) {
//                         Cell currentCell = cellsInRow.next();
//                         fieldIndex.put(currentCell.getStringCellValue(), cellIdx);
//                         cellIdx++;
//                     }
//                     rowNumber.incrementAndGet();
//                     continue;
//                 }

//                 T instance = klass.newInstance();
//                 forEachClassFormField(
//                         klass,
//                         (field, formItem, prefix, contextGroup) -> {
//                             Integer cellId = fieldIndex.get(formItem.value());
//                             if (cellId == null && !formItem.optional()) {
//                                 throw new RuntimeException(formItem.value() + " row not present");
//                             }
//                             if (cellId != null) {
//                                 String key = prefix + field.getName();
//                                 if (rowNumber.get() == 1) {
//                                     TableColumn tableColumn = TableColumn.builder()
//                                             .index(cellId)
//                                             .key(key)
//                                             .name(formItem.value())
//                                             .sort(false)
//                                             .show(true)
//                                             .type(Column.Type.errors)
//                                             .handleKey("data")
//                                             .build();
//                                     tableResult.getColumns().add(tableColumn);
//                                     if (formItem.unique()) {
//                                         uniqueKeys.add(key);
//                                     }
//                                 }
//                                 Cell currentCell = currentRow.getCell(cellId);
//                                 logger.info("Import Row: " + rowNumber + " Field: " + key, " Value: " + currentCell);
//                                 Object value = null;
//                                 if (currentCell != null) {
//                                     switch (formItem.type()) {

//                                         case text:
//                                         case textarea:
//                                             value = currentCell.getStringCellValue();
//                                             break;
//                                         case email:
//                                         case image:
//                                             value = currentCell.getStringCellValue();
//                                             break;
//                                         case number:
//                                             value = currentCell.getNumericCellValue();
//                                             break;
//                                         case list:
//                                             if (field.getType().isEnum()) {
//                                                 value = getEnumInstance(field, currentCell.getStringCellValue());
//                                             } else if (field.isAnnotationPresent(SubCollection.class)) {
//                                                 Class<? extends BaseEntityModel> collection = field.getAnnotation(SubCollection.class).value();
//                                                 value = currentCell.getStringCellValue();
//                                                 if (value != null && Utils.isNotEmpty((String) value)) {
//                                                     List<Option> searchResult = search(collection, (String) value, true);
//                                                     if (Utils.isNotEmpty(searchResult)) {
//                                                         value = searchResult.get(0).getKey();
//                                                     }
//                                                 }
//                                             } else {
//                                                 value = currentCell.getStringCellValue();
//                                             }

//                                             break;
//                                         case date:
//                                             if (field.getType().equals(Date.class)) {
//                                                 value = currentCell.getDateCellValue();
//                                             } else {
//                                                 value = currentCell.getStringCellValue();
//                                             }
//                                             break;
//                                         case bool:
//                                             value = currentCell.getBooleanCellValue();
//                                             break;
//                                     }
//                                 }
//                                 if (value != null) {
//                                     if (formItem.unique()) {
//                                         uniqueValues.computeIfAbsent(key, i -> new ArrayList<>()).add(value);
//                                     }
//                                     setFormFieldValue(key, value, instance);
//                                 }
//                             }
//                         });
//                 Map<String, Object> errors = new HashMap<>();
//                 validateToSave(null, klass, instance, errors, null);
//                 // if errors not present in

//                 uniqueKeys.forEach(k -> {
//                     if (MapUtils.getDataValue(errors, k) == null) {
//                         List<Object> values = uniqueValues.computeIfAbsent(k, i -> new ArrayList<>());
//                         if (values.contains(getFormFieldValue(k, instance))) {
//                             MapUtils.setDataValue(errors, k, "Duplicate values found in row");
//                         }
//                     }
//                 });
//                 instance.setMeta(errors);
//                 if (!errors.isEmpty() && !hasError) {
//                     hasError = true;
//                 }
//                 tableResult.getData().add(instance);
//                 rowNumber.incrementAndGet();
//             }
//             workbook.close();

//             tableResult.getColumns().sort(Comparator.comparingInt(TableColumn::getIndex));
//             if (hasError) {
//                 return tableResult;
//             } else {
//                 tableResult.getData().forEach(d -> {
//                     if (updateAction != null) {
//                         updateAction.run(d);
//                     }
//                     if (isCreate)
//                         create(d, klass);
//                 });
//                 return null;
//             }

//         } catch (IOException | InstantiationException | IllegalAccessException e) {
//             logger.error(e.getMessage(), e);
//             throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
//         }
//     }

//     public <T extends BaseEntityModel> ResponseEntity<Resource> export(ExportForm exportForm, Class<T> klass, List<CriteriaDefinition> criteriaDefinitions, TableResult<T> tableResult) {

//         TableRequest tableRequest = new TableRequest();
// //        tableRequest.setFields(exportForm.getFields());
// //        tableRequest.setAdditionalCriterias(criteriaDefinitions);
//         /// FULL DATA CONDITION ///
//         tableRequest.setP(-1);
//         tableRequest.setS(0);
//         /// ///
//         if (tableResult == null){
//             tableResult = accountOrmDao.table(tableRequest, klass);
//         }
//         String SHEET = "Sheet1";

//         try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream();) {
//             Sheet sheet = workbook.createSheet(SHEET);
//             CreationHelper createHelper = workbook.getCreationHelper();

//             // Header
//             Row headerRow = sheet.createRow(0);
//             Map<String, BasicFormItem> fieldScore = new TreeMap<>();

//             forEachClassFormField(
//                     klass,
//                     (field, formItem, prefix, contextGroup) -> {
//                         String key = prefix + field.getName();
//                         if (exportForm.getFields().contains(key)) {
//                             String score = (contextGroup < 10 ? ("0" + contextGroup) : contextGroup)
//                                     + "0" + (formItem.index() < 10 ? ("0" + formItem.index()) : formItem.index());
//                             BasicFormItem basicFormItem = new BasicFormItem();
//                             basicFormItem.field = field;
//                             basicFormItem.formItem = formItem;
//                             basicFormItem.key = key;
//                             fieldScore.put(score, basicFormItem);
//                         }
//                     }
//             );

//             int rowIdx = 1;
//             for (T d : tableResult.getData()) {
//                 Row row = sheet.createRow(rowIdx++);

//                 if (rowIdx == 2) {
//                     int cellId = 0;
//                     for (BasicFormItem basicFormItem : fieldScore.values()) {
//                         Cell cell = headerRow.createCell(cellId++);
//                         cell.setCellValue(basicFormItem.formItem.value());
//                     }
//                 }
//                 int cellId = 0;
//                 for (BasicFormItem basicFormItem : fieldScore.values()) {
//                     try {
//                         Object value = getFormFieldValue(basicFormItem.key, d);
//                         if (value != null) {
//                             if (basicFormItem.field.isAnnotationPresent(SubCollection.class)) {
//                                 Class<? extends BaseEntityModel> collection = basicFormItem.field.getAnnotation(SubCollection.class).value();
//                                 List<CriteriaDefinition> criteriaList = new ArrayList<>();
//                                 criteriaList.add(Criteria.where("id").is(value));
//                                 List<Option> searchResult = search(collection, "", criteriaList, true);
//                                 if (Utils.isNotEmpty(searchResult)) {
//                                     value = searchResult.get(0).getValue();
//                                 } else {
//                                     value = "";
//                                 }
//                             }
//                             if (basicFormItem.field.getType().isEnum()) {
//                                 Method method = basicFormItem.field.getType().getDeclaredMethod("value");
//                                 value = method.invoke(value);
//                             }
//                             Cell cell = row.createCell(cellId);
//                             if (value instanceof Integer) {
//                                 cell.setCellValue((Integer) value);
//                             } else if (value instanceof Boolean) {
//                                 cell.setCellValue((Boolean) value);
//                             } else if (value instanceof Double) {
//                                 cell.setCellValue((Double) value);
//                             } else if (value instanceof Date) {
//                                 CellStyle cellStyle = workbook.createCellStyle();
//                                 cellStyle.setDataFormat(
//                                         createHelper.createDataFormat().getFormat("d/m/yyyy"));
//                                 cell.setCellValue((Date) value);
//                                 cell.setCellStyle(cellStyle);
//                             } else if (value instanceof Long) {
//                                 cell.setCellValue((Long) value);
//                             } else if (value instanceof Float) {
//                                 cell.setCellValue((Float) value);
//                             } else {
//                                 cell.setCellValue((String) value);
//                             }
//                         }
//                     } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
//                         logger.error("Failed to get formFieldValue, " + e.getMessage(), e);
//                     }
//                     cellId++;
//                 }
//             }

//             workbook.write(out);
//             String filename = exportForm.getFileName();
//             InputStreamResource file = new InputStreamResource(new ByteArrayInputStream(out.toByteArray()));

//             return ResponseEntity.ok()
//                     .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
//                     .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
//                     .body(file);
//         } catch (IOException e) {
//             throw new RuntimeException("Failed to import data to Excel file: " + e.getMessage());
//         }
//     }

// //    public <T extends BaseEntityModel> void setFormValues(FormGroup[] formGroups, final T data) {
// //        for (FormGroup formGroup : formGroups) {
// //            formGroup.getFields().forEach(f -> {
// //                try {
// //                    f.setValue(getFormFieldValue(f.getId(), data));
// //                } catch (Exception e) {
// //                    logger.error(e.getMessage(), e);
// //                }
// //            });
// //        }
// //    }

//     public <T extends BaseEntityModel> T create(T obj, Class<T> klass) {
//         try {
//             validateToSave(null, klass, obj);
//             return accountOrmDao.create(obj);
//         } catch (IllegalAccessException | InstantiationException e) {
//             logger.error("Failed to create: " + e.getMessage(), e);
//             throw new RuntimeException(Constants.INTERNAL_ERROR);
//         }
//     }

//     public <T extends BaseEntityModel> void update(T obj, Class<T> klass) {
//         try {
//             Update update = new Update();
//             validateToSave(obj.getId(), klass, obj, null, update);
//             accountOrmDao.update(obj.getId(), update, obj.getClass());
//         } catch (IllegalAccessException | InstantiationException e) {
//             logger.error("Failed to update: " + e.getMessage(), e);
//             throw new RuntimeException(Constants.INTERNAL_ERROR);
//         }
//     }

//     public <T extends BaseEntityModel> void updateTable(Class<T> klass, Map<String, Object> formValues) {

//         try {
//             T instance = null;
//             Update update = null;
//             for (Map.Entry<String, Object> entry : formValues.entrySet()) {

//                 Map<String, Object> valueMap = (Map<String, Object>) entry.getValue();

//                 if (entry.getKey().startsWith("new-")) {
//                     instance = klass.newInstance();
//                     validateTableItemToSave(null, klass, valueMap, update, instance);
//                     accountOrmDao.create(instance);
//                 } else {
//                     update = new Update();
//                     validateTableItemToSave(entry.getKey(), klass, valueMap, update, null);
//                     accountOrmDao.update(entry.getKey(), update, klass);
//                 }
//             }
//         } catch (IllegalAccessException | InstantiationException e) {
//             logger.error("Failed to update: " + e.getMessage(), e);
//             throw new RuntimeException(Constants.INTERNAL_ERROR);
//         }
//     }

//     public <T extends BaseEntityModel> List<Option> search(Class<T> klass, String query) {
//         return search(klass, query, new ArrayList<>(), false);
//     }

//     private <T extends BaseEntityModel> List<Option> search(Class<T> klass, String query, boolean primary) {
//         return search(klass, query, new ArrayList<>(), primary);
//     }

//     public <T extends BaseEntityModel> List<Option> search(Class<T> klass, String query, List<CriteriaDefinition> criteriaList) {
//         return search(klass, query, criteriaList, false);
//     }

//     public <T extends BaseEntityModel> List<Option> search(Class<T> klass, String query, List<CriteriaDefinition> criteriaList, boolean primary) {
//         return search(klass, query, criteriaList, primary, new String[0]);
//     }

//     public <T extends BaseEntityModel> List<Option> search(Class<T> klass, String query, List<CriteriaDefinition> criteriaList, boolean primary, String[] additionalKeys) {

//         Map<String, Searchable> fields = new LinkedHashMap<>();
//         List<CriteriaDefinition> orCriteria = new ArrayList<>();

//         forEachSearchable(klass, "",
//                 ((field, searchable, prefix) -> {
//                     String key = prefix + field.getName();
//                     if (primary) {
//                         if (searchable.primary()) {
//                             if (Utils.isNotEmpty(query)) {
//                                 String regex = ".*" + query + ".*";
//                                 orCriteria.add(Criteria.where(key).regex(regex, "si"));
//                             }
//                             fields.put(key, searchable);
//                         }
//                     } else {
//                         if(searchable.thumbColor()){
//                             fields.put(key, searchable);
//                         } else if (searchable.thumbInitials()) {
//                             if (Utils.isNotEmpty(query)) {
//                                 orCriteria.add(buildOrCriteria(query, key));
//                             }
//                             fields.put(key, searchable);
//                         } else if (searchable.thumb()) {
//                             fields.put(key, searchable);
//                         } else if (searchable.description()) {
//                             if (Utils.isNotEmpty(query)) {
//                                 orCriteria.add(buildOrCriteria(query, key));
//                             }
//                             fields.put(key, searchable);
//                         } else {
//                             if (Utils.isNotEmpty(query)) {
//                                 orCriteria.add(buildOrCriteria(query, key));
//                             }

//                             if (searchable.value()) {
//                                 fields.put(key, searchable);
//                             }
//                         }
//                     }
//                 })
//         );
//         if (Utils.isNotEmpty(orCriteria)) {
//             criteriaList.add(new Criteria().orOperator(orCriteria.toArray(new Criteria[0])));
//         }
//         List<String> finalList = new ArrayList<>(fields.keySet());
//         finalList.addAll(Arrays.asList(additionalKeys));
//         List<T> response = accountOrmDao.findByQuery(
//                 DaoQuery.builder()
//                         .criteriaList(criteriaList)
//                         .fields(finalList)
//                         .limit(10)
//                         .build(),
//                 klass
//         );
//         List<Option> result = new ArrayList<>();
//         if (Utils.isNotEmpty(response)) {
//             for (T o : response) {
//                 try {
//                     Option option = new Option();

//                     StringBuilder value = new StringBuilder();

//                     for (Map.Entry<String, Searchable> entry : fields.entrySet()) {
//                         String v = (String) getFormFieldValue(entry.getKey(), o);
//                         if (Utils.isNotEmptyOrNull(v)) {
//                             Searchable searchable = entry.getValue();
//                             if (searchable.primary()) {
//                                 value.append(v);
//                             }else if (searchable.thumbColor()) {
//                                 option.setThumbColor(v);
//                             } else if (searchable.thumbInitials()) {
//                                 option.setThumbInitials(v);
//                             } else if (searchable.thumb()) {
//                                 option.setThumb(v);
//                             } else if (searchable.description()) {
//                                 option.setDescription(v);
//                             } else {
//                                 value.append(v).append(" ");
//                             }
//                         }
//                     }
//                     if (additionalKeys.length > 0) {
//                         option.setMeta(new HashMap<>());
//                         for (String key : additionalKeys) {
//                             MapUtils.setDataValue(option.getMeta(), key, getFormFieldValue(key, o));
//                         }
//                     }
//                     option.setValue(value.toString());
//                     option.setKey(o.getId());
//                     result.add(option);
//                 } catch (Exception e) {
//                     e.printStackTrace();
//                 }
//             }
//         }
//         return result;
//     }

//     private <T> void forEachSearchable(Class<T> klass, String prefix, SearchFieldConsumer consumer) {
//         Field[] fields = klass.getDeclaredFields();
//         for (Field field : fields) {
//             if (field.isAnnotationPresent(Searchable.class)) {
//                 if (field.isAnnotationPresent(FieldIncluded.class)) {
//                     forEachSearchable(field.getType(), prefix + field.getName() + ".", consumer);
//                 } else {
//                     consumer.run(field, field.getAnnotation(Searchable.class), prefix);
//                 }
//             }
//         }
//     }

//     private <T extends BaseEntityModel> void validateToSave(String id, Class<T> klass, Object obj) throws InstantiationException, IllegalAccessException {
//         validateToSave(id, klass, obj, null, null);
//     }

//     private <T extends BaseEntityModel> void validateToSave(String id, Class<T> klass, Object obj, Map<String, Object> errors, Update update) throws IllegalAccessException, InstantiationException {

//         forEachClassFormField(
//                 klass,
//                 (field, formItem, prefix, contextGroup) -> {
//                     String name = field.getName();
//                     String key = prefix + name;
//                     Object value = getFormFieldValue(key, obj);
//                     if (validate(formItem, field, value, key, klass, id, errors)) {
//                         if (update != null) {
//                             update.set(key, value);
//                         }
//                     }
//                 }
//         );
//     }

//     private <T extends BaseEntityModel> void validateTableItemToSave(
//             String id,
//             Class<T> klass,
//             Map<String, Object> valueMap,
//             Update update,
//             T instance
//     ) {
//         forEachClassFormField(
//                 klass,
//                 (field, formItem, prefix, contextGroup) -> {
//                     String name = field.getName();
//                     String key = prefix + name;

//                     if (instance != null) {
//                         Object value = valueMap.get(key);
//                         if (validate(formItem, field, value, key, klass, id, null)) {
//                             setFormFieldValue(key, value, instance);
//                         }
//                     } else if (update != null) {
//                         if (valueMap.containsKey(key)) {
//                             Object value = valueMap.get(key);
//                             if (validate(formItem, field, value, key, klass, id, null)) {
//                                 update.set(key, value);
//                             }
//                         }
//                     }
//                 }
//         );
//     }


//     private <T extends BaseEntityModel> boolean validate(
//             FormItem formItem,
//             Field field,
//             Object value,
//             String key,
//             Class<T> klass,
//             String id,
//             Map<String, Object> errors
//     ) {

//         if (Utils.isNotEmpty(id) && (formItem.updateType() == FormItem.Visibility.hidden || formItem.updateType() == FormItem.Visibility.disabled)) {
//             return false;
//         }

//         if (key.equals("id") || key.equals("accountId")){
//             return false;
//         }
//         // if value required
//         if (!formItem.optional()) {

//             if (value != null) {
//                 if (formItem.unique()) {

//                     if (field.isAnnotationPresent(SubCollection.class)) {
//                         Class<? extends BaseEntityModel> collection = field.getAnnotation(SubCollection.class).value();
//                         // if not a valid id for current session
//                         if (accountOrmDao.getById((String) value, collection, DaoQuery.builder().fields(new ArrayList<>()).build()) == null) {
//                             throw new AuthException();
//                         }
//                     }
//                     List<T> results = accountOrmDao.findByQuery(
//                             DaoQuery.builder()
//                                     .criteriaList(Arrays.asList(
//                                             Criteria.where(key).is(value)
//                                     ))
//                                     .fields(Collections.singletonList("id")).build(), klass
//                     );
//                     if (Utils.isNotEmpty(results)) {
//                         if (Utils.isNotEmpty(id)) {
//                             if (!id.equals(results.get(0).getId())) {
//                                 if (errors != null) {
//                                     MapUtils.setDataValue(errors, key, formItem.value() + " is not unique");
//                                     return false;
//                                 }
//                                 throw new RuntimeException(formItem.value() + " is not unique");
//                             }
//                         } else {
//                             if (errors != null) {
//                                 MapUtils.setDataValue(errors, key, formItem.value() + " is not unique");
//                                 return false;
//                             }
//                             throw new RuntimeException(formItem.value() + " is not unique");
//                         }
//                     }
//                 }
//             } else {
//                 if (errors != null) {
//                     MapUtils.setDataValue(errors, key, formItem.value() + " is required");
//                     return false;
//                 }
//                 throw new RuntimeException(formItem.value() + " is required");
//             }
//         }
//         return true;
//     }

//     private <T> Matrix buildFormFields(Class<T> klass) {
//         return buildFormFields(klass, null);
//     }

//     private <T> Matrix buildFormFields(Class<T> klass, T currentData) {
//         Matrix matrix = new Matrix();
//         buildFormGroups(matrix, klass);
//         addClassFormFields(matrix, klass, currentData);
//         for (FormGroup formGroup : matrix.getFormGroups()) {
//             Collections.sort(formGroup.getFields(), Comparator.comparingInt(FormField::getIndex));
//         }
//         return matrix;
//     }

//     private <T> void buildFormGroups(Matrix matrix, Class<T> klass) {
//         if (klass.isAnnotationPresent(FieldGroups.class)) {
//             FieldGroups groups = klass.getAnnotation(FieldGroups.class);
//             int[] groupSize = new int[groups.value().length];
//             forEachClassFormField(
//                     klass,
//                     (field, formItem, prefix, contextGroup) -> groupSize[contextGroup]++
//             );
// //            forEachClassFormField(
// //                    klass,
// //                    (field, formItem, prefix, contextGroup) -> groupSize[contextGroup]++
// //            );
//             matrix.setFormGroups(new FormGroup[groups.value().length]);
//             for (FieldGroup group : groups.value()) {
//                 FormGroup formGroup = new FormGroup();
//                 formGroup.setFields(new ArrayList<>(groupSize[group.index()]));
//                 formGroup.setLabel(group.value());
//                 formGroup.setTab(group.tab());
//                 matrix.getFormGroups()[group.index()] = formGroup;
//             }
//         } else {
//             FormGroup[] formGroups = new FormGroup[1];
//             formGroups[0] = new FormGroup();
//             formGroups[0].setFields(new ArrayList<>());
//             matrix.setFormGroups(formGroups);
//         }
//     }

//     private <T> CheckboxGroup[] buildClassExportFields(Class<T> klass) {
//         Matrix matrix = new Matrix();
//         buildFormGroups(matrix, klass);
//         matrix.setExportFields(new CheckboxGroup[matrix.getFormGroups().length]);
//         forEachClassFormField(
//                 klass,
//                 (field, formItem, prefix, contextGroup) -> {

//                     CheckboxGroup checkboxGroup = matrix.getExportFields()[contextGroup];
//                     if (checkboxGroup == null) {
//                         checkboxGroup = new CheckboxGroup();
//                         FormGroup group = matrix.getFormGroups()[contextGroup];
//                         String groupName = "Fields";
//                         if (group != null && Utils.isNotEmpty(group.getLabel())) {
//                             groupName = group.getLabel();
//                         }
//                         checkboxGroup.setName(groupName);
//                         matrix.getExportFields()[contextGroup] = checkboxGroup;
//                     }

//                     checkboxGroup.getOptions().add(new Option(prefix + field.getName(), formItem.value()));
//                 }
//         );
//         return matrix.getExportFields();
//     }

//     private <T> void addClassFormFields(Matrix matrix, Class<T> klass, T currentData) {
//         forEachClassFormField(
//                 klass,
//                 (field, formItem, prefix, contextGroup) -> addFormField(matrix, field, formItem, prefix, contextGroup, currentData)
//         );
//     }

//     private <T> void forEachClassFormField(Class<T> klass, FormFieldConsumer consumer) {
//         forEachClassFormField(klass, consumer, "", null);
//     }

//     private <T> void forEachClassFormField(Class<T> klass, FormFieldConsumer consumer, String prefix, Integer contextGroup) {

//         if (klass.isAnnotationPresent(FormItems.class)) {
//             FormItems items = klass.getAnnotation(FormItems.class);
//             for (FormItem formItem : items.value()) {
//                 consumer.run(getField(klass.getSuperclass(), formItem.key()), formItem, "", formItem.group());
//             }
//         }
//         Field[] fields = klass.getDeclaredFields();
//         for (Field field : fields) {
//             if (field.isAnnotationPresent(FieldIncluded.class)) {
//                 FieldIncluded fieldIncluded = field.getAnnotation(FieldIncluded.class);
//                 int group = fieldIncluded.value();
//                 if (group == -1) {
//                     if (contextGroup != null && contextGroup >= 0) {
//                         group = contextGroup;
//                     }
//                 }
//                 forEachClassFormField(field.getType(), consumer, prefix + field.getName() + ".", group);
//             } else if (field.isAnnotationPresent(FormItem.class)) {
//                 FormItem formItem = field.getAnnotation(FormItem.class);
//                 int group = formItem.group();
//                 if (contextGroup != null && contextGroup >= 0) {
//                     group = contextGroup;
//                 }
//                 consumer.run(field, formItem, prefix, group);
//             }
//         }
//     }

//     private void addFormField(Matrix matrix, Field field, FormItem formItem, String prefix, Integer contextGroup, Object currentData) {

//         String key = prefix + field.getName();
//         FormField formField = buildFormItem(formItem, key);
//         Object value = null;
//         if (currentData != null) {
//             value = getFormFieldValue(key, currentData);
//         }
//         if (formItem.updateType() != FormItem.Visibility.none) {
//             if (formItem.updateType() == FormItem.Visibility.disabled) {
//                 formField.setDisableCondition("return true;");
//             } else if (formItem.updateType() == FormItem.Visibility.hidden || formItem.updateType() == FormItem.Visibility.hidden_fe) {
//                 formField.setShowCondition("return false;");
//             }
//         }

//         if (formItem.type() == FormItem.Type.list || formItem.type() == FormItem.Type.radio || formItem.type() == FormItem.Type.card) {
//             if (field.getType().isEnum()) {
//                 List<Option> options = new ArrayList<>();
//                 for (Object obj : field.getType().getEnumConstants()) {
//                     try {
//                         Method method = field.getType().getDeclaredMethod("value");
//                         Object enumValue = method.invoke(obj);
//                         options.add(new Option(obj.toString(), enumValue.toString()));
//                     } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
//                         e.printStackTrace();
//                     }
//                 }
//                 formField.setOptions(options);
//             }
//             if ((formItem.type() == FormItem.Type.list || formItem.type() == FormItem.Type.card) && field.isAnnotationPresent(SubCollection.class)) {
//                 Class<? extends BaseEntityModel> collection = field.getAnnotation(SubCollection.class).value();
//                 if (!Utils.isNotEmpty(formField.getApiUrl())) {
//                     if (collection.isAnnotationPresent(SearchUrl.class)) {
//                         formField.setApiUrl(collection.getAnnotation(SearchUrl.class).value());
//                     }
//                 }
//                 if (Utils.isNotEmpty(value) && value instanceof List) {
//                     ArrayList<String> listOfValues = (ArrayList<String>) value;
//                     List<Option> options = new ArrayList<>();
//                     for (String listValue : listOfValues) {
//                         List<CriteriaDefinition> criteriaList = new ArrayList<>();
//                         criteriaList.add(Criteria.where("id").is(listValue));
//                         String[] additionalKeys = field.getAnnotation(SubCollection.class).keys();
//                         List<Option> searchResult = search(collection, null, criteriaList, false, additionalKeys);
//                         if (Utils.isNotEmpty(searchResult)) {
//                             options.add(searchResult.get(0));
//                         }
//                     }
//                     formField.setOptions(options);
//                 } else if (Utils.isNotEmpty(value)) {
//                     List<CriteriaDefinition> criteriaList = new ArrayList<>();
//                     criteriaList.add(Criteria.where("id").is(value));
//                     String[] additionalKeys = field.getAnnotation(SubCollection.class).keys();
//                     List<Option> searchResult = search(collection, null, criteriaList, false, additionalKeys);
//                     if (Utils.isNotEmpty(searchResult)) {
//                         formField.setOptions(searchResult);
//                     }
//                 }
//             }
//         }

//         if (formItem.type() == FormItem.Type.form_table || formItem.type() == FormItem.Type.input_table) {
//             if (field.getType().equals(List.class)) {

//                 ParameterizedType stringListType = (ParameterizedType) field.getGenericType();
//                 Class<?> listClass = (Class<?>) stringListType.getActualTypeArguments()[0];

//                 Matrix formMatrix = buildFormFields(listClass);
//                 Matrix tableMatrix = buildColumns(listClass);
//                 formField.setFormRows(formMatrix.getFormGroups());
//                 formField.setColumns(tableMatrix.getColumns());
//             } else {
//                 throw new RuntimeException("Form Table Class can only have a list of object");
//             }
//         }
//         if (formItem.type() == FormItem.Type.form_array) {
//             if (field.getType().equals(List.class)) {

//                 ParameterizedType stringListType = (ParameterizedType) field.getGenericType();
//                 Class<?> listClass = (Class<?>) stringListType.getActualTypeArguments()[0];

//                 Matrix formMatrix = buildFormFields(listClass);
//                 formField.setFormRows(formMatrix.getFormGroups());
//             } else {
//                 throw new RuntimeException("Form Table Class can only have a list of object");
//             }
//         }
//         if (formItem.type() == FormItem.Type.tag) {
//             if (Utils.isNotEmpty(formItem.value())) {
//                 List<Option> options = new ArrayList<>();
//                 List<Option> searchResult = getAccountTags(formItem.value().toLowerCase());
//                 if (Utils.isNotEmpty(searchResult)) {
//                     options.add(searchResult.get(0));
//                 }
//                 formField.setOptions(options);
//             }
//         }

//         if (value != null) {
//             formField.setValue(value);
//         }

//         matrix.getFormGroups()[contextGroup].getFields().add(formField);
//     }

//     private Object getEnumInstance(Field field, String value) {
//         for (Object obj : field.getType().getEnumConstants()) {
//             try {
//                 Method method = field.getType().getDeclaredMethod("value");
//                 Object enumValue = method.invoke(obj);
//                 if (value.equals(enumValue.toString())) {
//                     return obj;
//                 }
//             } catch (Exception e) {
//                 e.printStackTrace();
//             }
//         }
//         return null;
//     }

//     private FormField buildFormItem(FormItem formItem, String key) {
//         FormField formField = FormField.builder()
//                 .id(Utils.isNotEmpty(formItem.key()) ? formItem.key() : key)
//                 .span(formItem.span())
//                 .min(formItem.min())
//                 .max(formItem.max())
//                 .index(formItem.index())
//                 .title(formItem.value())
//                 .type(formItem.type())
//                 .o(formItem.optional())
//                 .build();

//         if (!formItem.properties().equals(FieldOptionProvider.class)) {
//             try {
//                 FieldOptionProvider provider = formItem.properties().newInstance();
//                 FieldOption fieldOption = provider.getFieldOption();
//                 formField.setApiUrl(fieldOption.getApiUrl());
//                 formField.setNullIds(fieldOption.getNullIds());
//                 formField.setDisableCondition(fieldOption.getDisableCondition());
//                 formField.setShowCondition(fieldOption.getShowCondition());
//                 formField.setOnChangeCondition(fieldOption.getOnChangeCondition());
//                 formField.setMultiple(fieldOption.isMultiple());
//                 formField.setDetails(fieldOption.getDetails());
//                 formField.setValidate(fieldOption.getValidate());
//                 formField.setFormat(fieldOption.getFormat());
//                 formField.setAddApiUrl(fieldOption.getAddApiUrl());
//                 formField.setOtpField(fieldOption.getOtpField());
//                 formField.setAdd(fieldOption.isAdd());
//                 formField.setDelete(fieldOption.isDelete());
//                 formField.setCountTitle(fieldOption.getCountTitle());
//                 formField.setTag(fieldOption.getTag());
//                 formField.setValue(fieldOption.getValue());
//                 formField.setMeta(fieldOption.getMeta());
//             } catch (Exception e) {
//                 e.printStackTrace();
//             }
//         }
//         return formField;
//     }

//     public <T> Matrix buildColumns(Class<T> klass) {
//         return buildColumns(klass, null);
//     }

//     private <T> Matrix buildColumns(Class<T> klass, TableRequest tableRequest) {
//         Matrix matrix = new Matrix();
//         List<String> feColumns = getColumnsFe(tableRequest);
//         if (klass.isAnnotationPresent(Columns.class)) {
//             Columns columns = klass.getAnnotation(Columns.class);
//             int index = 0;
//             for (Column column : columns.value()) {
//                 String key = Utils.isNotEmpty(column.key()) ? column.key() : "col-" + index++;
//                 addTableColumnFe(feColumns, matrix, buildTableColumn(column, key), column.key());
//             }
//         }
//         addClassColumns(matrix, klass, feColumns);
//         Collections.sort(matrix.getColumns(), Comparator.comparingInt(TableColumn::getIndex));
//         return matrix;
//     }

//     private <T> void addClassColumns(Matrix matrix, Class<T> klass, List<String> feColumns) {
//         forEachClassColumns(klass, "", (field, column, prefix) -> {
//             String key = prefix + field.getName();
//             if (Utils.isNotEmpty(column.value())) {
//                 addTableColumnFe(feColumns, matrix, buildTableColumn(column, key), key);
//             } else {
//                 matrix.getFields().add(key);
//             }
//         });
//     }

//     private boolean addTableColumnFe(List<String> feColumns, Matrix matrix, TableColumn tableColumn, String key) {

//         if (Utils.isNotEmpty(key)) {
//             matrix.getFields().add(key);
//             if (Utils.isNotEmpty(feColumns)) {
//                 if (!feColumns.contains(key)) {
//                     tableColumn.setShow(false);
//                 }
//             }
//         }
//         return matrix.getColumns().add(tableColumn);
//     }

//     private List<String> getColumnsFe(TableRequest tableRequest) {
//         List<String> columns = null;
//         if (tableRequest != null && Utils.isNotEmpty(tableRequest.getMeta())) {
//             Object cols = MapUtils.getDataValue(tableRequest.getMeta(), "columns");
//             if (cols != null) {
//                 columns = (List<String>) cols;
//             }
//         }
//         return columns;
//     }

//     private <T> void forEachClassColumns(Class<T> klass, String prefix, ColumnConsumer consumer) {
//         Field[] fields = klass.getDeclaredFields();
//         for (Field field : fields) {
//             if (field.isAnnotationPresent(ColumnIncluded.class)) {
//                 forEachClassColumns(field.getType(), prefix + field.getName() + ".", consumer);
//             } else if (field.isAnnotationPresent(Column.class)) {
//                 Column column = field.getAnnotation(Column.class);
//                 consumer.run(field, column, prefix);
//             }
//         }
//     }

//     private TableColumn buildTableColumn(Column column, String key) {
//         TableColumn tableColumn = TableColumn.builder()
//                 .index(column.index())
//                 .key(key)
//                 .name(column.value())
//                 .sort(column.sort())
//                 .type(column.type())
//                 .width(column.width())
//                 .show(column.show())
//                 .select(column.select())
//                 .build();

//         if (!column.properties().equals(TableCellProvider.class)) {
//             try {
//                 TableCellProvider provider = column.properties().newInstance();
//                 TableCell tableCell = provider.getTableCell();
//                 if (Utils.isNotEmpty(tableCell.getAlign())) {
//                     tableColumn.setAlign(tableCell.getAlign());
//                 }
// //                if (tableCell.getValueGetter() != null) {
// //                    matrix.getValueGetters().put(key, tableCell.getValueGetter());
// //                    tableColumn.setKey("data." + key);
// //                }
//                 if (Utils.isNotEmpty(tableCell.getLabels())) {
//                     tableColumn.setLabels(tableCell.getLabels());
//                 }
//                 if (Utils.isNotEmpty(tableCell.getHandleKey())) {
//                     tableColumn.setHandleKey(tableCell.getHandleKey());
//                 }
//                 if (Utils.isNotEmpty(tableCell.getDisableKey())) {
//                     tableColumn.setDisableKey(tableCell.getDisableKey());
//                 }
//                 if (Utils.isNotEmpty(tableCell.getInputType())) {
//                     tableColumn.setInputType(tableCell.getInputType());
//                 }
//                 if (Utils.isNotEmpty(tableCell.getMeta())) {
//                     tableColumn.setMeta(tableCell.getMeta());
//                 }
//                 if (Utils.isNotEmpty(tableCell.getValueCondition())) {
//                     tableColumn.setValueCondition(tableCell.getValueCondition());
//                 }

//                 switch (column.type()) {
//                     case link:
//                         tableColumn.setSlug(tableCell.getLink().getSlug());
//                         break;
//                     case actions:
//                         tableColumn.setActions(tableCell.getActions());
//                         break;
//                 }
//             } catch (Exception e) {
//                 e.printStackTrace();
//             }
//         }
//         return tableColumn;
//     }

//     private <T extends BaseEntityModel> void resolveSuperCollectionColumns(TableResult<T> result, Class<T> klass) {
//         forEachClassColumns(klass, "", (field, column, prefix) -> {
//             if (field.isAnnotationPresent(SubCollection.class) && field.getType().equals(String.class)) {
//                 String key = prefix + field.getName();
//                 result.getColumns().forEach(c -> {
//                     if (c.getKey().equals(key)) {
//                         c.setHandleKey("data." + key);
//                     }
//                 });
//                 Class<? extends BaseEntityModel> collection = field.getAnnotation(SubCollection.class).value();
//                 String absentValue = field.getAnnotation(SubCollection.class).absentValue();
//                 result.getData().forEach(data -> {
//                     if (data.getMeta() == null) {
//                         data.setMeta(new HashMap<>());
//                     }
//                     String id = (String) getFormFieldValue(key, data);
//                     if (Utils.isNotEmpty(id)) {
//                         List<CriteriaDefinition> criteriaList = new ArrayList<>();
//                         criteriaList.add(Criteria.where("id").is(id));
//                         List<Option> searchResult = search(collection, null, criteriaList, false);
//                         if (Utils.isNotEmpty(searchResult)) {
//                             MapUtils.setDataValue(data.getMeta(), key, searchResult.get(0).getValue());
//                         }
//                     } else if (Utils.isNotEmpty(absentValue)) {
//                         MapUtils.setDataValue(data.getMeta(), key, absentValue);
//                     }
//                 });
//             }
//         });
//     }

//     // get value of field in a object
//     private Object getFormFieldValue(String field, Object o) {
//         try {
//             String[] fields;
//             if (field.contains(".")) {
//                 fields = field.split("\\.");
//             } else {
//                 fields = new String[]{field};
//             }

//             for (int i = 0; i < fields.length && o != null; i++) {
//                 String name = fields[i];

// //                String pre = "get";
// //                boolean superClass = false;

// //                try {
// //                    o.getClass().getDeclaredField(name).isAnnotationPresent(FormItem.class);
// //                } catch (NoSuchFieldException e) {
// //                    superClass = true;
// //                }

//                 String methodName = "get" + name.substring(0, 1).toUpperCase() + name.substring(1);

//                 Method method = getMethod(o.getClass(), methodName);
// //                if (superClass) {
// //                    method = o.getClass().getSuperclass().getDeclaredMethod(methodName);
// //                } else {
// //                    method = o.getClass().getDeclaredMethod(methodName);
// //                }
//                 if (method != null) {
//                     Object value = method.invoke(o);
//                     if (i == fields.length - 1) {
//                         return value;
//                     }
//                     o = value;
//                 } else {
//                     return null;
//                 }
//             }
//         } catch (InvocationTargetException | IllegalAccessException e) {
//             logger.error("Failed to get form field value: " + e.getMessage(), e);
//         }
//         return null;
//     }

//     private Method getMethod(Class<?> klass, String name, Class<?>... parameterTypes) {
//         try {
//             return klass.getDeclaredMethod(name, parameterTypes);
//         } catch (NoSuchMethodException e) {
//             klass = klass.getSuperclass();
//             if (klass == null || Object.class.equals(klass)) {
//                 return null;
//             }
//             return getMethod(klass, name, parameterTypes);
//         }
//     }

//     private Field getField(Class<?> klass, String name) {
//         try {
//             return klass.getDeclaredField(name);
//         } catch (NoSuchFieldException e) {
//             klass = klass.getSuperclass();
//             if (klass == null || Object.class.equals(klass)) {
//                 return null;
//             }
//             return getField(klass, name);
//         }
//     }

//     // set value of field in a object
//     private void setFormFieldValue(String field, Object value, Object o) {
//         String[] fields;
//         try {
//             if (field.contains(".")) {
//                 fields = field.split("\\.");
//             } else {
//                 fields = new String[]{field};
//             }

//             for (int i = 0; i < fields.length && o != null; i++) {
//                 String name = fields[i];
//                 String fieldName = name.substring(0, 1).toUpperCase() + name.substring(1);

//                 if (i == fields.length - 1) {
//                     String methodName = "set" + fieldName;
//                     Method method = getMethod(o.getClass(), methodName, value.getClass());

// //                    try {
// //                        if (o.getClass().getDeclaredField(name).isAnnotationPresent(FormItem.class)) {
// //                            method = o.getClass().getDeclaredMethod(methodName, value.getClass());
// //                        }
// //                    } catch (NoSuchFieldException e) {
// //                        method = o.getClass().getSuperclass().getDeclaredMethod(methodName, value.getClass());
// //                    }

//                     if (method != null) {
//                         method.invoke(o, value);
//                     }
//                 } else {
//                     String methodName = "get" + fieldName;

//                     Method method = getMethod(o.getClass(), methodName);
//                     Object o1 = method.invoke(o);
//                     if (o1 == null) {
//                         Class<?> type = o.getClass().getDeclaredField(name).getType();
//                         o1 = type.newInstance();
//                         String setMethodName = "set" + fieldName;
//                         method = getMethod(o.getClass(), setMethodName, type);
//                         method.invoke(o, o1);
//                     }
//                     o = o1;
//                 }
//             }
//         } catch (Exception e) {
//             logger.error(e.getMessage(), e);
//         }
//     }

//     /**
//      * Create the Tags based on the category.
//      *
//      * @param tag
//      * @param category
//      */
//     public void createAccountTag(String tag, String category) {
//         List<CriteriaDefinition> criteriaDefinitions = new ArrayList<>();
//         criteriaDefinitions.add(Criteria.where("category").is(category));
//         Update update = new Update();
//         update.addToSet("name", tag);
//         accountOrmDao.upsert(DaoQuery.builder().criteriaList(criteriaDefinitions).build(), update, AccountTag.class);
//     }

//     /**
//      * Get the list og tags.
//      *
//      * @param category
//      * @return
//      */
//     public List<Option> getAccountTags(String category) {
//         List<Option> tags = new ArrayList<>();
//         List<CriteriaDefinition> criteriaDefinitions = new ArrayList<>();
//         criteriaDefinitions.add(Criteria.where("category").is(category));
//         List<AccountTag> accountTags = accountOrmDao.findByQuery(DaoQuery.builder().criteriaList(criteriaDefinitions).build(), AccountTag.class);
//         if (Utils.isNotEmpty(accountTags)) {
//             AccountTag tag = accountTags.get(0);
//             List<String> optionTags = tag.getName();
//             tags.add(new Option("tags", optionTags));
//         }
//         return tags;
//     }

// }