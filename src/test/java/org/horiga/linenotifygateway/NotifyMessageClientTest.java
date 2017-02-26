package org.horiga.linenotifygateway;

import org.horiga.linenotifygateway.model.NotifyMessage;
import org.horiga.linenotifygateway.service.NotifyMessageClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.common.collect.Lists;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@RunWith(SpringRunner.class)
@SpringBootTest
public class NotifyMessageClientTest {

    private static final String TEST_TOKEN = "xxxxxxxxxx";

    @Autowired
    NotifyMessageClient notifyClient;

    @Test
    public void send() throws Exception {
        notifyClient.send(
                new NotifyMessage("direct-test",
                                  "こんにちは、これはテストです。(；・∀・)",
                                  "",
                                  "",
                                  Lists.newArrayList(TEST_TOKEN)));
        Thread.sleep(500);
    }
}
