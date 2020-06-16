# line-notify-gateway
Message notification via LINE Notify with personal token.

## About LINE Notify
https://notify-bot.line.me/en/

## Run
```
$JAVA_HOME/bin/java -Dlogging.file=${YOUR_LOG_DIR} -Dlogging.level.ROOT=${LEVEL} -jar line-notify-gateway.jar
```

### Run from Docker
```
docker run -d -p 18081:18081 -e line-notify.personal-access-token='${YOUR_LINE_NOTIFY_PRIVATE_ACCESS_TOKEN}' --name line-notify-gateway nontster/line-notify-gateway
```

## Instant alert


```
POST /v1/notify-with-token?noify_token=${YOUR_LINE_NOTIFY_PRIVATE_ACCESS_TOKEN}&notify_service=${YOUR_SERVICE_NAME}&message=${YOUR_MESSAGE}
```
* noify_token (required)
* notify_service (required)
* message (required)
* thumbnail_url (optional)
* image_url (optional)
* sticker (optional)

If you specify access token via environment variable when start application you can omit noify_token parameter and use below API instead 
```
POST /v1/notify?notify_service=${YOUR_SERVICE_NAME}&message=${YOUR_MESSAGE}
```
* notify_service (required)
* message (required)
* thumbnail_url (optional)
* image_url (optional)
* sticker (optional)


Sticker list (https://devdocs.line.me/files/sticker_list.pdf)

## Prometheus Alertmanager Webhook event
```
POST /v1/alertmanager/payload?noify_token=${YOUR_LINE_NOTIFY_PRIVATE_ACCESS_TOKEN}
Content-Type: application/json
```

### JSON message format from Prometheus Alertmanager
```
{
  "version": "4",
  "groupKey": <string>,    // key identifying the group of alerts (e.g. to deduplicate)
  "status": "<resolved|firing>",
  "receiver": <string>,
  "groupLabels": <object>,
  "commonLabels": <object>,
  "commonAnnotations": <object>,
  "externalURL": <string>,  // backlink to the Alertmanager.
  "alerts": [
    {
      "status": "<resolved|firing>",
      "labels": <object>,
      "annotations": <object>,
      "startsAt": "<rfc3339>",
      "endsAt": "<rfc3339>",
      "generatorURL": <string> // identifies the entity that caused the alert
    },
    ...
  ]
}
```

### Line Notify template for Firing Alert
```
Alert {{groupLabels.alertname}} is {{status}}
Severity {{commonLabels.severity}}

Affected nodes,
{{#alerts}}
  - {{labels.instance}}
{{/alerts}}
```

### Line Notify template for Resolved Alert
```
Alert {{groupLabels.alertname}} for nodes below has been {{status}}
{{#alerts}}
    - {{labels.instance}}
{{/alerts}}
```

### Sticker (STKPKGID, STKID)

* (2,24) for alert firing
* (1,114) for alert resolved

#### About Mustache
* [Introduction to Mustache](https://www.baeldung.com/mustache)

## GitHub Webhook events
```
POST /v1/github/payload?noify_token=${YOUR_LINE_NOTIFY_PRIVATE_ACCESS_TOKEN}
Content-Type: application/json
```

### Support Webhook Event Types
 Currently support [GitHub Event Types](https://developer.github.com/webhooks/#events) are `create`, `issues`, `issue_comment`, `pull_request`, `pull_request_review`, `pull_request_review_comment`, `release`

## Management

```
GET /management/metrics
```