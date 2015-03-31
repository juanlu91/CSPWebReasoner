/**
 * 
 */
package es.us.isa.cwr.controller.util;

import ilog.concert.IloConstraint;
import ilog.concert.cppimpl.IloEnv;
import ilog.cp.IloCP;
import ilog.cp.IloCP.ConflictStatus;
import ilog.cplex.IloCplex;
import ilog.opl.IloOplElement;
import ilog.opl.IloOplErrorHandler;
import ilog.opl.IloOplFactory;
import ilog.opl.IloOplModel;
import ilog.opl.IloOplModelDefinition;
import ilog.opl.IloOplModelSource;
import ilog.opl.IloOplSettings;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;

import es.us.isa.aml.util.OperationResponse;

/**
 * @author jdelafuente
 *
 */
public class CplexHandler {

	private static final Logger LOG = Logger.getLogger(CplexHandler.class
			.getName());

	private IloEnv env;
	private IloOplFactory factory;
	private IloOplErrorHandler errorHandler;

	public void init() {
		IloOplFactory.setDebugMode(false);

		ByteArrayOutputStream errors = new ByteArrayOutputStream();
		env = new IloEnv();
		factory = new IloOplFactory();
		errorHandler = factory.createOplErrorHandler(errors);
		IloOplFactory.setDebugMode(false);
	}

	public String solve(String content) {
		Date date = new Date();
		File temp;

		Boolean solve = false;

		try {
			temp = File.createTempFile(String.valueOf(date.getTime()), ".opl");
			BufferedWriter bw = new BufferedWriter(new FileWriter(temp));
			content = URLDecoder.decode(content, "UTF-8");
			bw.write(content);
			bw.close();

			IloOplModelSource modelSource = factory.createOplModelSource(temp
					.getAbsolutePath());

			IloOplSettings settings = new IloOplSettings(env, errorHandler);
			IloOplModelDefinition def = factory.createOplModelDefinition(
					modelSource, settings);

			String using = content.substring(0, content.indexOf('\n')).trim();
			Boolean useCP = using.equals("using CP;");

			if (useCP) {
				IloCP cp = factory.createCP();
				cp.setOut(null);
				// cp.setParameter(IntParam.ConflictRefinerOnVariables,
				// ParameterValues.On);
				IloOplModel opl = factory.createOplModel(def, cp);

				opl.generate();
				solve = cp.solve();

				cp.clearModel();
				opl.endAll();

			} else {
				IloCplex cplex = factory.createCplex();
				cplex.setOut(null);
				IloOplModel opl = factory.createOplModel(def, cplex);
				opl.generate();
				solve = cplex.solve();

				cplex.clearModel();
				opl.endAll();
			}

		} catch (IOException e) {
		} catch (Error | Exception e) {
			solve = false;
			LOG.log(Level.SEVERE, "There was an error processing the file", e);
		}

		return new Gson().toJson(solve);
	}

	public String explain(String content) {
		OperationResponse response = new OperationResponse();

		Boolean solve = false;
		String result = "";
		Map<String, List<String>> conflictsMap = new HashMap<>();

		Date date = new Date();
		File temp;

		try {
			temp = File.createTempFile(String.valueOf(date.getTime()), ".opl");
			BufferedWriter bw = new BufferedWriter(new FileWriter(temp));

			content = URLDecoder.decode(content, "UTF-8");
			bw.write(content);
			bw.close();

			ByteArrayOutputStream errors = new ByteArrayOutputStream();
			IloEnv env = new IloEnv();
			IloOplFactory oplFactory = new IloOplFactory();
			IloOplErrorHandler errHandler = oplFactory
					.createOplErrorHandler(errors);
			IloOplModelSource modelSource = oplFactory
					.createOplModelSource(temp.getAbsolutePath());

			IloOplSettings settings = new IloOplSettings(env, errHandler);
			IloOplModelDefinition def = oplFactory.createOplModelDefinition(
					modelSource, settings);

			String using = content.substring(0, content.indexOf('\n')).trim();
			Boolean useCP = using.equals("using CP;");

			if (useCP) {
				IloCP cp = oplFactory.createCP();
				cp.setOut(null);
				// cp.setParameter(IntParam.ConflictRefinerOnVariables,
				// ParameterValues.On);
				IloOplModel opl = oplFactory.createOplModel(def, cp);

				opl.generate();
				solve = cp.solve();

				List<IloConstraint> cts_list = new ArrayList<>();
				for (@SuppressWarnings("unchecked")
				Iterator<IloOplElement> it = opl.getElementIterator(); it
						.hasNext();) {
					IloOplElement e = it.next();
					if (!e.isDecisionVariable() && !e.isData()
							&& !e.isCalculated()) {
						IloConstraint c = e.asConstraint();
						c.setName(e.getName());
						cts_list.add(c);
					}
				}

				IloConstraint[] constraints = cts_list
						.toArray(new IloConstraint[cts_list.size()]);
				double[] prefs = new double[constraints.length];
				for (int p = 0; p < constraints.length; p++) {
					prefs[p] = 1.0;
				}

				try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
					if (solve) {
						opl.printSolution(baos);
						result = baos.toString();
					} else {
						result = "The document has conflicts";
						if (cp.refineConflict(constraints, prefs)) {
							List<String> provedConflicts = new LinkedList<>();
							List<String> possibleConflicts = new LinkedList<>();
							for (IloConstraint constraint : constraints) {
								ConflictStatus cs = cp.getConflict(constraint);
								if (cs.equals(ConflictStatus.ConflictMember)) {
									provedConflicts.add(constraint.getName());
								} else if (cs
										.equals(ConflictStatus.ConflictPossibleMember)) {
									possibleConflicts.add(constraint.getName());
								}
							}
							conflictsMap
									.put("provedConflicts", provedConflicts);
							conflictsMap.put("possibleConflicts",
									possibleConflicts);
						}
					}
				}
				cp.clearModel();
				opl.endAll();

			} else {
				IloCplex cplex = oplFactory.createCplex();
				cplex.setOut(null);
				IloOplModel opl = oplFactory.createOplModel(def, cplex);
				opl.generate();
				solve = cplex.solve();

				try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
					if (solve) {
						opl.printSolution(baos);
						result = baos.toString();
					} else {
						result = "The document has conflicts";
						opl.printConflict(baos);
						List<String> aux = new ArrayList<String>();
						aux.add(baos.toString());
						conflictsMap.put("conflicts", aux);
					}
				}
				cplex.clearModel();
				opl.endAll();
			}

		} catch (Error | Exception e) {
			result = "ERROR";
			LOG.log(Level.SEVERE, "There was an error processing the file", e);
		}

		response.put("result", result);
		response.put("conflicts", conflictsMap);
		return new Gson().toJson(response);
	}
}
