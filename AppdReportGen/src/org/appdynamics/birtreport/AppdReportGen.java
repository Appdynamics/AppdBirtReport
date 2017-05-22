/**
 * 
 */
package org.appdynamics.birtreport;

import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Logger;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.eclipse.birt.report.engine.api.ReportRunner;

/**
 * @author john.aronson
 *
 */
public class AppdReportGen
{
	private static final String PARAM_PREFIX = "RunReport.";
	private static final int IDX_ARG_DESIGN = 6;
	private static final int IDX_ARG_PARAMETERS = 5;
	private static final int IDX_ARG_FORMAT = 3;
	private static final int IDX_ARG_OUTPUT = 1;
	private static final String OPTION_D = "d";
	private static final String OPTION_F = "f";
	private static final String OPTION_O = "o";
	private static final String OPTION_P = "p";
	private static final String OPTION_DESIGN = "design";
	private static final String OPTION_FORMAT = "format";
	private static final String OPTION_OUTPUT = "output";
	private static final String OPTION_PARAMETERS = "parameters";
	private CommandLine commandLine;

	public AppdReportGen(String[] args) throws ParseException
	{
		Logger.getAnonymousLogger().fine("command line args:" +Arrays.toString(args));
		
		Options options = new Options();
		options.addOption(OPTION_P, OPTION_PARAMETERS, true, "location of the parameters file for the program and report");
		options.addOption(OPTION_O, OPTION_OUTPUT, true, "location of the report output file");
		options.addOption(OPTION_F, OPTION_FORMAT, true, "report output format [html, xls, pdf, cvs, ...]");
		options.addOption(OPTION_D, OPTION_DESIGN, true, "location of the report design file");
		//TODO add more options from the parameters file
		CommandLineParser clp = new BasicParser();
		commandLine = clp.parse(options, args, true);
	}

	public int runReport() throws Exception 
	{	
		Properties props = new Properties();
		File paramFile = null;
		
		if(commandLine.hasOption(OPTION_P))
			paramFile = new File(commandLine.getOptionValue(OPTION_P));
		if(commandLine.hasOption(OPTION_PARAMETERS))
			paramFile = new File(commandLine.getOptionValue(OPTION_PARAMETERS));
		
		if(paramFile == null || !paramFile.exists())
			throw new Exception("Invalid report parameter file location: " +paramFile);
		
		//get the parameters cmd arg and load up the props from it
		props.load(new FileReader(paramFile));
		
		//override the props from the cmd args
		Option parsedOptions[] = commandLine.getOptions();
		for (int i = 0; i < parsedOptions.length; i++)
			props.put(parsedOptions[i].getLongOpt(), parsedOptions[i].getValue());
		
		
		//create runner args
		String runnerArgs[] = new String[] { "-o", null, "-f", null, "-F", null, null };
		runnerArgs[IDX_ARG_FORMAT] = props.getProperty(OPTION_FORMAT);
		runnerArgs[IDX_ARG_PARAMETERS] = props.getProperty(OPTION_PARAMETERS);
		runnerArgs[IDX_ARG_OUTPUT] = props.getProperty(OPTION_OUTPUT);
		runnerArgs[IDX_ARG_DESIGN] = props.getProperty(OPTION_DESIGN);	
		
		if(runnerArgs[IDX_ARG_OUTPUT] == null)
		{
			runnerArgs[IDX_ARG_OUTPUT] = File.createTempFile(this.getClass().getSimpleName(), "."+runnerArgs[IDX_ARG_FORMAT].toLowerCase()).getAbsolutePath();
			Logger.getAnonymousLogger().fine("created output file" +runnerArgs[IDX_ARG_OUTPUT]);
		}
		
		//validate runner args
		File outputFile = new File(runnerArgs[IDX_ARG_OUTPUT]);
		if(runnerArgs[IDX_ARG_OUTPUT] == null || !outputFile.getParentFile().exists())
			throw new Exception("Invalid report output file location: " +runnerArgs[IDX_ARG_OUTPUT]);
		if(runnerArgs[IDX_ARG_DESIGN] == null || !new File(runnerArgs[IDX_ARG_DESIGN]).exists())
			throw new Exception("Invalid report design file location: " +runnerArgs[IDX_ARG_DESIGN]);		
		if(runnerArgs[IDX_ARG_PARAMETERS] == null || !paramFile.exists())
			throw new Exception("Invalid report parameter file location: " +runnerArgs[IDX_ARG_PARAMETERS]);
		
		Logger.getAnonymousLogger().info("running the report");
		
		//run the report
		ReportRunner runner = new ReportRunner(runnerArgs);
		int rtn = runner.execute();
		if(rtn != 0)
			throw new Exception("ERROR: ReportRunner step reported a problem. See the birt logs for more details.");

		Logger.getAnonymousLogger().info("report complete");
				
		if (Boolean.parseBoolean(props.getProperty(PARAM_PREFIX+"sendEmail", "true")))
		{
			//email the results
			EmailUtil.sendMultiPartEmail(props, props.getProperty(PARAM_PREFIX+"toEmail"), 
				props.getProperty(PARAM_PREFIX+"fromEmail"), props.getProperty(PARAM_PREFIX+"emailPassword"), 
				props.getProperty(PARAM_PREFIX+"subject"), props.getProperty(PARAM_PREFIX+"body"), outputFile);
		}		
		return rtn;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		AppdReportGen runner = null;
		try 
		{
			runner = new AppdReportGen(args);
			int result = runner.runReport();
			if(result != 0) 
				System.err.println("Error detected while running report! See the logs for more info...");
			System.exit(result);
		} catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
}
