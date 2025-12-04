package com.radyfy.common.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.radyfy.common.model.crm.grid.CrmForm;
import com.radyfy.common.model.crm.model.CrmModel;
import com.radyfy.common.service.CurrentUserSession;
import com.radyfy.common.service.crm.CrmModelService;
import com.radyfy.common.service.crm.EntityOrmDao;
import com.radyfy.common.service.crm.grid.CrmFormService;
import com.radyfy.common.service.filestore.FileStorageService;
import com.radyfy.common.service.filestore.UploadFileResponse;
import com.radyfy.common.utils.Utils;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Calendar;

@RestController
public class FileController {

	private static final Logger logger = LoggerFactory.getLogger(FileController.class);

	private final FileStorageService fileStorageService;
	private final CurrentUserSession currentUserSession;
	private final CrmFormService crmFormService;
	private final CrmModelService crmModelService;

	@Autowired
	public FileController(
			FileStorageService fileStorageService,
			CurrentUserSession currentUserSession,
			CrmFormService crmFormService,
			CrmModelService crmModelService
	) {
		this.fileStorageService = fileStorageService;
		this.currentUserSession = currentUserSession;
		this.crmFormService = crmFormService;
		this.crmModelService = crmModelService;
	}

	// @RequestMapping(value = "/api/io/crm/uploadFile", method = RequestMethod.POST)
	// public UploadFileResponse uploadFile(
	// 		@RequestParam("file") MultipartFile file,
	// 		@RequestParam("gridId") String gridId,
	// 		@RequestParam("fieldKey") String fieldKey,
	// 		@RequestParam("name") String name,
	// 		HttpServletRequest request
	// ) {

	// 	CrmForm crmGrid = crmFormService.getById(gridId);
	// 	StringBuilder pathBuilder = new StringBuilder();
	// 	pathBuilder.append(currentUserSession.getAccount().getId()).append("/");
	// 	CrmModel crmModel = crmModelService.getModel(crmGrid.getCrmModelId());
	// 	crmModelService.forEachBaseModels(bm -> {
	// 		String value = EntityOrmDao.getBaseFilterValue(
	// 				bm.getFieldName(),
	// 				currentUserSession.getAccountSession(),
	// 				currentUserSession.getUserSession());
	// 		pathBuilder.append(value).append("/");
	// 	}, crmModel);

	// 	Calendar calendar = Calendar.getInstance();
	// 	pathBuilder.append(gridId).append("/").append(fieldKey).append("/")
	// 			.append(calendar.get(Calendar.YEAR)).append("/")
	// 			.append(calendar.get(Calendar.MONTH)).append("/")
	// 			.append(calendar.get(Calendar.DATE)).append("/")
	// 			.append(name);

	// 	String fileName = pathBuilder.toString();
	// 	fileStorageService.storeFile(file, fileName);

	// 	String fileDownloadUri = "/api/public/downloadFile/" + fileName;

	// 	return new UploadFileResponse(fileName, fileDownloadUri, file.getContentType(), file.getSize());
	// }

//    @PostMapping("/uploadMultipleFiles")
//    public List<UploadFileResponse> uploadMultipleFiles(@RequestParam("file") MultipartFile[] files) {
//        return Arrays.asList(files)
//                .stream()
//                .map(file -> uploadFile(file))
//                .collect(Collectors.toList());
//    }

//	@GetMapping("/api/public/downloadFile/{fileName:.+}")
	// public ResponseEntity<Resource> downloadFile(
	// 		@PathVariable String fileName,
	// 		HttpServletRequest request
	// ) {
	// 	// Load file as Resource
	// 	Resource resource = fileStorageService.loadFileAsResource(fileName);

	// 	// Try to determine file's content type
	// 	String contentType = null;
	// 	try {
	// 		contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
	// 	} catch (IOException ex) {
	// 		logger.error("Could not determine file type.");
	// 	}

	// 	// Fallback to the default content type if type could not be determined
	// 	if (contentType == null) {
	// 		contentType = "application/octet-stream";
	// 	}

	// 	return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
	// 			.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
	// 			.body(resource);
	// }

	// @GetMapping("/api/public/downloadFile/**")
	// public ResponseEntity<byte[]> downloadFile(
	// 		HttpServletRequest httpServletRequest
	// ) {
	// 	String path = httpServletRequest.getRequestURI();
	// 	String fileName = path.substring("/api/public/downloadFile/".length());
	// 	byte[] fileBytes = fileStorageService.downloadS3File(fileName);

	// 	// create response entity object with file bytes and headers
	// 	HttpHeaders headers = new HttpHeaders();
	// 	headers.setContentType(Utils.getMediaType(fileName));
  //     headers.setCacheControl("max-age=31536000");
	// 		headers.setContentLength(fileBytes.length);

	// 	return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);
	// }


}
