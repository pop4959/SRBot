import * as fs from 'fs';
import axios, { HttpStatusCode } from 'axios';
import SteamAPI from 'steamapi';
import Plotly from 'plotly';
import binarySearch from './utils/binarySearch';
import Config from './types/config';
import RankHistory from './types/rankHistory';
import Shape from './types/shape';

(async () => {
    const args = process.argv;
    if (args.length !== 5) return;

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
    const layout = {
        title: `${summary.nickname} points in ${season.seasonName}`,
        xaxis: {
            title: 'Time',
            type: 'date',
            showgrid: false,
            separatethousands: true,
            tickangle: 45
        },
        yaxis: {
            exponentformat: 'none',
            zeroline: false
        },
        shapes: new Array(season.seasonRanks.length - 1).fill({}) as Shape[]
    };
    const trace = {
        x: [] as number[],
        y: [] as number[],
        mode: 'lines+markers',
        line: {
            shape: 'lines',
            width: 3
        }
    };

    for (const tab of rankHistory) {
        if (tab.season !== seasonNumber) {
            continue;
        }

        if (trace.x.length > 150) {
            trace.mode = 'lines';
        }

        trace.x.push(tab.time * 1e3);
        trace.y.push(tab.season === 1 ? tab.score : tab.rating);
    }

    if (trace.y.length === 0) {
        console.log('User didn\'t play in the given season');
        return;
    }

    const shapes = layout.shapes;
    const ranks = season.seasonRanks;
    const max = Math.max(...trace.y);
    const min = Math.min(...trace.y);

    // add shapes for ranks
    shapes.forEach((_, index) => {
        shapes[index] = {
            x0: 0,
            x1: 1,
            y0: ranks[index].rankPoints,
            y1: ranks[index].rankPoints,
            xref: 'paper',
            line: {
                dash: 'dot',
                width: '1.5',
                color: config.rankColours[index]
            },
            label: {
                text: ranks[index].rankName
            }
        };
    });

    // remove shapes for ranks beyond user rating
    for (let i = shapes.length - 1; i >= 0; i--) {
        if (max < shapes[i].y0 || min > shapes[i].y0) {
            shapes.splice(i, 1);
        }
    }

    // gray out areas that would have had elo seasons
    if (season.seasonName === 'Off-season') {
        const eloSeasons = [new Date('2018-02-01'), new Date('2019-01-01')];

        for (let season of eloSeasons) {
            const boundaries = binarySearch(trace.x, Number(season));
            if (trace.x.at(-1) === boundaries.at(-1) || trace.x.at(0) === boundaries.at(0)) {
                continue;
            }

            shapes.push({
                type: 'rect',
                x0: boundaries.at(0),
                x1: boundaries.at(-1),
                y0: 0,
                y1: 1,
                yref: 'paper',
                fillcolor: '#d3d3d3',
                opacity: 0.2,
                line: {
                    width: '1.2',
                    color: '#000'
                }
            })
        }
    }

    trace.x.reverse();
    trace.y.reverse();

    const figure = {
        data: [trace],
        layout: layout
    };

    const imgOpts = {
        format: 'png',
        width: 1500,
        height: 750
    };


    const plotly = new Plotly(config.plotly.plotlyUsername, config.plotly.plotlyApikey);
    plotly.getImage(figure, imgOpts, (error, image) => {
        if (error) {
            console.error(error);
            return;
        }
        const dir = 'charts';
        if (!fs.existsSync(dir)) {
            fs.mkdirSync(dir);
        }

        const path = `${dir}/${Date.now()}.png`;
        const file = fs.createWriteStream(path);
        const stream = image.pipe(file);
        stream.on('finish', () => console.log(path));
    });
})();