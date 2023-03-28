package com.amica.billing.ws;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.amica.billing.Billing;
import com.amica.billing.Update;

import lombok.AllArgsConstructor;

/**
 * REST controller for the change history.
 *
 * @author Will Provost
 */
@RestController
@RequestMapping("history")
@AllArgsConstructor
public class HistoryController {

	public static final String DAWN_OF_TIME = "1970-01-01T00:00:00";
	private Billing billing;
	
	@GetMapping
	public Stream<Update> getHistory
		(@RequestParam(name="since", defaultValue=DAWN_OF_TIME) 
			@DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) LocalDateTime since) {
		return billing.getHistory()
				.filter(u -> u.getWhen().isAfter(since));
	}

}
