package com.rits.dccollect.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class DcPromptTranslationList {
    private String  commonLocale;

            private String  prompt;
}
