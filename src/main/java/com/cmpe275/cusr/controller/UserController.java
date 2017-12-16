package com.cmpe275.cusr.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.cmpe275.cusr.model.OneWayList;
import com.cmpe275.cusr.model.OneWayTrip;
import com.cmpe275.cusr.model.SearchContent;
import com.cmpe275.cusr.service.TrainService;
import com.cmpe275.cusr.service.UserService;


@Controller
@SessionAttributes(value={"oneWayList", "searchContent", "oneWayTrip", "returnWayList"})
public class UserController {
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private TrainService trainService;
	
	@GetMapping("/signin")
	public String signinSuccessGet(Model model, @RequestParam(value="firebaseToken", required=true) String firebaseToken) {
		
		userService.signInAuthentication(firebaseToken);
		
		return "redirect:/tickets";
	}
	
	@GetMapping("/search")
	public String index(Model model) {
		SearchContent search = new SearchContent();
		model.addAttribute("searchContent", search);
		return "search";
	}
	
	//show search results after login
	@PostMapping("/search")
	public String searchTrip(@ModelAttribute SearchContent search, Model model) {
		
				//test for trainServiceImpl
				OneWayList dbResult = trainService.searchOneWay(search);
				model.addAttribute("oneWayList", dbResult);
				
				//add search inquiry in the view
				model.addAttribute("searchContent", search);
				return "searchResult";
	}
	
	@PostMapping("/selectReturn")
	public String bookingConfirm(@ModelAttribute("oneWayList") OneWayList oneWayList, 
								@ModelAttribute("searchContent") SearchContent search,
								@RequestParam("Select") String selectTrip, Model model) {
		int tripIndexSelected = Integer.valueOf(selectTrip.substring(4, 5))-1;
		OneWayTrip forwardTrip = oneWayList.getFirstFive().get(tripIndexSelected);
		model.addAttribute("oneWayTrip", forwardTrip);
		
		
		OneWayList returnResult = trainService.searchOneWay(search.getReturnSearch());
		model.addAttribute("returnWayList", returnResult);
		
		return "selectReturn";
	}
	
	@PostMapping("/purchase")
	public String select(@ModelAttribute("oneWayTrip") OneWayTrip forwardTrip, 
						@ModelAttribute("searchContent") SearchContent search,
						@ModelAttribute("returnWayList") OneWayList returnWayList,
						@RequestParam("Select") String selectTrip, Model model) { 
		
		//obtain return trip object
		int tripIndexSelected = Integer.valueOf(selectTrip.substring(4, 5))-1;
		OneWayTrip returnTrip = returnWayList.getFirstFive().get(tripIndexSelected);
		
		//fill in model
		int numOfSeats = search.getNumberOfSeats();
		model.addAttribute("numOfSeats", numOfSeats);
		List<String> passenger = new ArrayList<>();
		for (int i = 0; i < numOfSeats; ++i) {
			passenger.add("");
		}
		model.addAttribute("passenger", passenger);
		
		double price= forwardTrip.getTicketPrice() + returnTrip.getTicketPrice();
		model.addAttribute("price", price);
		model.addAttribute("totalPrice", price + 1);
		model.addAttribute("departureDate", search.getDepartureDate());
		
		model.addAttribute("departureTrip", forwardTrip.getConnections());
		String returnDate = search.getReturnDate();
		model.addAttribute("round", search.isRoundTrip()? "Y" : "N");
		if (returnDate != null) {
			model.addAttribute("returnDate", returnDate);
			model.addAttribute("returnTrip", returnTrip.getConnections());
		}
		return "purchase";
	}
	
	
}
