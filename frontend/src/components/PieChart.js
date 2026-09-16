import React, { useEffect, useState } from 'react';
import { Pie } from 'react-chartjs-2';
import { Chart as ChartJS, ArcElement, Tooltip, Legend } from 'chart.js';

ChartJS.register(ArcElement, Tooltip, Legend);

function PieChart() {
    const [chartData, setChartData] = useState({
        labels: ['Category 1', 'Category 2', 'Category 3'],
        datasets: [
            {
                label: 'Categories',
                data: [30, 50, 20], // Initial data
                backgroundColor: ['#FF6384', '#36A2EB', '#FFCE56'],
                hoverBackgroundColor: ['#FF6384', '#36A2EB', '#FFCE56'],
            },
        ],
    });

    useEffect(() => {
        // Simulate reading data from the database and updating the chart
        fetch('/api/data') // Assume this is the API for reading database data
            .then((response) => response.json())
            .then((data) => {
                setChartData({
                    labels: data.labels,
                    datasets: [
                        {
                            label: 'Categories',
                            data: data.values, // Dynamically updated data
                            backgroundColor: ['#FF6384', '#36A2EB', '#FFCE56'],
                            hoverBackgroundColor: ['#FF6384', '#36A2EB', '#FFCE56'],
                        },
                    ],
                });
            });
    }, []);

    return <Pie data={chartData} />;
}

export default PieChart;
