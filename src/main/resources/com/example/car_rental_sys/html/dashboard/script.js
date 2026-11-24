let avtivityBox = document.getElementById("right-bottom-box");

let hd1 = document.getElementById("headerData-1");
let hd2 = document.getElementById("headerData-2");
let hd3 = document.getElementById("headerData-3");

function addHeaderData(headerData) {
    if (!headerData) return;
    let hd1Text = (headerData[0] || 0).toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
    let hd2Text = (headerData[1] || 0).toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
    let hd3Text = (headerData[2] || 0).toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");

    if (hd1) hd1.innerHTML = "₹" + hd1Text;
    if (hd2) hd2.innerHTML = "₹" + hd2Text;
    if (hd3) hd3.innerHTML = "₹" + hd3Text;
}

function addAvtivityCard(id, name, time, event) {
    if (!avtivityBox) return;
    let card = document.createElement("div");
    card.classList.add("activity-card");
    let avatarNum = id ? id : 11;
    card.innerHTML = `
        <div><img src="../contactUs/avatar/${avatarNum}.png" class="activity-card-avatar" alt="user avatar"></div>
        <div class="activity-card-info">
            <p class="activity-card-name">${name}</p>
            <p class="activity-card-time">${time}</p>
        </div>
        <div class="activity-card-text">${event}</div>
    `;
    avtivityBox.appendChild(card);
}

function addTableRow(name, id, date, anount) {
    let tableBody = document.getElementsByTagName("tbody")[0];
    if (!tableBody) return;
    let row = document.createElement("tr");
    let formattedAmount = (anount !== undefined && anount !== null) ? anount.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",") : "0";
    row.innerHTML = `
        <td>${name}</td>
        <td>${id}</td>
        <td>${date}</td>
        <td>₹ ${formattedAmount}</td>
    `;
    tableBody.appendChild(row);
}

/////// chart - line chart (Daily Activity)

let lineChartInstance = null;
let pieChartInstance = null;

function addLineChart(lineChartData) {
    if (!lineChartData) return;
    var dom = document.getElementById('line-chart');
    if (!dom) return;
    if (!lineChartInstance) {
        lineChartInstance = echarts.init(dom, null, {
            renderer: 'canvas',
            useDirtyRect: false
        });
        window.addEventListener('resize', function () {
            if (lineChartInstance) lineChartInstance.resize();
        });
    }

    var categories = [];
    var paidData = [];
    var deliveredData = [];
    var finishedData = [];

    for (var i = 0; i < lineChartData.length; i++) {
        var item = lineChartData[i];
        categories.push(item.product);
        paidData.push(item.Paid !== undefined ? item.Paid : 0);
        deliveredData.push(item.Delivered !== undefined ? item.Delivered : 0);
        finishedData.push(item.Finished !== undefined ? item.Finished : 0);
    }

    var option = {
        animation: false,
        backgroundColor: '#fafafa',
        tooltip: {
            trigger: 'axis',
            axisPointer: { type: 'shadow' }
        },
        grid: {
            left: '8%',
            right: '5%',
            top: '22%',
            bottom: '12%'
        },
        xAxis: {
            type: 'category',
            data: categories,
            axisTick: { alignWithLabel: true },
            axisLine: { lineStyle: { color: '#888' } }
        },
        yAxis: {
            type: 'value',
            splitLine: { lineStyle: { color: '#eaeaea' } }
        },
        series: [
            {
                name: 'Paid',
                type: 'bar',
                barWidth: 10,
                barGap: '25%',
                itemStyle: {
                    color: '#F76560',
                    borderRadius: [3, 3, 0, 0]
                },
                data: paidData
            },
            {
                name: 'Delivered',
                type: 'bar',
                barWidth: 10,
                itemStyle: {
                    color: '#1D90F4',
                    borderRadius: [3, 3, 0, 0]
                },
                data: deliveredData
            },
            {
                name: 'Finished',
                type: 'bar',
                barWidth: 10,
                itemStyle: {
                    color: '#7EB712',
                    borderRadius: [3, 3, 0, 0]
                },
                data: finishedData
            }
        ]
    };

    lineChartInstance.setOption(option, true);
}

function addPieChart(pieData) {
    if (!pieData) return;
    var dom = document.getElementById('pie-char');
    if (!dom) return;
    if (!pieChartInstance) {
        pieChartInstance = echarts.init(dom, null, {
            renderer: 'canvas',
            useDirtyRect: false
        });
        window.addEventListener('resize', function () {
            if (pieChartInstance) pieChartInstance.resize();
        });
    }

    var option = {
        animation: false,
        tooltip: {
            trigger: 'item'
        },
        series: [
            {
                type: 'pie',
                radius: ['40%', '70%'],
                avoidLabelOverlap: false,
                itemStyle: {
                    borderRadius: 10,
                    borderColor: '#fff',
                    borderWidth: 2
                },
                label: {
                    show: false,
                    position: 'center'
                },
                emphasis: {
                    label: {
                        show: true,
                        fontSize: '24',
                        fontWeight: 'bold'
                    }
                },
                labelLine: {
                    show: false
                },
                data: pieData
            }
        ]
    };

    pieChartInstance.setOption(option, true);
}

function updateDashboardData(newHeaderData, newLineData, newPieData, newTableData, newActivityData) {
    if (newHeaderData) addHeaderData(newHeaderData);
    if (newLineData) addLineChart(newLineData);
    if (newPieData) addPieChart(newPieData);
    if (newTableData) {
        let tableBody = document.getElementsByTagName("tbody")[0];
        if (tableBody) {
            tableBody.innerHTML = "";
            for (let i = 0; i < newTableData.length; i++) {
                addTableRow(newTableData[i][0], newTableData[i][1], newTableData[i][2], newTableData[i][3]);
            }
        }
    }
    if (newActivityData && avtivityBox) {
        avtivityBox.innerHTML = "";
        for (let i = 0; i < newActivityData.length; i++) {
            addAvtivityCard(newActivityData[i][0], newActivityData[i][1], newActivityData[i][2], newActivityData[i][3]);
        }
    }
}

// Initial hydration from preloaded/cached data
if (typeof headerData !== 'undefined' && typeof lineData !== 'undefined' && typeof pieData !== 'undefined') {
    updateDashboardData(headerData, lineData, pieData, (typeof tableData !== 'undefined' ? tableData : null), (typeof activityCardData !== 'undefined' ? activityCardData : null));
}
