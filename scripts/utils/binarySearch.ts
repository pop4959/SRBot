export default function binarySearch<T>(tArr: T[], tMatch: T): T[] {
    const length = tArr.length;
    let half = Math.floor(length / 2);

    if (length === 2) {
        return tArr;
    }

    if (tArr[half - 1] > tMatch && tArr[half] < tMatch) {
        return [tArr[half], tArr[--half]];
    }

    if (tArr[half] > tMatch && tArr[half + 1] < tMatch) {
        return [tArr[half], tArr[++half]];
    }

    if (tArr[0] < tMatch || tArr[length - 1] > tMatch) {
        return tArr.slice(0, 2);
    }

    if (tArr[half] < tMatch) {
        return binarySearch(tArr.slice(0, half), tMatch);
    }

    return binarySearch(tArr.slice(half), tMatch);
}
