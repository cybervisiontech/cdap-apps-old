<!DOCTYPE html>
<html>
<head>
    <title>IP Anomalies</title>
    <link rel="stylesheet" type="text/css" href="css/base.css">
    <script src="http://code.jquery.com/jquery-1.9.1.js"></script>
    <script type='text/javascript' src="http://code.highcharts.com/highcharts.js"></script>
    <script type='text/javascript' src="http://code.highcharts.com/modules/exporting.js"></script>
</head>
<div style="width: 1208px; margin-left: auto; margin-right: auto; margin-top: 20px">
    <div style="width: 1204px; height: 40px; text-align: center; font-size: 110%">
        <a href="dashboard.jsp">Dashboard</a> &nbsp;| &nbsp;
        <b>Anomalies</b>
    </div>
    <div style="overflow:hidden">
        <div style="width: 399px; float: left; border: solid 1px #8FC7FF">
            <div style="width: 399px; height: 200px">
                <%@include file="chart/anomalies-count.jsp"%>
            </div>
        </div>
        <div style="width: 400px; float: left; border: solid 1px #8FC7FF; border-left: 0px">
            <div style="width: 399px; height: 200px">
                <%@include file="chart/anomalies-unique-ip-count.jsp"%>
            </div>
        </div>
        <div style="width: 400px; float: left; border: solid 1px #8FC7FF; border-left: 0px">
            <div style="width: 400px; height: 200px; overflow: hidden">
                <%@include file="chart/top-ip-anomaly.jsp"%>
            </div>
        </div>
    </div>
    <div style="overflow:hidden">
        <div style="width: 1201px; height: 640px; border: solid 1px #8FC7FF; border-top: 0px; overflow:hidden">
            <%@include file="chart/anomalies-list.jsp"%>
        </div>
    </div>
</div>
</body>
</html>