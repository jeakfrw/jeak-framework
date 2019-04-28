package de.fearnixx.jeak.controller.testImpls;

import de.fearnixx.jeak.controller.connection.RequestMethod;
import de.fearnixx.jeak.controller.reflect.RequestBody;
import de.fearnixx.jeak.controller.reflect.RequestMapping;
import de.fearnixx.jeak.controller.reflect.RequestParam;
import de.fearnixx.jeak.controller.reflect.RestController;

@RestController(endpoint = "/test")
public class TestController {

    @RequestMapping(method = RequestMethod.GET, endpoint = "/hello")
    public String hello() {
        return "hallo";
    }

    @RequestMapping(method =  RequestMethod.GET, endpoint = "/info/:name")
    public String returnSentInfo(@RequestParam(name = "name") String name) {
        return "received " + name;
    }

    @RequestMapping(method = RequestMethod.POST, endpoint = "/body")
    public String sendBody(@RequestBody(type = String.class, name = "string") String string) {
        return "this is the body " + string;
    }
}
