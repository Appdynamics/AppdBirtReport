package org.appdynamics.metricdataset;
import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.appdynamics.appdrestapi.RESTAccess;
import org.appdynamics.appdrestapi.data.MetricData;
import org.appdynamics.appdrestapi.data.MetricDatas;
import org.appdynamics.appdrestapi.data.MetricItem;
import org.appdynamics.appdrestapi.data.MetricItems;
import org.appdynamics.appdrestapi.data.MetricValues;
import org.appdynamics.appdrestapi.util.TimeRange;
import org.appdynamics.appdrestapi.util.TimeRangeHelper;

/**
 * 
 */

/**
 * @author john.aronson
 *
 */
public class MetricQuery
{

	private static Pattern ptn = Pattern.compile("^(\\[(?<app>[^\\]]*)\\])?(?<path>[^\\[]*)(\\[(?<field>[^\\]]*)\\])?$");
	private static Logger logger = Logger.getLogger("org.appdynamics.metricdataset.MetricQuery");

	/**
	 * @param args
	 * @throws ParseException 
	 */
	public static void main(String[] args) throws ParseException
	{
		int idx = 0;
		String controllerHost = (args.length > idx++ ? args[idx-1] : "primarycontrollerc-johnatestbp2-kzdyyozq.srv.ravcloud.com");
		String controllerPort = (args.length > idx++ ? args[idx-1] : "8090");
		boolean controllerSsl = (args.length > idx++ ? Boolean.getBoolean(args[idx-1]) : false);
		String userId = (args.length > idx++ ? args[idx-1] : "admin");
		String userPass = (args.length > idx++ ? args[idx-1] : "Appd-admin");
		String userAcct = (args.length > idx++ ? args[idx-1] : "customer1");
		String app = (args.length > idx++ ? args[idx-1] : null);
		
		TimeRange range = null;
		if(args.length > idx +1)
		{
			DateFormat df =  SimpleDateFormat.getDateTimeInstance();
			range = TimeRangeHelper.getTimeRange(df.parse(args[idx++]).getTime(), df.parse(args[idx++]).getTime());
		}else
		{
			long currentTimeMillis = System.currentTimeMillis();
			range = TimeRangeHelper.getTimeRange(currentTimeMillis -1440l*60*1000, currentTimeMillis);
			idx +=2;
		}
		
		String metricPaths[] = null;
		if(args.length > idx)
		{
			int startIdx = idx;
			metricPaths = new String [args.length -idx];
			for( ; idx < args.length; idx++)
				metricPaths[idx -startIdx] = args[idx]; 
		}else
		{
			metricPaths = new String[] 
    		{ 
       			"[BDR5]Business Transaction Performance|Business Transaction Groups|indoor2_group|Calls per Minute[Sum]", 
       			"[BDR5]Business Transaction Performance|Business Transaction Groups|indoor2_group|Number of Very Slow Calls" 
			};
		}
		
		MetricQuery exporter = new MetricQuery();
		System.exit(exporter.run(controllerHost, controllerPort, controllerSsl, userId, userPass, userAcct, app, range, metricPaths));
	}
	
	private int run(String controllerHost, String controllerPort, boolean controllerSsl, String userId, String userPass, String userAcct, String app, TimeRange range, String metricPaths[])
	{
		try
		{	        			
			MetricDataset dataset = queryData(controllerHost, controllerPort, new Boolean(controllerSsl), userId, userPass, userAcct, metricPaths, app, range);
			
			//output the data
			File tempFile = File.createTempFile("metricExporter", ".csv", new File("/tmp"));
			logger.info("output: " +tempFile.getAbsolutePath());
			FileWriter fw = new FileWriter(tempFile);
			CSVPrinter out = new CSVPrinter(fw, CSVFormat.RFC4180.withFirstRecordAsHeader());
			
			Iterator<Object[]> datasetIt = dataset.getIterator();
			while(datasetIt.hasNext())
			{
				Object row[] = datasetIt.next();
				for (int headerIdx = 0; headerIdx < row.length; headerIdx++)
				{
					out.print(row[headerIdx]);
				}
				out.println();
			}
			out.flush();
			out.close();
			
		}catch(Exception ex)
		{
			ex.printStackTrace(System.err);
			return 1;
		}
		return 0;
	}

