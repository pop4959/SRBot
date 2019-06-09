from sys import argv as args
from json.decoder import JSONDecodeError
from json import loads
import hashlib, time, urllib3, plotly

links = [
    "http://api.speedrunners.doubledutchgames.com/GetRankHistory?id=",
    "http://api.steampowered.com/ISteamUser/GetPlayerSummaries/v0002/?key=",
]

seasonNames = [
    "Off-season",
    "Beta season",
    "Winter season",
    "Christmas season"
]

seasonRanks = [
    [100000, 50000, 25000, 10000, 5000, 2500, 1000, 300],
    [2900, 2400, 2000, 1700, 1200, 1000, 900, 800],
    [29000, 24000, 20000, 17000, 12000, 10000, 9000, 8000],
    [29000, 24000, 20000, 17000, 12000, 10000, 9000, 8000]
]


def generator():
    yield "#69bdff"
    yield "#3789e6"
    yield "#d8d038"
    yield "#757575"
    yield "#a8512d"
    yield "#175f19"
    yield "#309b43"
    yield "#3ad94c"


def connect(link):
    return urllib3.PoolManager().request("GET", link).data


def parseJson(data):
    return loads(data)


def getKey():
    return open("data/tokensteam").read()


def binarySearch(arr, date):
    l = len(arr)
    m = l // 2
    if l == 2:
        return arr
    if arr[m - 1] > date > arr[m]:
        return [arr[m], arr[m - 1]]
    if arr[m] > date > arr[m + 1]:
        return [arr[m], arr[m + 1]]
    if arr[0] < date or arr[l - 1] > date:
        return arr[0:2]
    if arr[m] < date:
        return binarySearch(arr[0:m], date)
    return binarySearch(arr[m:], date)


if __name__ == "__main__":
    if len(args) != 3 or not 0 < int(args[2]) < 5:
        print("Wrong season provided")
        exit(0)

    key, steamId64, seasonNumber = getKey(), args[1], int(args[2])
    summaries = parseJson(connect(f"{links[1]}{key}&steamids={steamId64}"))
    reply = connect(f"{links[0]}{steamId64}")
    try:
        JSON = parseJson(reply)
    except JSONDecodeError:
        print(reply.decode("utf-8"))
        exit()

    name = summaries["response"]["players"][0]["personaname"]
    seasonName = seasonNames[seasonNumber - 1]
    layout = {
        "title": f"{name} points in {seasonName}",
        "xaxis": {
            "title": "Time",
            "type": "date",
            "showgrid": False,
            "separatethousands": True,
            "tickangle": 45
        },
        "yaxis": {
            "exponentformat": "none",
            "zeroline": False
        },
        "shapes": [{}, {}, {}, {}, {}, {}, {}, {}]
    }
    trace = {
        "x": [],
        "y": [],
        "mode": "lines+markers",
        "line": {
            "shape": "linear",
            "width": 3
        }
    }

    for tab in JSON:
        if tab["season"] != seasonNumber:
            continue
        trace["x"].append(tab["time"] * 1e3)
        trace["y"].append(tab["score"] if tab["season"] == 1 else tab["rating"])
        if len(trace["x"]) > 150:
            trace["mode"] = "lines"

    try:
        maximum = max(trace["y"])
        minimum = min(trace["y"])
    except ValueError:
        print("User didn't play in the given season")
        exit()

    shapes = layout["shapes"]
    ranks = seasonRanks[seasonNumber - 1]
    c, gen = 0, generator()
    for shape in shapes:
        c, shape["x0"], shape["x1"], shape["y0"], shape["y1"], shape["xref"], shape["line"] \
            = c + 1, 0, 1, ranks[c], ranks[c], "paper", {
            "dash": "dot",
            "width": 1.5,
            "color": gen.__next__()
        }

    i = len(shapes)
    while i > 0:
        i -= 1
        if maximum < shapes[i]["y0"] or minimum > shapes[i]["y0"]:
            del shapes[i]

    if seasonNumber == 1:
        elo = [1517443200000, 1546300800000]
        for season in elo:
            boundaries = binarySearch(trace["x"], season)
            if trace["x"][-1] == boundaries[1] or trace["x"][0] == boundaries[0]:
                continue
            layout["shapes"].append({
                "type": "rect",
                "x0": boundaries[0],
                "x1": boundaries[1],
                "y0": 0,
                "y1": 1,
                "yref": "paper",
                "fillcolor": "#d3d3d3",
                "opacity": 0.2,
                "line": {
                    "width": 1.2,
                    "color": "#000"
                }
            })

    trace["x"].reverse()
    trace["y"].reverse()

    seed = hashlib.sha1()
    seed.update(str(time.time()).encode("utf-8"))
    file = f"charts/{seed.hexdigest()[:32]}.png"

    plotly.io.write_image({
        "data": [trace],
        "layout": layout
    }, file=file, format="png", width=1500, height=750)

    print(file)
