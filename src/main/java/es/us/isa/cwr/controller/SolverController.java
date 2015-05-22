package es.us.isa.cwr.controller;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;

import es.us.isa.cwr.controller.handler.CplexHandler;
import es.us.isa.cwr.controller.util.OperationResponse;

/**
 * @author jdelafuente
 *
 */
@Controller
@RequestMapping("solver")
public class SolverController {

	private static final Logger LOG = Logger.getLogger(SolverController.class
			.getName());

	@RequestMapping(value = "solve", method = RequestMethod.POST)
	@ResponseBody
	public String solve(@RequestBody String content) {

		Boolean solve = false;

		if (content != null) {
			try {
				CplexHandler ch = new CplexHandler();
				ch.init();
				solve = new Gson().fromJson(ch.solve(content), Boolean.class);
			} catch (Exception e) {
				solve = false;
				LOG.log(Level.SEVERE, "There was an error processing the file",
						e);
			}
		} else {
			solve = false;
			LOG.log(Level.SEVERE, "There was an error processing the file");
		}
		
		return new Gson().toJson(solve);
	}

	@RequestMapping(value = "explain", method = RequestMethod.POST)
	@ResponseBody
	public String explain(@RequestBody String content) {

		OperationResponse response = new OperationResponse();

		if (content != null) {
			try {
				CplexHandler ch = new CplexHandler();
				ch.init();
				response = new Gson().fromJson(ch.explain(content),
						OperationResponse.class);
			} catch (Exception e) {
				response = null;
				LOG.log(Level.SEVERE, "There was an error processing the file",
						e);
			}
		} else {
			response = null;
			LOG.log(Level.SEVERE, "There was an error processing the file");
		}

		return new Gson().toJson(response);
	}

	@RequestMapping(value = "implies", method = RequestMethod.POST)
	@ResponseBody
	public String implies(@RequestBody String content) {
		Boolean compliant = false;

		if (content != null) {
			try {
				CplexHandler ch = new CplexHandler();
				ch.init();
				compliant = !(new Gson().fromJson(ch.solve(content),
						Boolean.class));

				// OperationResponse response = new
				// Gson().fromJson(ch.explain(model.toString()),
				// OperationResponse.class);

			} catch (Exception e) {
				LOG.log(Level.SEVERE, "There was an error processing the file",
						e);
			}
		} else {
			LOG.log(Level.SEVERE, "There was an error processing the file");
		}
		return new Gson().toJson(compliant);
	}

	public OperationResponse whyNotImplies() {
		throw new UnsupportedOperationException("Not supported yet."); // Todo
	}
}
