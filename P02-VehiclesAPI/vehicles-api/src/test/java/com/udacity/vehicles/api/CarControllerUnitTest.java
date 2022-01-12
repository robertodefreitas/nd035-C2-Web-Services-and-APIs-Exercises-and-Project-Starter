package com.udacity.vehicles.api;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.udacity.vehicles.client.maps.MapsClient;
import com.udacity.vehicles.client.prices.PriceClient;
import com.udacity.vehicles.domain.Condition;
import com.udacity.vehicles.domain.Location;
import com.udacity.vehicles.domain.car.Car;
import com.udacity.vehicles.domain.car.Details;
import com.udacity.vehicles.domain.manufacturer.Manufacturer;
import com.udacity.vehicles.service.CarService;
import java.net.URI;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Implements testing of the CarController class.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
public class CarControllerUnitTest {

    private static final Logger log = LoggerFactory.getLogger(CarControllerUnitTest.class);

    /**
     * MockMvc instance to simulate HTTP requests
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * Create a JSON Content from an Object
     * Example: mockJson.write(mockCar).getJson()
     */
    @Autowired
    private JacksonTester<Car> mockJsonCar;
    //private JacksonTester<MvcResult> mockJsonMvcResult;
    //private JacksonTester<Location> mockJsonLocation;

    @MockBean
    private CarService carService;

    @MockBean
    private PriceClient priceClient;

    @MockBean
    private MapsClient mapsClient;


    /**
     * Creates an example Car object for use in testing.
     * @return an example Car object
     */
    private Car getCar() {
        String methodeName = new Object(){}.getClass().getEnclosingMethod().getName();
        log.info("[{}] UnitTest is started...", methodeName);

        Car car = new Car();
        car.setLocation(new Location(40.730610, -73.935242));
        Details details = new Details();
        Manufacturer manufacturer = new Manufacturer(101, "Chevrolet");
        details.setManufacturer(manufacturer);
        details.setModel("Impala");
        details.setMileage(32280);
        details.setExternalColor("white");
        details.setBody("sedan");
        details.setEngine("3.6L V6");
        details.setFuelType("Gasoline");
        details.setModelYear(2018);
        details.setProductionYear(2018);
        details.setNumberOfDoors(4);
        car.setDetails(details);
        car.setCondition(Condition.USED);

        log.info("[{}] UnitTest is finished.", methodeName);

        return car;
    }


    /**
     * Creates pre-requisites for testing, such as an example car.
     */
    @Before
    public void setup() throws Exception {
        String methodeName = new Object(){}.getClass().getEnclosingMethod().getName();
        log.info("[{}] UnitTest is started...", methodeName);

        Car mockCarSetup = getCar();
        mockCarSetup.setId(11L); //11L == (long)11
        /**
         * https://stackoverflow.com/questions/33546124/mockito-given-versus-when
         * JUNIT: when(foo.doSomething()).thenReturn(somethingElse);
         * MOCKITO: given(foo.doSomething()).willReturn(somethingElse);
         * Also, wenn carService.save() verwendet wird, dann soll mockCar verwendet werden.
         */
        given(carService.save(any())).willReturn(mockCarSetup); // this will be used by createCar()
        given(carService.list()).willReturn(Collections.singletonList(mockCarSetup)); // this will be used by listCars()
        given(carService.findById(any())).willReturn(mockCarSetup); // this will be used by findCar()
        //given(carService.delete(any())).willReturn(mockCarSetup); // this is wrong, because the methode delete(id) is void, also no Return.

        log.info("[{}] UnitTest is finished.", methodeName);
    }


    /**
     * Tests for successful creation of new car in the system
     * @throws Exception when car creation fails in the system
     */
    @Test
    public void createCar() throws Exception {
        String methodeName = new Object(){}.getClass().getEnclosingMethod().getName();
        log.info("[{}] UnitTest is started...", methodeName);

        Car mockCarCreateCar = getCar();

        // because of following error by build this api, I added an id to the created car
        // mvn clean package
        // [ERROR] Failed to execute goal org.apache.maven.plugins:maven-surefire-plugin:2.22.2:test (default-test) on project vehicles-api: There are test failures.
        //mockCarCreateCar.setId(1L); //1L == (long)1

        mockMvc.perform(
            post(new URI("/cars"))
            .content(mockJsonCar.write(mockCarCreateCar).getJson())
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8)
        )
        .andExpect(status().isCreated());

