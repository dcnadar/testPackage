package com.radyfy.common.service.crm.config.search;

import java.util.List;

import com.radyfy.common.model.crm.model.CrmModel;
import com.radyfy.common.model.dynamic.Option;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CrmSearchConsumerProps {
    private final CrmModel crmModel;
    private final String query;
    private final String absentValue;
    private final String modelUniqueKey;
    private final List<String> additionalKeys;
    private final List<Option> result;
}
