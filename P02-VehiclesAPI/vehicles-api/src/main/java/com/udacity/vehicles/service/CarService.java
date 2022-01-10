package com.udacity.vehicles.service;

import com.udacity.vehicles.client.maps.MapsClient;
import com.udacity.vehicles.client.prices.Price;
import com.udacity.vehicles.client.prices.PriceClient;
import com.udacity.vehicles.domain.Location;
import com.udacity.vehicles.domain.car.Car;
import com.udacity.vehicles.domain.car.CarRepository;

import java.util.List;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Implements the car service create, read, update or delete
 * information about vehicles, as well as gather related
 * location and price data when desired.
 */
@Service
public class CarService {

    private static final Logger log = LoggerFactory.getLogger(CarService.class);

    private final CarRepository carRepository;
    private final MapsClient mapsClient;
    private final PriceClient priceClient;

    //public CarService(CarRepository carRepository) {
    public CarService(CarRepository carRepository, MapsClient mapsClient, PriceClient priceClient) {
        /**
         * TODO: Add the Maps and Pricing Web Clients you create
         *   in `VehiclesApiApplication` as arguments and set them here. [DONE]
         */
        this.carRepository = carRepository;
        this.mapsClient = mapsClient;
        this.priceClient = priceClient;
    }

    /**
     * Gathers a list of all vehicles
     * @return a list of all vehicles in the CarRepository
     */
    public List<Car> list() {
        return carRepository.findAll();
    }

    /**
     * Gets car information by ID (or throws exception if non-existent)
     * @param id the ID number of the car to gather information on
     * @return the requested car's information, including location and price
     */
    public Car findById(Long id) {
        /**
         * TODO: Find the car by ID from the `repository` if it exists. [DONE]
         *   If it does not exist, throw a CarNotFoundException [DONE]
         *   Remove the below code as part of your implementation.
         */
        //Car car = new Car();
        Car carFounded = null;
        
        String className = this.getClass().getSimpleName();
        String methodeName = new Object(){}.getClass().getEnclosingMethod().getName();

        /**
         * ## Example from P2-L2 ##
         * public String retrieveDogBreedById(Long id) {
         *     Optional<String> optionalBreed = Optional.ofNullable(dogRepository.findBreedById(id));
         *     String breed = optionalBreed.orElseThrow(DogNotFoundException::new);
         *     return breed;
         * }
         * -> doesnt work!
         */
        // carRepository gets data by the method save(Car car)
//        Optional<Car> carFoundedOptional = Optional.ofNullable(repository.findById(id).get());
//        carFounded = carFoundedOptional.orElseThrow(CarNotFoundException::new);


/*
        // Example with the class Optional results from repository.findById
        // https://stackoverflow.com/questions/30686215/avoid-nosuchelementexception-with-stream

        // https://docs.oracle.com/javase/8/docs/api/java/util/Optional.html
        // carRepository gets data by the method save(Car car)
        //Optional<Car> carResultOptinal = repository.findById((long)1);
        Optional<Car> carResultOptinal = repository.findById(id);
        if (carResultOptinal.isPresent()){
            System.out.println("INFO [" + className + "] [" + methodeName + "] object carFounded with ID " + id + " is NOT NULL");
            carFounded = carResultOptinal.get();
        } else {
            System.out.println("INFO [" + className + "] [" + methodeName + "] object carFounded with ID " + id + " is NULL");
        }
*/


        try {
            // carRepository gets data by the method save(Car car)
            carFounded = carRepository.findById(id).get();
            System.out.println("INFO [" + className + "] [" + methodeName + "] Object carFounded with ID " + id + " is NOT NULL (car available)");
            log.info("[{}] Object carFounded with ID {} is NOT NULL (car available)", methodeName, id);
        }
//        catch (NoSuchElementException | NullPointerException exc) {
//            System.out.println("ERRO [" + className + "] [" + methodeName + "]  object carFounded with ID " + id + " is NULL (car NOT available) (both Exceptions)");
//        }
        catch (NullPointerException npe) {
            System.out.println("ERRO [" + className + "] [" + methodeName + "]  Object carFounded with ID " + id + " is NULL (car NOT available) (NullPointerException)");
            log.error("[{}] Object carFounded with ID {} is NULL (car NOT available) (NullPointerException)", methodeName, id);
        }
        catch (NoSuchElementException nsee) {
            System.out.println("ERRO [" + className + "] [" + methodeName + "]  Object carFounded with ID " + id + " is NULL (car NOT available) (NoSuchElementException)");
            log.error("[{}] Object carFounded with ID {} is NULL (car NOT available) (NoSuchElementException)", methodeName, id);
            // https://stackoverflow.com/questions/8423700/how-to-create-a-custom-exception-type-in-java
            throw new CarNotFoundException();
        }
        // https://stackoverflow.com/questions/8423700/how-to-create-a-custom-exception-type-in-java
        catch (CarNotFoundException cnfe) {
            System.out.println("ERRO [" + className + "] [" + methodeName + "]  Object carFounded with ID " + id + " is NULL (car NOT available) (CarNotFoundException)");
            log.error("[{}] Object carFounded with ID {} is NULL (car NOT available) (CarNotFoundException)", methodeName, id);
        }


        /**
         * TODO: Use the Pricing Web client you create in `VehiclesApiApplication`
         *   to get the price based on the `id` input' [DONE]
         * TODO: Set the price of the car [DONE]
         * Note: The car class file uses @transient, meaning you will need to call
         *   the pricing service each time to get the price.
         */
        String priceReturn = priceClient.getPrice(id);
        carFounded.setPrice(priceReturn);
        log.info("[{}] priceClient.getPrice({}): {}",
                methodeName,
                id,
                priceReturn
        );


        /**
         * TODO: Use the Maps Web client you create in `VehiclesApiApplication`
         *   to get the address for the vehicle. You should access the location
         *   from the car object and feed it to the Maps service. [DONE]
         * TODO: Set the location of the vehicle, including the address information [DONE]
         * Note: The Location class file also uses @transient for the address,
         * meaning the Maps service needs to be called each time for the address.
         */
        Location carLocation = carFounded.getLocation();
        carLocation = mapsClient.getAddress(carLocation);
        carFounded.setLocation(carLocation);
        log.info("[{}] mapsClient.getAddress(carLocation).getCity(): {}",
                methodeName,
                mapsClient.getAddress(carLocation).getCity()
        );

        //return car;
        return carFounded;
    }

