package com.arenaHub.backendArena.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.arenaHub.backendArena.entity.Impostazioni;

@Repository
public interface ImpostazioniRepository extends JpaRepository<Impostazioni, Short> {
}