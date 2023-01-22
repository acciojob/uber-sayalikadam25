package com.driver.services.impl;

import com.driver.model.*;
import com.driver.repository.CabRepository;
import com.driver.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.driver.repository.CustomerRepository;
import com.driver.repository.DriverRepository;
import com.driver.repository.TripBookingRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	CustomerRepository customerRepository2;

	@Autowired
	DriverRepository driverRepository2;

	@Autowired
	TripBookingRepository tripBookingRepository2;
	@Autowired
	CabRepository cabRepository;

	@Override
	public void register(Customer customer) {
		//Save the customer in database
		customerRepository2.save(customer);
	}

	@Override
	public void deleteCustomer(Integer customerId) {
		// Delete customer without using deleteById function
		Customer customer=customerRepository2.findById(customerId).get();
		customerRepository2.delete(customer);
	}

	@Override
	public TripBooking bookTrip(int customerId, String fromLocation, String toLocation, int distanceInKm) throws Exception{
		//Book the driver with lowest driverId who is free (cab available variable is Boolean.TRUE). If no driver is available, throw "No cab available!" exception
		//Avoid using SQL query
		List<Driver> driverList=driverRepository2.findAll();
		Customer customer = customerRepository2.findById(customerId).get();
		int min=Integer.MAX_VALUE;
		for(Driver driver:driverList){
			if(driver.getCab().getAvailable() && driver.getDriverId()<min){
				TripBooking bookedTrip = new TripBooking();
				int bill = driver.getCab().getPerKmRate() * distanceInKm;

				bookedTrip.setCustomer(customer);
				bookedTrip.setDriver(driver);
				bookedTrip.setFromLocation(fromLocation);
				bookedTrip.setToLocation(toLocation);
				bookedTrip.setDistanceInKm(distanceInKm);
				bookedTrip.setBill(bill);
				bookedTrip.setStatus(TripStatus.CONFIRMED);

				driver.getTripBookingList().add(bookedTrip);
				driver.getCab().setAvailable(false);

				customer.getTripBookingList().add(bookedTrip);

				customerRepository2.save(customer);
				driverRepository2.save(driver);

				return bookedTrip;
			}

		}
		throw new Exception("No cab available!");
	}

	@Override
	public void cancelTrip(Integer tripId){
		//Cancel the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking trip=tripBookingRepository2.findById(tripId).get();
		trip.setStatus(TripStatus.CANCELED);
		Driver driver=trip.getDriver();
		Cab cab=driver.getCab();
		cab.setAvailable(true);
		driver.setCab(cab);
		driverRepository2.save(driver);
		tripBookingRepository2.save(trip);
	}

	@Override
	public void completeTrip(Integer tripId){
		//Complete the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking trip=tripBookingRepository2.findById(tripId).get();
		trip.setStatus(TripStatus.COMPLETED);
		Driver driver=trip.getDriver();
		Cab cab=driver.getCab();
		cab.setAvailable(true);
		driver.setCab(cab);
		driverRepository2.save(driver);
		tripBookingRepository2.save(trip);
	}
}
