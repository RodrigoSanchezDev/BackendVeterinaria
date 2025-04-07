package com.duoc.backend.Appointment;

import org.springframework.data.repository.CrudRepository;

import com.duoc.backend.Appointment.Appointment;

public interface AppointmentRepository extends CrudRepository<Appointment, Long> {
}
