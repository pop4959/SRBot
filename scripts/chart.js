const plotly = require("plotly")("S0rax", "vQYWbP1FwMS67s9Gojx4");
const fs = require("fs");
const axios = require("axios");
const uuid = require("uuid");

const key = fs.readFileSync("data/tokensteam").toString();

const links = [
	"http://api.speedrunners.doubledutchgames.com/GetRankHistory?id=",
	"http://api.steampowered.com/ISteamUser/GetPlayerSummaries/v0002/?key=",
];

const seasonNames = [
	"Off-season",
	"Beta season",
	"Winter season",
	"Christmas season"
];

const seasonRanks = [
	[100000, 50000, 25000, 10000, 5000, 2500, 1000, 300],
	[2900, 2400, 2000, 1700, 1200, 1000, 900, 800],
	[29000, 24000, 20000, 17000, 12000, 10000, 9000, 8000],
	[29000, 24000, 20000, 17000, 12000, 10000, 9000, 8000]
];

function* generator() {
	yield "#69bdff";
	yield "#3789e6";
	yield "#d8d038";
	yield "#757575";
	yield "#a8512d";
	yield "#175f19";
	yield "#309b43";
	yield "#3ad94c";
}

function binSearch(arr, date) {
	let l = arr.length;
	let m = ~~(l / 2);
	if (l === 2)
		return arr;
	if (arr[m - 1] > date && arr[m] < date)
		return [arr[m], arr[--m]];
	if (arr[m] > date && arr[m + 1] < date)
		return [arr[m], arr[++m]];
	if (arr[0] < date || arr[l - 1] > date)
		return arr.slice(0, 2);
	if (arr[m] < date)
		return binSearch(arr.slice(0, m), date);
	return binSearch(arr.slice(m), date);
}

class Highlight {
	constructor() {
		this.personaname = null;
		this.players = null;
		this.rating = null;
		this.season = null;
		this.score = null;
		this.count = null;
		this.time = null;
		this.tier = null;
		this.xref = null;
	}
}

/*
 * MAIN BLOCK
 */

if (process.argv.length !== 4) return;

let steamId = process.argv[2];
let seasonNumber = +process.argv[3];
let seasonName = seasonNames[seasonNumber - 1];

axios.all([
	axios.get(`${links[1]}${key}&steamids=${steamId}`),
	axios.get(`${links[0]}${steamId}`)
]).then(axios.spread((vanity, reply) => {
	if (typeof reply.data === "string") {
		console.log(reply.data);
		return;
	}

	let name = vanity.data.response.players[0].personaname,
		layout = {
			title: `${name} points in ${seasonName}`,
			xaxis: {
				title: "Time",
				type: "date",
				showgrid: false,
				separatethousands: true,
				tickangle: 45
			},
			yaxis: {
				exponentformat: "none",
				zeroline: false
			},
			shapes: [{}, {}, {}, {}, {}, {}, {}, {}]
		},
		trace = {
			x: [],
			y: [],
			mode: "lines+markers",
			line: {
				shape: "lines",
				width: 3
			}
		};


	for (let tab of reply.data) {
		if (tab.season !== seasonNumber)
			continue;
		if (trace.x.length > 150)
			trace.mode = "lines";
		trace.x.push(tab.time * 1e3);
		trace.y.push(tab.season === 1 ? tab.score : tab.rating);
	}

	if (trace.y.length === 0) {
		console.log("User didn't play in the given season");
		return;
	}

	let c = 0,
		gen = generator(),
		shapes = layout.shapes,
		max = Math.max(...trace.y),
		min = Math.min(...trace.y),
		ranks = seasonRanks[seasonNumber - 1];

	for (let shape of shapes) {
		[shape.x0, shape.x1, shape.y0, shape.y1, shape.xref, shape.line] =
			[0, 1, ranks[c], ranks[c++], "paper", {
				dash: "dot",
				width: "1.5",
				color: gen.next().value
			}];
	}

	let i = shapes.length;
	while (i--) {
		if (max < shapes[i].y0 || min > shapes[i].y0) {
			shapes.splice(i, 1);
		}
	}

	if (seasonNumber === 1) {
		let elo = [1517443200000, 1546300800000];

		for (let season of elo) {
			let boundaries = binSearch(trace.x, season);
			if (trace.x[trace.x.length - 1] === boundaries[1] || trace.x[0] === boundaries[0])
				continue;
			layout.shapes.push({
				type: "rect",
				x0: boundaries[0],
				x1: boundaries[1],
				y0: 0,
				y1: 1,
				yref: "paper",
				fillcolor: "#d3d3d3",
				opacity: 0.2,
				line: {
					width: "1.2",
					color: "#000"
				}
			})
		}
	}

	let last = trace.y.shift();
	trace.x.reverse();
	trace.y.reverse();
	trace.x.push(Date.now());
	trace.y.push(last, last);

	let figure = {
		data: [trace],
		layout: layout
	};

	let imgOpts = {
		format: 'png',
		width: 1500,
		height: 750
	};

	plotly.getImage(figure, imgOpts, (error, image) => {
		if (error) return console.log(error);
		let hash = uuid().replace(/-/g, "");
		let file = fs.createWriteStream(`charts/${hash}.png`);
		let stream = image.pipe(file);
		stream.on("finish", () => console.log(`charts/${hash}.png`));
	});
})).catch((error) => {
	console.error(error);
});