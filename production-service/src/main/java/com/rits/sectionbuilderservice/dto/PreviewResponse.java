package com.rits.sectionbuilderservice.dto;

import com.rits.componentbuilderservice.model.Component;
import com.rits.sectionbuilderservice.model.ComponentBuilder;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PreviewResponse {
    private List<Component> componentList;
}
