/**
 * 
 */
package org.appdynamics.metricdataset;

import java.text.DateFormat;
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
 */
public class MetricDataset
{
	public static final Pattern metricNamePattern = Pattern.compile("^(\\[(?<app>[^\\]]*)\\])?(?<path>[^\\[]*)(\\[(?<field>[^\\]]*)\\])?$");
	
	private static Logger logger = Logger.getLogger("org.appdynamics.metricdataset.MetricDataset");

	protected List<String> metricHeaders = new ArrayList<String>();
	protected Map<String, MetricValues> metricData = new HashMap<String, MetricValues>();
	protected boolean initialized = false;
	protected Long timestamps[];
	protected DateFormat dateFormat = null;
	protected long startTime = -1;
	protected long endTime = -1;
	protected long minTimestampPeriod = -1;

	/**
	 * Default contructor
	 */
	public MetricDataset()
	{
	}

	
	/**
	 * Constructor that ensures the data set covers the entire time range filling with 0 records if necessary
	 * 
	 * @param startTime
	 * @param endTime
	 * @param minTimestampPeriod
	 */
	public MetricDataset(long startTime, long endTime, long minTimestampPeriod)
	{
		super();
		this.startTime = startTime;
		this.endTime = endTime;
		this.minTimestampPeriod = minTimestampPeriod;
	}


	public void addData(String metricHeader, MetricValues values)
	{
		if(initialized)
			throw new ConcurrentModificationException("Error: can't modify a dataset which has been initialized.");
		
		if(logger.isLoggable(Level.FINER))
			logger.finer("addData(); header: " +metricHeader +"\nvalues: " +values);
		else if(logger.isLoggable(Level.FINE))
			logger.fine("addData(); header: " +metricHeader +"\nvalue count: " +(values != null ? values.getMetricValue().size() : null));
		
		if(!metricData.containsKey(metricHeader))
		{
			metricHeaders.add(metricHeader);
			metricData.put(metricHeader, values);
		}else 
			logger.info("dataset already contains metricHeader: " +metricHeader +", skipping ...");
	}
	
	public DateFormat getDateFormat()
	{
		return dateFormat;
	}

	public void setDateFormat(DateFormat dateFormat)
	{
		this.dateFormat = dateFormat;
	}

	protected void init()
	{
		if(initialized)
			return;
		
		Set<Long> timestampSet = new HashSet<Long>();
		
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
		
		if(minTimestampPeriod > -1)
		{
			for(long ts = startTime; ts < endTime; ts += minTimestampPeriod)
			{
				timestampSet.add(ts);
			}
		}
		
		timestamps = timestampSet.toArray(new Long[0]);
		Arrays.sort(timestamps);

		initialized = true;
	}
	
	public List<String> getMetricHeaders()
	{
		return metricHeaders;
	}
	
	public MetricValues getMetricValues(String metricHeader)
	{
		return metricData.get(metricHeader);
	}
	
	public long getMinTimestampPeriod()
	{
		return minTimestampPeriod;
	}


	public boolean containsHeader(String metricHeader)
	{
		return metricData.containsKey(metricHeader);
	}
	
	/**
	 * Iterator will have the following form:
	 * row 0: all strings with wither 'Date' label or the header string for the column as given when the dataset was 
	 * built.
	 * rows 1 -> n: column 0: will have a java.util.Date for the row timestamp, 
	 * 				columns 1 -> y: will have a simple type [probably Long] that represents field from the MetricValue 
	 * 					associated with the cell. The field is chosen using the field part of the header string for the 
	 * 					column.
	 * 
	 * @return Iterator that contains headers and simple data values
	 */
	public Iterator<Object[]> getIterator()
	{
		init();
		
		return new SimpleIterator();
	}
	
	
	/**
	 * Iterator will have the following form:
	 * row 0: all strings with wither 'Date' label or the header string for the column as given when the dataset was 
	 * built.
	 * rows 1 -> n: column 0: will have a java.util.Date for the row timestamp, 
	 * 				columns 1 -> y: will have the MetricValue associated with the cell. The field part of the header 
	 * 				string is ignored.
	 * 
	 * @return Iterator that contains headers and simple data values
	 */
	public Iterator<Object[]> getMetricValueIterator()
	{
		init();
		
		return new MetricValueIterator();
	}
	
	/**
	 * Iterates the data set returning simple fields from the metric values using the metric field part of the header 
	 * string
	 * 
	 * @author john.aronson
	 */
	public class SimpleIterator implements Iterator<Object[]> 
	{
		int metricIdx[] = new int[metricHeaders.size()];
		int rowIdx = -1;
		
		@Override
		public boolean hasNext()
		{
			return rowIdx < timestamps.length;
		}

		@Override
		public Object[] next()
		{
			Object result[] = new Object[metricHeaders.size() +1];
			
			if(rowIdx >= 0)
			{
				result[0] = new Date(timestamps[rowIdx]);
				if(dateFormat != null)
				{
					result[0] = dateFormat.format(result[0]);
				}
			}else
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
									Matcher matcher = metricNamePattern.matcher(header);
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
							}else
							{
								// this column doesn't have a cell value for this row
								if(minTimestampPeriod > -1)
								{
									//fill with a zero
									result[headerIdx +1] = 0;
								}
							}
						}else if(minTimestampPeriod > -1)							
							result[headerIdx +1] = 0; //fill with zero at the end
					}else
						logger.warning("metricData failed to contain expected header: " +header +", metricData: " +metricData);
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
	
	/**
	 * DataSet iterator that returns the raw MetricValue objects and ignore the field part of the header string
	 * 
	 * @author john.aronson
	 *
	 */
	class MetricValueIterator implements Iterator<Object[]> 
	{
		int metricIdx[] = new int[metricHeaders.size()];
		int rowIdx = -1;
		
		@Override
		public boolean hasNext()
		{
			return rowIdx < timestamps.length;
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
									Matcher matcher = metricNamePattern.matcher(header);
									if(matcher.matches())
										metricField = MetricField.valueOf(matcher.group("field"));
								}
								
								if(logger.isLoggable(Level.FINER))
								{
									logger.finer("next(); headerIdx: " +headerIdx +", rowIdx: " +rowIdx 
										+", metricField: " +metricField +", metricValue: " +metricValue);
								}							
								result[headerIdx +1] = metricValue;
							}
						}else if(minTimestampPeriod > -1)
						{
							//if filling with zeros create an empty MetricValue here
							MetricValue mv = new MetricValue();
							mv.setStartTimeInMillis(timestamps[rowIdx]);
							mv.setCount(0);
							mv.setOccurrences(0);
							mv.setSum(0);
							result[headerIdx +1] = mv;
						}
					}else if(minTimestampPeriod > -1)
					{
						//if filling with zeros create an empty MetricValue here
						MetricValue mv = new MetricValue();
						mv.setStartTimeInMillis(timestamps[rowIdx]);
						mv.setCount(0);
						mv.setOccurrences(0);
						mv.setSum(0);
						result[headerIdx +1] = mv;
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
