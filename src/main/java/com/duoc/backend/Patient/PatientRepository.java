package com.duoc.backend.Patient;

import org.springframework.data.repository.CrudRepository;

import com.duoc.backend.Patient.Patient;

public interface PatientRepository extends CrudRepository<Patient, Long> {
}