package org.example.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class IngestionReq {

    private String content;
    private String source;
    private String description;
    private List<String> topics;

}
