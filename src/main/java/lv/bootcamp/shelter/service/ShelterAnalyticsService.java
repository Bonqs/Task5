package lv.bootcamp.shelter.service;

import lv.bootcamp.shelter.model.Animal;
import lv.bootcamp.shelter.service.data.ImportResult;
import lv.bootcamp.shelter.service.data.ShelterReportData;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

public class ShelterAnalyticsService {

    public ShelterReportData buildReportData(ImportResult importResult) {
        List<Animal> allAnimals = importResult.allAnimals();

        Set<String> uniqueSpecies = new TreeSet<>();
        Map<String, List<Animal>> animalsBySpecies = new TreeMap<>();
        List<String> animalsNeedingVetInput = new ArrayList<>();

        for (Animal animal : allAnimals) {
            // - uniqueSpecies
            uniqueSpecies.add(animal.getSpecies());
            // - animalsBySpecies
            animalsBySpecies.computeIfAbsent(animal.getSpecies(), species -> new ArrayList<>()).add(animal);
            // - animalsNeedingVetInput with format name(species)
            if (!animal.isVaccinated()) {
                animalsNeedingVetInput.add(animal.getName() + "(" + animal.getSpecies() + ")");
            }
        }

        // - vaccinated vs unvaccinated counts per species
        Map<String, Map<Boolean, Long>> vaccinationCounts = allAnimals.stream()
                .collect(Collectors.groupingBy(
                        Animal::getSpecies,
                        TreeMap::new,
                        Collectors.partitioningBy(Animal::isVaccinated, Collectors.counting())));

        // - oldest animal per species (excluding unknown ages)
        Map<String, Animal> oldestBySpecies = allAnimals.stream()
                .filter(a -> a.getAge() != null)
                .collect(Collectors.toMap(
                        Animal::getSpecies,
                        a -> a,
                        BinaryOperator.maxBy(Comparator.comparing(Animal::getAge)),
                        TreeMap::new));


        // Add necessary fields to ShelterReportData
        return new ShelterReportData(importResult, allAnimals, uniqueSpecies, animalsBySpecies, animalsNeedingVetInput, vaccinationCounts, oldestBySpecies);
    }
}
