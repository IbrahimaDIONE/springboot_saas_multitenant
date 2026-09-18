package com.example.saas.mapper;

import com.example.saas.domain.ReglePenalite;
import com.example.saas.dto.ReglePenaliteResponse;
import org.springframework.stereotype.Component;

@Component
public class ReglePenaliteMapper {

    public ReglePenaliteResponse toResponse(ReglePenalite r) {
        return new ReglePenaliteResponse(
                r.getId(),
                r.getJoursTolérance(),
                r.getTypeConsequence().name(),
                r.getDescription()
        );
    }
}