package lv.bootcamp.shelter.service;

import lombok.extern.slf4j.Slf4j;
import lv.bootcamp.shelter.model.Animal;
import lv.bootcamp.shelter.service.data.ShelterReportData;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Map;

@Slf4j
public class ReportExportService {
    public void writeReport(Path outputPath, ShelterReportData reportData) {
        log.info("Starting export to {}", outputPath);
        try (BufferedWriter bw = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
            bw.write("Shelter Report");
            bw.newLine();
            bw.newLine();
            bw.write("Date: " + LocalDate.now());
            bw.newLine();
            bw.write("Imported rows: " + reportData.importResult().allAnimals().size());
            bw.newLine();
            bw.write("Skipped rows: " + reportData.importResult().skippedRows());
            bw.newLine();
            bw.newLine();
            bw.write("Unique species: " + reportData.uniqueSpecies().size());
            bw.newLine();
            bw.write(String.join(" ", reportData.uniqueSpecies()));
            bw.newLine();
            bw.newLine();
            bw.write("Per-species breakdown:");
            bw.newLine();
            for (Map.Entry<String, Map<Boolean, Long>> e : reportData.vaccinationCounts().entrySet()) {
                String species = e.getKey();
                long vaccinated = e.getValue().get(true);
                long unvaccinated = e.getValue().get(false);
                bw.write(species + ": total=" + (vaccinated + unvaccinated)
                        + ", vaccinated=" + vaccinated
                        + ", unvaccinated=" + unvaccinated);
                bw.newLine();
            }
            bw.newLine();
            bw.write("Oldest animal per species:");
            bw.newLine();
            for (Animal a : reportData.oldestBySpecies().values()) {
                bw.write(a.getSpecies() + ": " + a.getName() + " (" + a.getAge() + ")");
                bw.newLine();
            }
            bw.newLine();
            bw.write("Needs vet input: " + String.join(", ", reportData.animalsNeedingVetInput()));
            bw.newLine();

        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write report to " + outputPath, e);
        }
        log.info("Data report is ready at {}", outputPath);
    }
}
