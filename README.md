# line-notify-gateway
Message notification via LINE Notify with personal token.

## About LINE Notify
https://notify-bot.line.me/en/

##Run
```bash
$JAVA_HOME/bin/java -Dlogging.file=${YOUR_LOG_DIR} -Dlogging.level.ROOT=${LEVEL} -jar line-notify-gateway.jar
```

## GitHub Webhook events
```
POST /v1/github/payload?noify_token=${YOUR_LINE_NOTIFY_PRIVATE_ACCESS_TOKEN}
Content-Type: application/json
```

### Support Webhook Event Types
 Currently support [GitHub Event Types](https://developer.github.com/webhooks/#events) are `create`, `issues`, `issue_comment`, `pull_request`, `pull_request_review`, `pull_request_review_comment`, `release`
