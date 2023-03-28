# graphql-complexity-handling

An example of Query complexity limitation per API Client.

- java: 17
- spring-boot: 3.0.5
- spring-graphql: 1.1.3

## Run
```bash
./gradlew bootrun
```

## Request samples
### invocation succeeded (API Key: `key1`)
```shell-session
$ curl http://localhost:8080/graphql \
    -H 'Content-Type: application/json' \
    -H 'X-Api-Key: key1' \
    -d '{"query": "query { users(first: 5) { pageInfo { hasNextPage } totalCount edges { node { id name favorites { name description } } } } }"}' | jq .
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100   633  100   497  100   136  37217  10184 --:--:-- --:--:-- --:--:-- 70333
{
  "data": {
    "users": {
      "pageInfo": {
        "hasNextPage": false
      },
      "totalCount": 3,
      "edges": [
        {
          "node": {
            "id": "u1",
            "name": "taro",
            "favorites": [
              {
                "name": "u1-fav1",
                "description": "fav1 desc"
              },
              {
                "name": "u1-fav2",
                "description": "fav2 desc"
              }
            ]
          }
        },
        {
          "node": {
            "id": "u2",
            "name": "jiro",
            "favorites": [
              {
                "name": "u2-fav1",
                "description": "fav1 desc"
              },
              {
                "name": "u2-fav2",
                "description": "fav2 desc"
              }
            ]
          }
        },
        {
          "node": {
            "id": "u3",
            "name": "saburo",
            "favorites": [
              {
                "name": "u3-fav1",
                "description": "fav1 desc"
              },
              {
                "name": "u3-fav2",
                "description": "fav2 desc"
              }
            ]
          }
        }
      ]
    }
  }
}
```

### limit exceeded (API Key: `key2`)
```shell-session
$ curl http://localhost:8080/graphql \
    -H 'Content-Type: application/json' \
    -H 'X-Api-Key: key2' \
    -d '{"query": "query { users(first: 5) { pageInfo { hasNextPage } totalCount edges { node { id name favorites { name description } } } } }"}' | jq .
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100   257  100   121  100   136   9746  10954 --:--:-- --:--:-- --:--:-- 42833
{
  "errors": [
    {
      "message": "Maximum query complexity exceeded 125 > 100",
      "extensions": {
        "classification": "ExecutionAborted"
      }
    }
  ]
}
```
