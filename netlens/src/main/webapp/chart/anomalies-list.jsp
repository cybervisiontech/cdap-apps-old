<div class="table_container">
    <div class="table_title">
        Anomalies
    </div>
    <div id="anomaliesList" style="width: 100%; height: 100%; overflow: scroll;"></div>
</div>

<script type="text/javascript">
    $(function() {
        reloadAnomaliesList();
    });

    function reloadAnomaliesList() {
        drawAnomaliesList();
        setTimeout(function() {
            reloadAnomaliesList();
        }, 5000);
    }

    function drawAnomaliesList() {
        var startTs = Date.now() - 5000 * 120;
        var endTs = Date.now();
        var shorten = '<%= request.getAttribute("shorten")%>';
        var params = shorten == 'null' ?
                "{startTs:" + startTs + ", endTs:" + endTs + "}" :
                "{startTs:" + startTs + ", endTs:" + endTs + ", groupFor: 'src'}";

            $.post( "proxy/v2/apps/Netlens/procedures/AnomaliesProcedure/methods/timeRange", params)
                .done(function( data ) {
                    anomalies = JSON.parse(JSON.parse(data));

                    tableHtml =
                            "<table id='anomalies_table' class='anomalies_table' align='center'>" +
                                "<tr class='anomalies_table_header'>";
                    tableHtml +=
                                "<td style='display: none'></td>" +
                                "<td class='cell'>Time</td>" +
                                "<td class='cell'>Source IP</td>" +
                                "<td class='cell'>Source Port</td>" +
                                "<td class='cell'>Destination IP</td>" +
                                "<td class='cell'>Destination Port</td>" +
                                "<td class='cell'>Traffic Type</td>";
                    if (shorten == 'null') {
                        tableHtml +=
                                    "<td class='cell'>Latency</td>" +
                                    "<td class='cell'>Packet Size</td>" +
                                    "<td class='cell'>IPv</td>" +
                                    "<td class='cell'>atz</td>" +
                                    "<td class='cell'>dtz</td>";
                    }

                    tableHtml +=
                                "</tr>";
                    if (anomalies.length > 0) {
                        for (i = 0; i < anomalies.length; i++) {
                            tableHtml += i % 2 == 0 ? "<tr>" : "<tr class='anomalies_table_even'>";
                            // Link
                            var params = $.param ({
                                key: anomalies[i].dataSeriesKey,
                                fact: JSON.stringify(anomalies[i].fact)
                            });
                            tableHtml += "<td style='display: none'>ip-details.jsp?" + params + "</td>";
                            // Time
                            tableHtml += td(new Date(anomalies[i].fact.ts).toLocaleTimeString());
                            // Source IP
                            tableHtml += td(anomalies[i].fact.dimensions.src);
                            // Source Port
                            tableHtml += td(anomalies[i].fact.dimensions.spt);
                            // Destination IP
                            tableHtml += td(anomalies[i].fact.dimensions.dst);
                            // Destination Port
                            tableHtml += td(anomalies[i].fact.dimensions.dpt);
                            // Traffic Type
                            tableHtml += td(anomalies[i].fact.dimensions.app);
                            if (shorten == 'null') {
                                // latency
                                tableHtml += td(anomalies[i].fact.dimensions.rl);
                                // size
                                tableHtml += td(anomalies[i].fact.dimensions.rs);
                                // IPv
                                tableHtml += td(anomalies[i].fact.dimensions.ipv);
                                // atz
                                tableHtml += td(anomalies[i].fact.dimensions.atz);
                                // dtz
                                tableHtml += td(anomalies[i].fact.dimensions.dtz);
                            }

                            tableHtml += "</tr>";
                        }
                    } else {
                        tableHtml += "<tr>" + td("&nbsp;") + td("") + td("") + td("") + td("") + td("");
                        if (shorten == 'null') {
                            tableHtml += td("") + td("") + td("") + td("") + td("") + "</tr>";
                        }
                        tableHtml += "</tr>";
                    }

                    tableHtml += "</table>";
                    $("#anomaliesList").html(tableHtml);

                    if (anomalies.length > 0) {
                        $('#anomalies_table tbody').on('click', 'tr', function () {
                            var url = $('td', this).eq(0).text();
                            window.location.href=url;
                        } );
                    }
                })
                .fail( function(xhr, textStatus, errorThrown) {
                    $('#anomaliesList').html("<div class='server_error''>Failed to get data from server<div>");
                })
    }

    function td(html) {
        return "<td class='cell'>" + (html == null ? "<div style='color: #888'><i>[agg]</i></div>" : html) + "</td>";
    }

</script>