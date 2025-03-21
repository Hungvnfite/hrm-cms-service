package com.example.cms.feign;

import com.example.cms.config.FeignConfig;
import com.example.cms.dto.request.CreateAccountInfoCrmRequest;
import com.example.cms.dto.request.CrmVerifyCreateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "crm-service", url = "${spring.crm.url}",
        configuration = {FeignConfig.class})
public interface CRMService {

    @PostMapping(value = "${spring.crm.gen-token}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    String genToken(@RequestBody CreateAccountInfoCrmRequest request);

    @PostMapping(value = "${spring.crm.verify-create}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    String verifyCreate(@RequestBody CrmVerifyCreateRequest request);
}
