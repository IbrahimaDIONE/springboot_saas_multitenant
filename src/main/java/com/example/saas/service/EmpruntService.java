package com.example.saas.service;

import com.example.saas.dto.*;
import java.util.*;

public interface EmpruntService {
    EmpruntResponse       emprunter(EmpruntRequest request);
    List<EmpruntResponse> mesEmprunts();
    EmpruntResponse       findById(UUID id);
    List<EmpruntResponse> findAllByTenant(String statut);
    LectureResponse       lireEnLigne(UUID empruntId);
}