    /**
     * Either creates or updates a vehicle, based on prior existence of car
     * @param car A car object, which can be either new or existing
     * @return the new/updated car is stored in the repository
     */
    public Car save(Car car) {
        if (car.getId() != null) {
            log.info("Car {}/{} with ID {} will be updated.", car.getDetails().getBody(), car.getDetails().getModel(), car.getId());
            return carRepository.findById(car.getId())
                    .map(carToBeUpdated -> {
                        carToBeUpdated.setDetails(car.getDetails());
                        carToBeUpdated.setLocation(car.getLocation());
                        return carRepository.save(carToBeUpdated);
                    }).orElseThrow(CarNotFoundException::new);
        }

        log.info("Car {}/{} will be saved.", car.getDetails().getBody(), car.getDetails().getModel());
        return carRepository.save(car);
    }

    /**
     * Deletes a given car by ID
     * @param id the ID number of the car to delete
     */
    public void delete(Long id) {

        String className = this.getClass().getSimpleName();
        String methodeName = new Object(){}.getClass().getEnclosingMethod().getName();

        /**
         * TODO: Find the car by ID from the `repository` if it exists. [DONE]
         *   If it does not exist, throw a CarNotFoundException [DONE]
         */
        Car carToRemove = findById(id);

        /**
         * TODO: Delete the car from the repository. [DONE]
         */
        carRepository.delete(carToRemove);
        log.info("[{}] Car ID {} is removed.", methodeName, carToRemove.getId());
    }
}
