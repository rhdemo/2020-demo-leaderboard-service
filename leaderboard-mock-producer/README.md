# Leaderboard Mock Producer

This is a mock data producer project, that will send data to the multiple kafka mirror streams

## Data Structure

```json
{
    "game": {
        "date": "2020-02-24T12:45:35.000Z",
        "id": "new-game-1582548335",
        "state": "active"
    },
    "player": {
        "avatar": "{\"body\":4,\"color\":4,\"ears\":1,\"eyes\":0,\"mouth\":1,\"nose\":2}",
        "clusterSource": "sg",
        "game": {
            "date": "2020-02-24T12:45:35.000Z",
            "id": "new-game-1582548335",
            "state": "active"
        },
        "playerId": "pluto",
        "playerName": "pluto",
        "right": 3,
        "score": 1484,
        "wrong": 7
    }
}
```

## Start Streaming

By default the deployment will have replicas as `0`, to start streaming scale up the replica:

```shell
oc -n leaderboard scale deployments leaderboard-mock-producer --replicas=1
```

## Stop Streaming

```shell
oc -n leaderboard scale deployments leaderboard-mock-producer --replicas=0
```

## Adjusting Tick Time

The application sends new game data every 20 seconds, to modify it run the following command:
```shell
oc -n leaderboard scale deployments leaderboard-mock-producer --replicas=0
```