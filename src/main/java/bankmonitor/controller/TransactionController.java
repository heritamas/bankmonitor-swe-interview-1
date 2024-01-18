package bankmonitor.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/")
@Deprecated
public class TransactionController {

	@RequestMapping(value = "/transactions/**")
	public String redirect(HttpServletRequest request) {
		// redirect to /v1/transactions
		String newUrl = request.getRequestURI().replace("/transactions", "/api/v1/transactions");
		//response.sendRedirect(newUrl);
		return "redirect:" + newUrl;
	}

}