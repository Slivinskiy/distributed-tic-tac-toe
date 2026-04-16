const startButton = document.getElementById("start-button");
const boardCells = Array.from(document.querySelectorAll(".cell"));
const moveHistoryList = document.getElementById("move-history");
const template = document.getElementById("history-item-template");
const replayBadge = document.getElementById("replay-badge");
const sessionIdValue = document.getElementById("session-id");
const gameStatusValue = document.getElementById("game-status");
const winnerValue = document.getElementById("winner");
const moveCountValue = document.getElementById("move-count");
const timelineSummary = document.getElementById("timeline-summary");

const replayDelayMs = 550;

startButton.addEventListener("click", () => {
    void startSimulation();
});

async function startSimulation() {
    setIdleBoard();
    setButtonState(true);
    setReplayBadge("running", "Running");
    timelineSummary.textContent = "Creating a session and simulating a full game through the backend services.";

    try {
        const response = await fetch("/api/simulations", { method: "POST" });
        const payload = await response.json();

        if (!response.ok) {
            throw new Error(payload.message || "Simulation failed.");
        }

        const session = payload.session;
        renderHistory(session.moveHistory);
        await replaySession(session);
    }
    catch (error) {
        setReplayBadge("idle", "Error");
        timelineSummary.textContent = error.message;
    }
    finally {
        setButtonState(false);
    }
}

function setIdleBoard() {
    boardCells.forEach((cell) => {
        cell.textContent = "";
        cell.classList.remove("x", "o", "reveal");
    });

    moveHistoryList.innerHTML = "";
    sessionIdValue.textContent = "Not started";
    gameStatusValue.textContent = "Waiting";
    winnerValue.textContent = "TBD";
    moveCountValue.textContent = "0";
}

function setButtonState(isRunning) {
    startButton.disabled = isRunning;
    startButton.textContent = isRunning ? "Simulating..." : "Start Simulation";
}

function setReplayBadge(kind, label) {
    replayBadge.className = `status-badge ${kind}`;
    replayBadge.textContent = label;
}

function renderHistory(moveHistory) {
    moveHistoryList.innerHTML = "";

    moveHistory.forEach((move) => {
        const fragment = template.content.cloneNode(true);
        const item = fragment.querySelector(".history-item");
        const title = fragment.querySelector(".history-title");
        const meta = fragment.querySelector(".history-meta");

        item.dataset.moveNumber = String(move.moveNumber);
        item.classList.add("pending");
        title.textContent = `Move ${move.moveNumber}: ${move.player} to [${move.row}, ${move.column}]`;
        meta.textContent = `Resulting status: ${formatStatus(move.resultingGameStatus)}`;

        moveHistoryList.appendChild(fragment);
    });
}

async function replaySession(session) {
    sessionIdValue.textContent = session.sessionId;
    gameStatusValue.textContent = "Replaying...";
    winnerValue.textContent = "Pending";
    moveCountValue.textContent = "0";

    for (const move of session.moveHistory) {
        applyMoveToBoard(move);
        activateHistoryItem(move.moveNumber);
        moveCountValue.textContent = String(move.moveNumber);
        timelineSummary.textContent = `${move.player} played row ${move.row}, column ${move.column}.`;
        await sleep(replayDelayMs);
    }

    gameStatusValue.textContent = formatStatus(session.gameStatus);
    winnerValue.textContent = session.winner || "Draw";
    moveCountValue.textContent = String(session.moveCount);
    timelineSummary.textContent = `Session ${session.sessionId} finished with status ${formatStatus(session.gameStatus)}.`;
    setReplayBadge("finished", "Finished");
}

function applyMoveToBoard(move) {
    const index = move.row * 3 + move.column;
    const cell = boardCells[index];
    cell.textContent = move.player;
    cell.classList.remove("reveal");
    cell.classList.toggle("x", move.player === "X");
    cell.classList.toggle("o", move.player === "O");
    void cell.offsetWidth;
    cell.classList.add("reveal");
}

function activateHistoryItem(moveNumber) {
    moveHistoryList.querySelectorAll(".history-item").forEach((item) => {
        const isCurrent = Number(item.dataset.moveNumber) === moveNumber;
        item.classList.toggle("active", isCurrent);
        item.classList.toggle("pending", Number(item.dataset.moveNumber) > moveNumber);
    });
}

function formatStatus(status) {
    if (!status) {
        return "Waiting";
    }

    return status
        .replaceAll("_", " ")
        .toLowerCase()
        .replace(/\b\w/g, (letter) => letter.toUpperCase());
}

function sleep(durationMs) {
    return new Promise((resolve) => {
        window.setTimeout(resolve, durationMs);
    });
}
