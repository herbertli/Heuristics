const colors = ['red', 'blue', 'green', 'orange', 'yellow', 'purple', 'silver', 'olive', 'teal'];

const colorRGB = [
    [255, 0, 0],
    [0, 0, 255],
    [0, 255, 0],
    [255, 165, 0],
    [255, 255, 0],
    [128, 0, 128],
    [192, 192, 192],
    [128, 128, 0],
    [0, 128, 128],
];

const checkValid = (nx, ny, piecesList, minDist) => {
    for (let piece of piecesList) {
        const { x, y } = piece;
        const dist = Math.pow(nx - x, 2) + Math.pow(ny - y, 2);
        if (dist < Math.pow(minDist, 2)) return false;
    }
    return true;
}

const calculateBoard = (width, height, piecesList, numPlayers) => {
    let owners = [];
    let scores = [];
    for (let i = 0; i < numPlayers; i++) scores.push(0);
    for (let x = 0; x < width; x += 1) {
        owners.push([]);
        for (let y = 0; y < height; y += 1) {
            let influences = [];
            for (let i = 0; i < numPlayers; i++) influences.push(0);
            for (let piece of piecesList) {
                const pieceInfluence = piece.weight / (Math.pow(piece.x - x, 2) + Math.pow(piece.y - y, 2));
                influences[piece.playerInd] += pieceInfluence;
            }
            const maxInfluence = Math.max(...influences);
            if (maxInfluence !== 0) {
                owners[x][y] = influences.indexOf(maxInfluence);
                scores[owners[x][y]] += 1;
            } else {
                owners[x][y] = -1;
            }
        }
    }
    return { owners, scores };
}

export { colors, colorRGB, checkValid, calculateBoard };