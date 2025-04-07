package com.duoc.backend.Invoice;

import com.duoc.backend.Care.Care;
import com.duoc.backend.Care.CareRepository;
import com.duoc.backend.Medication.Medication;
import com.duoc.backend.Medication.MedicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private MedicationRepository medicationRepository;

    @Autowired
    private CareRepository careRepository;

    // --------------------------------------------------
    // Métodos originales
    // --------------------------------------------------

    public Iterable<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    public Invoice getInvoiceById(Long id) {
        return invoiceRepository.findById(id).orElse(null);
    }

    public Invoice saveInvoice(Invoice invoice) {
        // Validar que los medicamentos existen
        List<Medication> validMedications = StreamSupport.stream(
                medicationRepository.findAllById(
                        invoice.getMedications().stream().map(Medication::getId).collect(Collectors.toList())
                ).spliterator(), false
        ).collect(Collectors.toList());
        if (validMedications.size() != invoice.getMedications().size()) {
            throw new IllegalArgumentException("Algunos medicamentos no existen en la base de datos.");
        }

        // Validar que los servicios existen
        List<Care> validCares = StreamSupport.stream(
                careRepository.findAllById(
                        invoice.getCares().stream().map(Care::getId).collect(Collectors.toList())
                ).spliterator(), false
        ).collect(Collectors.toList());
        if (validCares.size() != invoice.getCares().size()) {
            throw new IllegalArgumentException("Algunos servicios no existen en la base de datos.");
        }

        // Calcular el costo total basado en los servicios y medicamentos asociados
        double totalCareCost = validCares.stream()
                .mapToDouble(Care::getCost)
                .sum();

        double totalMedicationCost = validMedications.stream()
                .mapToDouble(Medication::getCost)
                .sum();

        invoice.setTotalCost(totalCareCost + totalMedicationCost);

        // Guardar la factura en el repositorio
        return invoiceRepository.save(invoice);
    }

    public void deleteInvoice(Long id) {
        invoiceRepository.deleteById(id);
    }

    // --------------------------------------------------
    // NUEVOS MÉTODOS para generar factura detallada
    // --------------------------------------------------

    /**
     * Sobrecarga que recibe únicamente el ID de la Invoice y genera la factura detallada
     * (se asume que no hay cargos adicionales o se ponen en 0).
     *
     * @param invoiceId El ID de la factura en la base de datos
     * @return DetailedInvoiceDTO con el desglose y la factura actualizada
     */
    public DetailedInvoiceDTO generateDetailedInvoice(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("No existe la factura con ID: " + invoiceId));

        // Llamamos al método que requiere (Invoice, double) con additionalCharge = 0
        return generateDetailedInvoice(invoice, 0.0);
    }

    /**
     * Genera una factura detallada que incluye el desglose de servicios, medicamentos
     * y un cargo adicional si corresponde.
     *
     * @param invoice          Factura con listas de cares y medications ya asignadas
     * @param additionalCharge Cargos adicionales a sumar
     * @return DetailedInvoiceDTO con la factura guardada y el desglose
     */
    public DetailedInvoiceDTO generateDetailedInvoice(Invoice invoice, double additionalCharge) {
        // Validar que los medicamentos existen
        List<Medication> validMedications = StreamSupport.stream(
                medicationRepository.findAllById(
                        invoice.getMedications().stream().map(Medication::getId).collect(Collectors.toList())
                ).spliterator(), false
        ).collect(Collectors.toList());
        if (validMedications.size() != invoice.getMedications().size()) {
            throw new IllegalArgumentException("Algunos medicamentos no existen en la base de datos.");
        }

        // Validar que los servicios existen
        List<Care> validCares = StreamSupport.stream(
                careRepository.findAllById(
                        invoice.getCares().stream().map(Care::getId).collect(Collectors.toList())
                ).spliterator(), false
        ).collect(Collectors.toList());
        if (validCares.size() != invoice.getCares().size()) {
            throw new IllegalArgumentException("Algunos servicios no existen en la base de datos.");
        }

        // Calcular el costo total
        double totalCareCost = validCares.stream()
                .mapToDouble(Care::getCost)
                .sum();

        double totalMedicationCost = validMedications.stream()
                .mapToDouble(Medication::getCost)
                .sum();

        double totalCost = totalCareCost + totalMedicationCost + additionalCharge;

        // Construir el detalle de la factura
        StringBuilder breakdownBuilder = new StringBuilder();
        breakdownBuilder.append("Detalle de la factura:\n");

        if (!validCares.isEmpty()) {
            breakdownBuilder.append("Servicios:\n");
            for (Care care : validCares) {
                breakdownBuilder.append("- ")
                        .append(care.getName())
                        .append(": $")
                        .append(care.getCost())
                        .append("\n");
            }
        }

        if (!validMedications.isEmpty()) {
            breakdownBuilder.append("Medicamentos:\n");
            for (Medication med : validMedications) {
                breakdownBuilder.append("- ")
                        .append(med.getName())
                        .append(": $")
                        .append(med.getCost())
                        .append("\n");
            }
        }

        breakdownBuilder.append("Cargos adicionales: $").append(additionalCharge).append("\n");
        breakdownBuilder.append("Costo total: $").append(totalCost).append("\n");

        // Asegurar fecha y hora si no fueron seteadas
        if (invoice.getDate() == null) {
            invoice.setDate(LocalDate.now());
        }
        if (invoice.getTime() == null) {
            invoice.setTime(LocalTime.now());
        }

        // Actualizar el total y guardar
        invoice.setTotalCost(totalCost);
        Invoice savedInvoice = invoiceRepository.save(invoice);

        // Retornar un DTO con el desglose
        DetailedInvoiceDTO detailedInvoiceDTO = new DetailedInvoiceDTO();
        detailedInvoiceDTO.setInvoice(savedInvoice);
        detailedInvoiceDTO.setBreakdown(breakdownBuilder.toString());

        return detailedInvoiceDTO;
    }
}
