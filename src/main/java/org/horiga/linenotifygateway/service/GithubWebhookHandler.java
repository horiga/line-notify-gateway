package org.horiga.linenotifygateway.service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GithubWebhookHandler extends WebhookHandler {

    //
    // Event types:
    // https://developer.github.com/webhooks/#events
    //
    // - 'commit_comment'
    // - 'issue_comment'
    // - 'pull_request_review_comment'
    // - 'pull_request_review'
    // - 'pull_request'
    //
    // - 'push'


    public GithubWebhookHandler(
            String serviceName,
            NotifyService notifyService) {
        super(serviceName, notifyService);
    }
}
