package com.julien.lotterysystem.service.activitystatus.operator;

import com.julien.lotterysystem.entity.dto.ConvertActivityStatusDTO;
import org.springframework.stereotype.Component;

@Component
public class ActivityOperator extends AbstractActivityOperator {

    @Override
    public int sequence() {
        return 1;
    }

    @Override
    public boolean isNeedConvert(ConvertActivityStatusDTO activityStatusDTO) {
        return true;
    }

    @Override
    public Boolean convert(ConvertActivityStatusDTO activityStatusDTO) {
        return true;
    }
}
