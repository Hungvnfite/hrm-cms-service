package com.example.cms.dto.response;

import com.example.cms.dto.model.FileDto;
import lombok.Data;

import java.util.List;

@Data
public class FileResponse {

    private Boolean success;
    private List<FileDto> files;
}
