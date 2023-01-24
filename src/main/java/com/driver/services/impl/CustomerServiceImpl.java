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
		List<Driver> drivers = driverRepository2.findAll();
		int min = Integer.MAX_VALUE;
		Driver driver1 = null;

		for(Driver driver:drivers){
			if(driver.getCab().getAvailable() && driver.getDriverId()<min){
				min = driver.getDriverId();
				driver1 = driver;
			}
		}

		if(min < Integer.MAX_VALUE && driver1!=null){
			Customer customer = customerRepository2.findById(customerId).get();

			TripBooking tripBooking = new TripBooking();
			int bill = driver1.getCab().getPerKmRate() * distanceInKm;

			tripBooking.setCustomer(customer);
			tripBooking.setDriver(driver1);
			tripBooking.setFromLocation(fromLocation);
			tripBooking.setToLocation(toLocation);
			tripBooking.setDistanceInKm(distanceInKm);
			tripBooking.setBill(bill);
			tripBooking.setStatus(TripStatus.CONFIRMED);

			driver1.getTripBookingList().add(tripBooking);
			driver1.getCab().setAvailable(false);

			customer.getTripBookingList().add(tripBooking);

			tripBookingRepository2.save(tripBooking);
			customerRepository2.save(customer);
			driverRepository2.save(driver1);

			return tripBooking;
		}
		else
			throw new Exception("No cab available!");


//		List<Driver> driverList=driverRepository2.findAll();
//		Customer customer = customerRepository2.findById(customerId).get();
//		int min=Integer.MAX_VALUE;
//		Driver driver1=new Driver();
//		for(Driver driver:driverList){
//			if(driver.getCab().getAvailable() && driver.getDriverId()<min){
//				min = driver.getDriverId();
//				driver1 = driver;
//			}
//		}
//		if(min<Integer.MAX_VALUE){
//				TripBooking bookedTrip=new TripBooking(fromLocation,toLocation,distanceInKm);
//				int bill=driver1.getCab().getPerKmRate() * distanceInKm;
//				bookedTrip.setCustomer(customer);
//				bookedTrip.setDriver(driver1);
//				bookedTrip.setStatus(TripStatus.CONFIRMED);
//				bookedTrip.setBill(bill);
//				tripBookingRepository2.save(bookedTrip);

//				List<TripBooking> driverTrips=driver1.getTripBookingList();
//				if(driverTrips==null)
//					driverTrips=new ArrayList<>();
//				driverTrips.add(bookedTrip);
//				driver1.setTripBookingList(driverTrips);
//				driver1.getCab().setAvailable(false);
//				driverRepository2.save(driver1);
//
//				List<TripBooking> customerTrips=customer.getTripBookingList();
//				if(customerTrips==null)
//					customerTrips=new ArrayList<>();
//				customerTrips.add(bookedTrip);
//				customer.setTripBookingList(customerTrips);
//				customerRepository2.save(customer);
//
//				return bookedTrip;
//		}
//
//		throw new Exception("No cab available!");
	}

	@Override
	public void cancelTrip(Integer tripId){
		//Cancel the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking trip=tripBookingRepository2.findById(tripId).get();
		trip.setStatus(TripStatus.CANCELED);
		trip.setBill(0);
		Driver driver=trip.getDriver();
		driver.getCab().setAvailable(true);
		tripBookingRepository2.save(trip);
	}

	@Override
	public void completeTrip(Integer tripId){
		//Complete the trip having given trip Id and update TripBooking attributes accordingly
		if(tripBookingRepository2.findById(tripId).isPresent()) {
			TripBooking trip = tripBookingRepository2.findById(tripId).get();
			trip.setStatus(TripStatus.COMPLETED);
			Driver driver = trip.getDriver();
			driver.getCab().setAvailable(true);
			tripBookingRepository2.save(trip);
		}
	}
}
