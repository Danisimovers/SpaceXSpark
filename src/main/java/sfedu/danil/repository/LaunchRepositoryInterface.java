package sfedu.danil.repository;

import sfedu.danil.model.Launch;

import java.util.List;
import java.util.Optional;

public interface LaunchRepositoryInterface {


    void saveAll(List<Launch> launches);
    void saveByFlightNumber(Launch launch);

    Optional<Launch> readByFlightNumber(int flightNumber);
    List<Launch> readAll();
    boolean existsByFlightNumber(int flightNumber);

    void updateLaunch(int flightNumber, Launch updatedLaunch);
    void deleteLaunch(int flightNumber);
    void deleteAll();
}
