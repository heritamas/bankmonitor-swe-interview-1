package bankmonitor.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping("/")
@Deprecated
public class TransactionController {

	private final Logger logger = LoggerFactory.getLogger(TransactionController.class);

	@RequestMapping(value = "/transactions/**")
	public RedirectView redirect(RedirectAttributes attributes)  {
		// redirect to /v1/transactions
		logger.info("Redirecting to /api/v1/transactions");
		return new RedirectView("/api/v1/transactions", true);
	}

}