package com.faf.jiggly.pocket;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "jiggly.encryption-key=FSC4ukfGdXLhZkfugiE+MukosyckKOospVojwqYwa1Y=",
        "jiggly.storage-dir=target/test-uploads"
})
class JigglyPocketApplicationTests {

    @Test
    void contextLoads() {
    }
}