	public static MetricDataset queryData(String controllerHost, String controllerPort, Boolean controllerSsl, String userId, String userPass, String userAcct, Object inMetricPath, Object app, TimeRange range)
	{
		String metricPaths[] = new String[1];
		metricPaths[0] = inMetricPath.toString();
		if(app == null)
			app = "";

		return queryData(controllerHost, controllerPort, controllerSsl.booleanValue(), userId, userPass, userAcct, metricPaths, app.toString(), range);
	}

	public static MetricDataset queryData(String controllerHost, String controllerPort, Boolean controllerSsl, String userId, String userPass, String userAcct, Object[] inMetricPaths, String app, TimeRange range)
	{
		String metricPaths[] = new String[inMetricPaths.length];
		for (int j = 0; j < inMetricPaths.length; j++)
			metricPaths[j] = inMetricPaths[j].toString();

		return queryData(controllerHost, controllerPort, controllerSsl.booleanValue(), userId, userPass, userAcct, metricPaths, app, range);
	}

	public static MetricDataset queryData(String controllerHost, String controllerPort, boolean controllerSsl, String userId, String userPass, String userAcct, String[] metricPaths, String app, TimeRange range)
	{
		RESTAccess access = new RESTAccess(controllerHost, controllerPort, controllerSsl, userId, userPass, userAcct);

		MetricDataset dataset = new MetricDataset();
		
		for (int pathIdx = 0; pathIdx < metricPaths.length; pathIdx++)
		{
			String metricPath = metricPaths[pathIdx];
			String metricApp = app;
			
			Matcher matcher = ptn.matcher(metricPath);
			if(matcher.matches())
			{
				if(matcher.start("app") > -1)
					metricApp = matcher.group("app");
				metricPath = matcher.group("path").trim();
			}else
			{
				logger.warning("metricPath failed to match regex: " +metricPath);
			}
			
		
			boolean foundPath = false;
		    int idx = metricPath.lastIndexOf('|');
		    if(idx > 0)
		    {	
				MetricItems items = access.getBaseMetricListPath(metricApp, metricPath.substring(0, idx));
				if(items == null)
				{
					logger.warning("bad metric path: " +metricPaths[pathIdx] +", app:" +metricApp 
						+", metric base: " +metricPath.substring(0, idx));
				}else if(!items.getMetricItems().isEmpty())
				{	
			        for (MetricItem item : items.getMetricItems())
			        {
			        	//System.out.println(item.getName());
			        	//System.out.println(item.getType());
			        	if(item.getName().endsWith(metricPath.substring(idx +1)))
			        	{
			        		//test metric field here
			        		String fieldName = matcher.group("field");
			        		try
							{
								if (fieldName != null)
									MetricField.valueOf(fieldName);

								foundPath = true;
								break;
							} catch (Exception e)
							{
								logger.info("no matching field found for param: " +metricPaths[pathIdx] +", exception: " +e);
							}
			        	}
			        }
				}
				
				//get the data
				if(foundPath)
				{
					MetricDatas datas = access.getRESTGenericMetricQuery(metricApp, metricPath, range.getStart(), range.getEnd(), false);
					for(MetricData md : datas.getMetric_data())
					{
						if(!md.hasNoValues())
						{
							if(md.getMetricValues().size() == 1)
							{
								MetricValues metricValues = md.getFirstMetricValues();
								dataset.addData(metricPaths[pathIdx], metricValues);
							}else if(md.getMetricValues().size() > 0)
							{
								for (int valueIdx = 0; valueIdx < md.getMetricValues().size(); valueIdx++)
								{
									MetricValues metricValues = md.getMetricValues().get(valueIdx);
									dataset.addData(metricPaths[pathIdx], metricValues);
								}
							}
						} else
							logger.warning("no metric data found for param: " +metricPaths[pathIdx]);
					}
				}else
					logger.warning("no matching app/metric/field found for param: " +metricPaths[pathIdx]);
				
				//if the query failed, add the header as a placeholder
				if(!dataset.containsHeader(metricPaths[pathIdx]))
					dataset.addData(metricPaths[pathIdx], null);
		    }
		}
		return dataset;
	}

}
