package com.example.saas.mapper;

import com.example.saas.domain.Emprunt;
import com.example.saas.dto.EmpruntResponse;
import org.springframework.stereotype.Component;

@Component
public class EmpruntMapper {

    public EmpruntResponse toResponse(Emprunt e) {
        var etudiant = e.getEtudiant();
        return new EmpruntResponse(
                e.getId(),
            etudiant == null ? null : etudiant.getId(),
            etudiant == null ? null : etudiant.getUtilisateur().getPrenom(),
            etudiant == null ? null : etudiant.getUtilisateur().getNom(),
                e.getOuvrage().getId(),
                e.getOuvrage().getTitre(),
                e.getOuvrage().getAuteur(),
                e.getDateEmprunt(),
                e.getDateExpiration(),
                e.getDateRetour(),
                e.getStatut().name()
        );
    }
}