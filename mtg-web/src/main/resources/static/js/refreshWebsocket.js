// refreshWebsocket.js

import * as StompJs from "https://cdn.jsdelivr.net/npm/@stomp/stompjs@7.0.0/+esm";

let updateInProgress = false;

export function runDatabaseRefresh({ force, updateSets, onStatus, onCount, onComplete }) {

    updateInProgress = true;

    const client = new StompJs.Client({
        brokerURL: "ws://localhost:8080/ws-refresh",

        reconnectDelay: 0,   // no reconnect needed for one-shot admin action
        debug: () => {},     // silence logs

        onConnect: () => {
            const subscription = client.subscribe("/topic/refresh-status", msg => {
                const body = JSON.parse(msg.body);

                if (body.count !== undefined && body.count >= 0) {
                    onCount?.(body.count);
                }

                if (body.message) {
                    onStatus?.(body.message);
                }

                if (body.success !== undefined) {
                    onComplete?.(body);
                    updateInProgress = false;
                    subscription.unsubscribe();
                    client.deactivate();
                }
            });

            client.publish({
                destination: "/app/refresh",
                body: JSON.stringify({ force, updateSets })
            });
        },

        onWebSocketClose: () => {
            if (updateInProgress) {
                onStatus?.("Connection closed unexpectedly");
            }
        }
    });

    client.activate();
}

window.addEventListener("beforeunload", (event) => {
    if (updateInProgress) {
        event.preventDefault();
        event.returnValue = "";
    }
});