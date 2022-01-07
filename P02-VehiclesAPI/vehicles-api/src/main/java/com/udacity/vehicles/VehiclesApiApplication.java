package com.udacity.vehicles;

import java.time.LocalDateTime;
import java.time.Month;

import javax.xml.soap.Detail;

import com.udacity.vehicles.domain.Condition;
import com.udacity.vehicles.domain.Location;
import com.udacity.vehicles.domain.car.Car;
import com.udacity.vehicles.domain.car.CarRepository;
import com.udacity.vehicles.domain.car.Details;
import com.udacity.vehicles.domain.manufacturer.Manufacturer;
import com.udacity.vehicles.domain.manufacturer.ManufacturerRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Launches a Spring Boot application for the Vehicles API,
 * initializes the car manufacturers in the database,
 * and launches web clients to communicate with maps and pricing.
 * MY-NOTES:
 *   Some information about Auditing in german:
 *   https://www.appcare.at/2018/01/31/spring-data-auditing.html
 */
@SpringBootApplication
@EnableJpaAuditing
public class VehiclesApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(VehiclesApiApplication.class, args);
    }

    /**
     * Initializes the car manufacturers available to the Vehicle API.
     * @param repository where the manufacturer information persists.
     * @return the car manufacturers to add to the related repository
     * MY-NOTES:
     *   see "extends JpaRepository" to understand what happens
     *   DB is not configurated, we need to configure this on the file application.properties
     *   and add the dependency for h2 on pom.xml file
     */
    @Bean
    CommandLineRunner initDatabase(ManufacturerRepository repository) {
        return args -> {
            repository.save(new Manufacturer(100, "Audi"));
            repository.save(new Manufacturer(101, "Chevrolet"));
            repository.save(new Manufacturer(102, "Ford"));
            repository.save(new Manufacturer(103, "BMW"));
            repository.save(new Manufacturer(104, "Dodge"));
        };
    }

/*
    // MY-CODE
    // wrong, this doesn't work.
    @Bean
    CommandLineRunner initDatabase2(CarRepository repository) {
        // https://howtodoinjava.com/java/date-time/java-localdatetime-class/
        LocalDateTime localDateTime = LocalDateTime.of(2019, Month.MARCH, 28, 14, 33);
        // https://www.w3schools.com/java/java_enums.asp
        Condition cond = Condition.USED;
        return args -> {
            repository.save(new Car((long)1, localDateTime, localDateTime, cond, new Details(), new Location(20.0,30.0), "1.2"));
        };
    }
*/

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    /**
     * Web Client for the maps (location) API
     * @param endpoint where to communicate for the maps API
     * @return created maps endpoint
     * endpoint variable values are into the resources/application.properties available
     */
    @Bean(name="maps")
    public WebClient webClientMaps(@Value("${maps.endpoint}") String endpoint) {
        return WebClient.create(endpoint);
    }

    /**
     * Web Client for the pricing API
     * @param endpoint where to communicate for the pricing API
     * @return created pricing endpoint
     * endpoint variable values are into the resources/application.properties available
     * https://cloud.spring.io/spring-cloud-netflix/multi/multi__service_discovery_eureka_clients.html
     */
    @Bean(name="pricing")
    public WebClient webClientPricing(@Value("${pricing.endpoint}") String endpoint) {
        return WebClient.create(endpoint);
    }

}
