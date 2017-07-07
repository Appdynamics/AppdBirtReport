/**
 * 
 */
package org.appdynamics.metricdataset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.appdynamics.appdrestapi.data.MetricValue;
import org.appdynamics.appdrestapi.data.MetricValues;

/**
 * @author john.aronson
 *
 */
public class MetricDataset
{
	private static Logger logger = Logger.getLogger("org.appdynamics.metricdataset.MetricDataset");

	private List<String> metricHeaders = new ArrayList<>();
	private Map<String, MetricValues> metricData = new HashMap<>();
	private Set<Long> timestampSet = new HashSet<Long>();
	boolean initialized = false;
	private Long timestamps[];
	//private Pattern ptn = Pattern.compile("^(?<path>.*)\\[(?<field>[^\\]]*)\\]$");
	private Pattern ptn = Pattern.compile("^(\\[(?<app>[^\\]]*)\\])?(?<path>[^\\[]*)(\\[(?<field>[^\\]]*)\\])?$");

	public void addData(String metricHeader, MetricValues values)
	{
		if(initialized)
			throw new ConcurrentModificationException("Error: can't modify a dataset which has been initialized.");
		
		if(logger.isLoggable(Level.FINER))
			logger.finer("addData(); header: " +metricHeader +"\nvalues: " +values);
		else if(logger.isLoggable(Level.FINE))
			logger.fine("addData(); header: " +metricHeader +"\nvalue count: " +values.getMetricValue().size());
		
		if(!metricData.containsKey(metricHeader))
		{
			metricHeaders.add(metricHeader);
			metricData.put(metricHeader, values);
		}else 
			logger.info("dataset already contains metricHeader: " +metricHeader +", skipping ...");
	}
	
	private void init()
	{
		//build the list of timestamps
		for(MetricValues values : metricData.values())
		{
			if(values == null)
				continue;
			for(MetricValue mv : values.getMetricValue())
			{
				timestampSet.add(mv.getStartTimeInMillis());
			}
		}
		
		
		timestamps = timestampSet.toArray(new Long[0]);
		Arrays.sort(timestamps);

		initialized = true;
	}
	
	public boolean containsHeader(String metricHeader)
	{
		return metricData.containsKey(metricHeader);
	}
	
	public Iterator<Object[]> getIterator()
	{
		if(!initialized)
			init();
		
		return new thisIterator();
	}
	
	class thisIterator implements Iterator<Object[]> 
	{
		int metricIdx[] = new int[metricHeaders.size()];
		int rowIdx = -1;
		
		@Override
		public boolean hasNext()
		{
			return rowIdx < timestampSet.size();
		}

		@Override
		public Object[] next()
		{
			Object result[] = new Object[metricHeaders.size() +1];
			
			if(rowIdx >= 0)
				result[0] = new Date(timestamps[rowIdx]);
			else
				result[0] = "Time";
				
			for (int headerIdx = 0; headerIdx < metricHeaders.size(); headerIdx++)
			{
				if(rowIdx >= 0)
				{					
					String header = metricHeaders.get(headerIdx);
					MetricValues metricValues = metricData.get(header);
					if(metricValues != null)
					{	
						ArrayList<MetricValue> metricSet = metricValues.getMetricValue();
						if(metricIdx[headerIdx] < metricSet.size())
						{
							MetricValue metricValue = metricSet.get(metricIdx[headerIdx]);
							long timestamp = timestamps[rowIdx];
							
							//this part handles the case where different metric values sets in the table have 
							//different timestamps
							if(metricValue.getStartTimeInMillis() == timestamp)
							{
								//if the expected timestamp for the row matches this metric value go ahead and 
								//print the fields and increment the metricIdx
								//otherwise skip this one and leave metricIdx alone
								metricIdx[headerIdx]++;
								
								//parse the header to see if its a single field or all
								MetricField metricField = MetricField.Value;
								if(header.endsWith("]"))
								{
									Matcher matcher = ptn.matcher(header);
									if(matcher.matches())
										metricField = MetricField.valueOf(matcher.group("field"));
								}
								
								if(logger.isLoggable(Level.FINER))
								{
									logger.finer("next(); headerIdx: " +headerIdx +", rowIdx: " +rowIdx 
										+", metricField: " +metricField +", metricValue: " +metricValue);
								}
								
								switch(metricField)
								{
								case Count:
									result[headerIdx +1] = metricValue.getCount();
									break;
								case Current: 
									result[headerIdx +1] = metricValue.getCurrent();
									break;
								case Max:
									result[headerIdx +1] = metricValue.getMax();
									break;
								case Min:
									result[headerIdx +1] = metricValue.getMin();
									break;
								case Occurrences:
									result[headerIdx +1] = metricValue.getOccurrences();
									break;
								case StdDev:
									result[headerIdx +1] = metricValue.getStdDev();
									break;
								case Sum:
									result[headerIdx +1] = metricValue.getSum();
									break;
								case Value:
									result[headerIdx +1] = metricValue.getValue();
								}
							}
						}
					}
				}else if(rowIdx < 0)
				{
					String header = metricHeaders.get(headerIdx);
					result[headerIdx +1] = header;						
				}
			}
			
			rowIdx++;
			
			return result;
		}
	}
}
