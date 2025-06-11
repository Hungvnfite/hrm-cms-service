package com.example.cms.common;

import com.example.cms.dto.response.FileResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Component
public class FileUtil {

    private final Logger logger = LogManager.getLogger(FileUtil.class);

    public FileResponse writeFile(List<MultipartFile> files, List<String> mappings) throws IOException {
        String url = "https://service.vnfite.com.vn/file-manager/v2/upload";

        // Tạo multipart body
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        for (MultipartFile file : files) {
            ByteArrayResource byteArrayResource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };
            body.add("files", byteArrayResource);
        }

        // mappings JSON string (giả sử bạn dùng chung mapping cho tất cả file)
        body.add("mappings", mappings);

        // Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // Gửi request
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<FileResponse> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, FileResponse.class);
        return response.getBody();
    }
}