        MvcResult findCarResult = mockMvc
            .perform(
                post(new URI("/cars"))
                .content(mockJsonCar.write(mockCarCreateCar).getJson())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)
        )
        .andReturn();

        //String jsonResult = mockJsonMvcResult.write(findCarResult.getResponse().getContentAsString()).getJson();

        log.info("[{}] [mockJson.write(mockCarCreateCar).getJson()] INPUT JSON: {}", methodeName, mockJsonCar.write(mockCarCreateCar).getJson());
        log.info("[{}] RESULT/OUTPUT JSON: {}", methodeName, findCarResult.getResponse().getContentAsString());
        log.info("[{}] UnitTest is finished.", methodeName);
    }


    /**
     * Tests if the read operation appropriately returns a list of vehicles.
     * @throws Exception if the read operation of the vehicle list fails
     */
    @Test
    public void listCars() throws Exception {
        /**
         * TODO: Add a test to check that the `get` method works by calling
         *   the whole list of vehicles. This should utilize the car from `getCar()`
         *   below (the vehicle will be the first in the list). [DONE]
         */
        String methodeName = new Object(){}.getClass().getEnclosingMethod().getName();
        log.info("[{}] UnitTest is started...", methodeName);

        // First Car
        mockMvc
            .perform(
                get("/cars")
            )
            .andExpect(status().isOk());

        // Second Car, other possibility with the same result
        mockMvc
            .perform(
                get(new URI("/cars"))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)
            )
            .andExpect(status().isOk());

        verify(carService, times(2)).list();

        log.info("[{}] UnitTest is finished.", methodeName);

    }


    /**
     * Tests the read operation for a single car by ID.
     * @throws Exception if the read operation for a single car fails
     */
    @Test
    public void findCar() throws Exception {
        /**
         * TODO: Add a test to check that the `get` method works by calling
         *   a vehicle by ID. This should utilize the car from `getCar()` below. [DONE]
         */
        String methodeName = new Object(){}.getClass().getEnclosingMethod().getName();
        log.info("[{}] UnitTest is started...", methodeName);

        mockMvc
            .perform(
                get("/cars/41")
            )
            .andExpect(status().isOk());

        mockMvc
            .perform(
                    get("/cars/42")
            )
            .andExpect(status().isOk());

        /**
         * Created to see the OUTPUT JSON file
         */
        MvcResult findCarResult1 = mockMvc
            .perform(
                get("/cars/41")
            )
            .andReturn();

        //String jsonResult = mockJsonMvcResult.write(findCarResult.getResponse().getContentAsString()).getJson();

        log.info("[{}] RESULT1 JSON: {}", methodeName, findCarResult1.getResponse().getContentAsString());


        verify(carService, times(2)).findById(41L); // 41L == (long)41
        verify(carService, times(1)).findById(42L); // 42L == (long)42

        // #######################################################

        Car mockCarFindCar = getCar();
        mockCarFindCar.setId(43L); //43L == (long)43

        //String priceResult = priceClient.getPrice(43L); //43L == (long)43
        //String priceResult = "EUR 00000.00";
        //Location mapsResult = mapsClient.getAddress(mockCarFindCar.getLocation());
        //String mapsResultJson = mockJsonLocation.write(mapsResult).getJson();

        //given(carService.save(mockCarFindCar)).willReturn(mockCarFindCar); // this will be used by createCar()
        //given(carService.list()).willReturn(Collections.singletonList(mockCarFindCar)); // this will be used by listCars()
        given(carService.findById(43L)).willReturn(mockCarFindCar); // this will be used by findCar()
        //given(priceClient.getPrice(43L)).willReturn(priceResult); // this will be used by findCar()

        /**
         * Created to see the OUTPUT JSON file
         */
        MvcResult findCarResult2 = mockMvc
                .perform(
                        get("/cars/43")
                )
                .andReturn();
        log.info("[{}] RESULT2 JSON: {}", methodeName, findCarResult2.getResponse().getContentAsString());
        //log.info("[{}] PRICE: {}", methodeName, priceResult);
        //log.info("[{}] LOCATION JSON: {}", methodeName, mapsResultJson);

        // #######################################################

        log.info("[{}] UnitTest is finished.", methodeName);
    }


    /**
     * Tests the deletion of a single car by ID.
     * @throws Exception if the delete operation of a vehicle fails
     */
    @Test
    public void deleteCar() throws Exception {
        /**
         * TODO: Add a test to check whether a vehicle is appropriately deleted
         *   when the `delete` method is called from the Car Controller. This
         *   should utilize the car from `getCar()` below. [DONE]
         */
        String methodeName = new Object(){}.getClass().getEnclosingMethod().getName();
        log.info("[{}] UnitTest is started...", methodeName);

        mockMvc
            .perform(
                delete("/cars/51")
            )
            .andExpect(status().isNoContent());

        mockMvc
            .perform(
                    delete("/cars/51")
            )
            .andExpect(status().isNoContent());

        mockMvc
            .perform(
                    delete("/cars/52")
            )
            .andExpect(status().isNoContent());

        verify(carService, times(2)).delete(51L); // 51L == (long)51
        verify(carService, times(1)).delete(52L); // 52L == (long)52

        log.info("[{}] UnitTest is finished.", methodeName);
    }

}