package de.fearnixx.jeak.controller.testImpls;

import de.fearnixx.jeak.controller.connection.RequestMethod;
import de.fearnixx.jeak.controller.reflect.RequestMapping;
import de.fearnixx.jeak.controller.reflect.RestController;

@RestController(name = "test")
public class TestController {

    @RequestMapping(method = RequestMethod.GET, endpoint = "hello")
    public String hello() {
        return "hallo";
    }

    @RequestMapping(method =  RequestMethod.GET, endpoint = "info")
    public String returnSentInfo(String name) {
        return "received" + name;
    }
}
