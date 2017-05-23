# AppdBirtReport
Tools to enable BIRT reporting of AppDynamics data
Current Tools
 * AppdReportGen package contains code that will run on the command line,
generate a BIRT report and email the result. 

 * AppdRestDataset project to make REST calls via AppDRESTAPI-SDK and
present the output as columns in a table

 * Sample Reports 
  1) appd_metric_report - presents results from up to 10 metric calls,
aggregates them by time and formats the output
  2) appd_user_audit - proof concept for a simple user audit report using
the controller mysql database as a data source

*AppdReportGen Usage*

1) Download https://github.com/Appdynamics/AppdBirtReport/blob/master/AppdReportGen/deploy/AppdReportGen_1-0.tar.gz and transfer to the server where you want to generate the reports from the command line
2) untar the package
3) Test "java -version". If version 8+ isnt in the path, then edit AppdReportGen.sh to point JAVA_HOME to a local java 8+ install.
4) Edit appd-report-params-demo1.properties file
	a) If sending email update the AppdReportGen.* and mail.* properties for your SMTP server and email content. If not sending email, set the AppdReportGen.sendEmail property to false
	b) Set the design preoprty, by default its the appd_metric_report.rptdesign, the rest of these instructions assume that design is being used. This report presents the out of 1-10 metric queries made via REST to a controller.
	c) Set the Controller* parameters for your controller. Also set the user, password, account parameters to values that allow the report to authenticate with the controller and make the REST call[s].
	d) Copy metric path[s] from metric browser in the UI into Path* parameters and update ColHeader param to match
	e) Set the TimeRange using the StartTime and DaysBack parameters
	f) Set the time aggregation and display format parameters. GroupFormat controls how the Time column of the output is presented in the report. SortFormat controlls how the time data is aggregated. This part uses standard java format strings, see here for more info https://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html
5) Run the program "./AppdReportGen.sh -p appd-report-params-demo1.properties"

Sample Output 
<pre>
>./AppdReportGen.sh -p appd-report-params-demo1.properties 
Tue May 23 13:26:55 EDT 2017
May 23, 2017 1:26:55 PM org.appdynamics.birtreport.AppdReportGen runReport
INFO: running the report
May 23, 2017 1:27:02 PM org.mozilla.javascript.MemberBox invoke
INFO: processing report args
May 23, 2017 1:27:02 PM org.mozilla.javascript.MemberBox invoke
INFO: creating dataset
May 23, 2017 1:27:04 PM org.mozilla.javascript.MemberBox invoke
INFO: processing data rows
May 23, 2017 1:27:05 PM org.mozilla.javascript.MemberBox invoke
INFO: processed data rows
May 23, 2017 1:27:05 PM org.appdynamics.birtreport.AppdReportGen runReport
INFO: report complete
May 23, 2017 1:27:05 PM org.appdynamics.birtreport.EmailUtil sendMultiPartEmail
INFO: email message created
May 23, 2017 1:27:07 PM org.appdynamics.birtreport.EmailUtil sendMultiPartEmail
INFO: email sent
</pre>	
