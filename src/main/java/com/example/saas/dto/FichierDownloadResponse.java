package com.example.saas.dto;

public record FichierDownloadResponse(
        byte[] contenu,
        String nomOriginal,
        String typeMime) {
}
