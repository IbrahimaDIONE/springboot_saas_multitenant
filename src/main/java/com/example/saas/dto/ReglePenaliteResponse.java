package com.example.saas.dto;

import java.util.UUID;

public record ReglePenaliteResponse(
        UUID   id,
        int    joursTolérance,
        String typeConsequence,
        String description
) {}