import * as fs from 'fs';
import axios, { HttpStatusCode } from 'axios';
import SteamAPI from 'steamapi';
import QuickChart from 'quickchart-js';
import binarySearch from './utils/binarySearch';
import Config from './types/config';
import RankHistory from './types/rankHistory';

(async () => {
    const args = process.argv;
    if (args.length !== 4) return;

    const config = JSON.parse(fs.readFileSync('./config.json', { encoding: 'utf8' })) as Config;

    const [steamId, seasonNumber] = [process.argv[2], Number(process.argv[3])];
    const season = config.seasons.find(s => s.seasonNumber === seasonNumber)!;

    const steamApi = new SteamAPI(config.steamApiKey);
    const summary = await steamApi.getUserSummary(steamId);
    const rankHistoryRes = await axios.get<RankHistory[]>(config.ddApiRankUrl, { params: { id: steamId } });

    if (rankHistoryRes.status !== HttpStatusCode.Ok) {
        console.error(rankHistoryRes.data);
        return;
    }

    const rankHistory = rankHistoryRes.data;

    const labels: string[] = [];
    const points: number[] = [];
    let mode: 'lines+markers' | 'lines' = 'lines+markers';

    for (const tab of rankHistory) {
        if (tab.season !== seasonNumber) {
            continue;
        }

        if (points.length > 150) {
            mode = 'lines';
        }

        labels.push(new Date(tab.time * 1e3).toISOString());
        points.push(tab.season === 1 ? tab.score : tab.rating);
    }

    if (points.length === 0) {
        console.log('User didn\'t play in the given season');
        return;
    }

    labels.reverse();
    points.reverse();

    const max = Math.max(...points);
    const min = Math.min(...points);
    const annotations: Record<string, unknown> = {};

    // Add rank lines
    season.seasonRanks.forEach((rank, index) => {
        if (max < rank.rankPoints || min > rank.rankPoints) {
            return;
        }

        annotations[`rank-${index}`] = {
            type: 'line',
            scaleID: 'y',
            value: rank.rankPoints,
            borderColor: config.rankColours[index],
            borderWidth: 1.5,
            borderDash: [4, 4],
            label: {
                display: true,
                content: rank.rankName,
                position: 'end',
                backgroundColor: 'rgba(0, 0, 0, 0.65)',
                color: '#ffffff'
            }
        };
    });

    // Gray out areas that would have had elo seasons
    if (season.seasonName === 'Off-season') {
        const eloSeasons = [new Date('2018-02-01'), new Date('2019-01-01')];
        const timestamps = labels.map(label => Number(new Date(label)));

        for (const eloSeason of eloSeasons) {
            const boundaries = binarySearch(timestamps, Number(eloSeason));

            if (timestamps.at(-1) === boundaries.at(-1) || timestamps.at(0) === boundaries.at(0)) {
                continue;
            }

            annotations[`elo-season-${Number(eloSeason)}`] = {
                type: 'box',
                xScaleID: 'x',
                xMin: new Date(boundaries.at(0)!).toISOString(),
                xMax: new Date(boundaries.at(-1)!).toISOString(),
                backgroundColor: 'rgba(211, 211, 211, 0.2)',
                borderColor: '#000000',
                borderWidth: 1.2
            };
        }
    }

    const chart = new QuickChart();

    chart.setWidth(1500);
    chart.setHeight(750);
    chart.setVersion('4');

    chart.setConfig({
        type: 'line',
        data: {
            labels,
            datasets: [
                {
                    label: `${summary.nickname} points`,
                    data: points,
                    borderWidth: 3,
                    borderColor: '#3366cc',
                    backgroundColor: '#3366cc',
                    pointRadius: mode === 'lines' ? 0 : 3,
                    pointHoverRadius: mode === 'lines' ? 0 : 4,
                    tension: 0,
                    fill: false
                }
            ]
        },
        options: {
            responsive: false,
            plugins: {
                title: {
                    display: true,
                    text: `${summary.nickname} points in ${season.seasonName}`
                },
                legend: {
                    display: false
                },
                // annotation: {
                //     annotations
                // }
            },
            scales: {
                x: {
                    type: 'time',
                    title: {
                        display: true,
                        text: 'Time'
                    },
                    grid: {
                        display: false
                    },
                    ticks: {
                        maxRotation: 45,
                        minRotation: 45
                    }
                },
                y: {
                    beginAtZero: false,
                    ticks: {
                        callback: (value: any) => `${value}`
                    },
                    grid: {
                        drawTicks: true
                    }
                }
            }
        }
    });

    const dir = 'charts';
    if (!fs.existsSync(dir)) {
        fs.mkdirSync(dir);
    }

    const path = `${dir}/${Date.now()}.png`;
    await chart.toFile(path);

    console.log(path);
})();
