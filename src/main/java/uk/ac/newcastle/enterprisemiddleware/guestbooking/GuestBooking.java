package uk.ac.newcastle.enterprisemiddleware.guestbooking;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import uk.ac.newcastle.enterprisemiddleware.booking.Booking;
import uk.ac.newcastle.enterprisemiddleware.customer.Customer;

@ApplicationScoped
public class GuestBooking {

    @Inject 
    private Customer customer;
    
    @Inject 
    private Booking booking;

    public Customer getCustomer() {
    	return customer;
    }
    
    public void setCustomer(Customer c) {
    	this.customer = c;
    }
    
    public Booking getBooking() {
    	return booking;
    }
    
    public void setBooking(Booking b) {
    	this.booking = b;
    }
}