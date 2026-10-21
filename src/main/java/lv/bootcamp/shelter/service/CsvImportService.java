package lv.bootcamp.shelter.service;

import lombok.extern.slf4j.Slf4j;
import lv.bootcamp.shelter.model.Animal;
import lv.bootcamp.shelter.service.data.ImportResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CsvImportService {

    public ImportResult importAnimals(Path inputPath) {
        log.info("Starting import from {}", inputPath);
        List<Animal> allAnimals = new ArrayList<>();
        int skippedRows = 0;
        // Read intake.csv with UTF-8.
        try (BufferedReader br = Files.newBufferedReader(inputPath, StandardCharsets.UTF_8)) {
            // Skip header row.
            String header = br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split((","), -1);

                if (parts.length != 5) {
                    log.warn("Skipping malformed row: {}", line);
                    skippedRows++;
                } else {
                    try {
                        String name = parts[0].trim();
                        String species = parts[1].trim();
                        String ageText = parts[2].trim();
                        if (!isValidAge(ageText)) {
                            log.warn("Skipping row with invalid age: {}", line);
                            skippedRows++;
                            continue;
                        }
                        Integer age = ageText.isEmpty() ? null : Integer.valueOf(ageText);
                        boolean vaccinated = Boolean.parseBoolean((parts[3].trim()));
                        // Parse intakeDate using DateTimeFormatter.
                        LocalDate intakeDate = LocalDate.parse(parts[4].trim(), DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                        if (!name.isEmpty() && !species.isEmpty()) {
                            Animal newAnimal = new Animal(name, species, age, vaccinated, intakeDate);
                            allAnimals.add(newAnimal);
                        } else {
                            log.warn("Skipping row: {}", line);
                            skippedRows++;
                        }
                    } catch (DateTimeException e) {
                        log.warn("Skipping row: {} ({})", line, e.getMessage());
                        skippedRows++;
                    }
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read " + inputPath, e);
        }


        // Map each row to Animal object.
        log.info("Import finished: {} imported, {} skipped", allAnimals.size(), skippedRows);
        return new ImportResult(allAnimals, skippedRows);
    }

    // Skip header row.
    private boolean isValidAge(String ageText) {
        if (ageText.isEmpty()) {
            return true;
        }
        try {
            int age = Integer.parseInt(ageText);
            return age >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
