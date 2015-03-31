package es.us.isa.cwr.controller;

import ilog.concert.cppimpl.IloEnv;
import ilog.cp.IloCP;
import ilog.cplex.IloCplex;
import ilog.opl.IloOplFactory;
import ilog.opl.IloOplModel;
import ilog.opl.IloOplModelDefinition;
import ilog.opl.IloOplModelSource;
import ilog.opl.IloOplSettings;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URLDecoder;
import java.util.Date;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;

import es.us.isa.cwr.controller.error.CustomErrorHandler;


/**
 * @author jdelafuente
 *
 */
@Controller
@RequestMapping("language")
public class LanguageController {
	
	@RequestMapping(value = "check", method = RequestMethod.POST)
	@ResponseBody
	public Object checkSyntax(@RequestParam String content) {

		Gson gson = new Gson();
		
		IloEnv env = new IloEnv();
		IloOplFactory oplF = new IloOplFactory();
		CustomErrorHandler errorHandler = new CustomErrorHandler(oplF);
		
		Date date = new Date();
		File temp;
		
		try {			
			temp = File.createTempFile(String.valueOf(date.getTime()), ".opl");
			BufferedWriter bw = new BufferedWriter(new FileWriter(temp));
			
			content = URLDecoder.decode(content, "UTF-8");
			bw.write(content);
			bw.close();
			
			IloOplModelSource modelSource = oplF.createOplModelSource(temp.getAbsolutePath());
			
			IloOplSettings settings = new IloOplSettings(env, errorHandler);
			IloOplModelDefinition def = oplF.createOplModelDefinition(
					modelSource, settings);

			String using = content.substring(0, content.indexOf("\n"))
					.trim();
			Boolean useCP = using.equals("using CP;") ? true : false;

			if (useCP) {
				IloCP cp = oplF.createCP();
				cp.setOut(null);
				IloOplModel oplModel = oplF.createOplModel(def, cp);
				oplModel.generate();
			} else {
				IloCplex clex = oplF.createCplex();
				clex.setOut(null);
				IloOplModel oplModel = oplF.createOplModel(def, clex);
				oplModel.generate();
			}
		} catch(Exception e){
		}
		
		return gson.toJson(errorHandler.getAnnotations());
	}
}
