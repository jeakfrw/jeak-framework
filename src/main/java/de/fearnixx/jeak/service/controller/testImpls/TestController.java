package de.fearnixx.jeak.service.controller.testImpls;

import de.fearnixx.jeak.service.controller.RequestMethod;
import de.fearnixx.jeak.reflect.RequestBody;
import de.fearnixx.jeak.reflect.RequestMapping;
import de.fearnixx.jeak.reflect.RequestParam;
import de.fearnixx.jeak.reflect.RestController;

@RestController(pluginId = "testPluginId", endpoint = "/test")
public class TestController {

    @RequestMapping(method = RequestMethod.GET, endpoint = "/hello")
    public DummyObject hello() {
        return new DummyObject("Finn", 20);
    }

    @RequestMapping(method =  RequestMethod.GET, endpoint = "/info")
    public String returnSentInfo(@RequestParam(name = "name") String name) {
        return "received " + name;
    }

    @RequestMapping(method = RequestMethod.POST, endpoint = "/body")
    public String sendBody(@RequestBody(type = String.class, name = "string") String string) {
        return "this is the body " + string;
    }
}
